package com.ok.o_k;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class add_cloth_Activity extends AppCompatActivity {
    boolean isSlideOpen = false;
    Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;

    //이미지 선택 1)사진찍기 2)앨범에서 선택
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private static final int REQUEST_CAMERA_CROP = 5555;

    ImageView img;
    Button btn_add_image;

    private String mCurrentPhotoPath;
    private Uri photoURI;
    private Uri albumURI;
    private Uri cameraURI;

    //라디오 버튼
    RadioGroup classiRad;
    RadioGroup thickRad;
    RadioGroup lengthRad;

    //옷 추가 버튼
    Button btnAdd;

    //SQLite
    private DBHelper dbHelper = new DBHelper(add_cloth_Activity.this, "MyDB.db", null, 1);

    //Permission
    private boolean isAccessCamera = false;
    private boolean isAccessStorage = false;
    private boolean isPermission = false;
    private final int PERMISSIONS_CAMERA = 111;
    private final int PERMISSIONS_EXTERNAL_STORAGE = 222;

    /**
     * @brief onCreate() method add_cloth_Activity.java
     * @detail Can add clothe image and select each category info to Database
     * @date 2019.05.15
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_cloth);

        if(!isPermission){
            callPermission();
            return;
        }
        img = findViewById(R.id.image_view);
        btn_add_image = findViewById(R.id.btn_add_image);

        //Show Slide Menu
        slideMenu = findViewById(R.id.slideMenu);
        non_showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        slideMenu.bringToFront();

        add_cloth_Activity.SlidingPageAnimationListener animationListener = new add_cloth_Activity.SlidingPageAnimationListener();
        showMenu.setAnimationListener(animationListener);
        non_showMenu.setAnimationListener(animationListener);

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.bringToFront();
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSlideOpen){
                    slideMenu.startAnimation(non_showMenu);
                }
                else{
                    slideMenu.setVisibility(View.VISIBLE);
                    slideMenu.startAnimation(showMenu);
                }
            }
        });

        //Activity 전환
        closetMenu = findViewById(R.id.closet_menu);
        weatherMenu = findViewById(R.id.weather_menu);
        settingMenu = findViewById(R.id.setting_menu);

        closetMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        weatherMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), weather_Activity.class);
                startActivity(intent);
                finish();
            }
        });
        settingMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), setting_Activity.class);
                startActivity(intent);
                finish();
            }
        });


        //최종 추가버튼
        btnAdd = findViewById(R.id.add_clothe);
    }

    //Slide menu animation

    /**
     * @brief Menu animation
     * @detail If you click menu_button, the menu is visible or invisible
     * @var boolean isSlide
     * menu flag visible = true, invisible = false
     */
    private class SlidingPageAnimationListener implements Animation.AnimationListener{
        @Override
        public void onAnimationEnd(Animation animation){
            if(isSlideOpen){
                slideMenu.setVisibility(View.INVISIBLE);
                isSlideOpen = false;
            }
            else{
                isSlideOpen = true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation){

        }
        @Override
        public void onAnimationStart(Animation animation){

        }
    }

    /**
     * @brief Add image button
     * @detail
     * If click 'take a picture', link camera. So you can take a picture and crop image.
     * If click 'get album', link gallery. So you can get image in gallery and crop image.
     */
    public void mOnClick(View v){
        PopupMenu popup = new PopupMenu(add_cloth_Activity.this, v);

        MenuInflater inflater = popup.getMenuInflater();
        Menu menu = popup.getMenu();

        inflater.inflate(R.menu.add_clothe_popupmenu, menu);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.useCamera:
                        captureCamera();
                        return true;
                    case R.id.useAlbum:
                        getAlbum();
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }
    /**
     * @brief Store result to database
     * @detail Store the selected radio button values for each category and images in the database
     * @var int classID
     * @var int thickID
     * @var int lengthID
     */
    public void addOnClick(View v){
        SQLiteDatabase db;
        String sql;

        //라디오 그룹
        classiRad = findViewById(R.id.classification);
        thickRad = findViewById(R.id.thickness);
        lengthRad = findViewById(R.id.length);

        int classiID = classiRad.getCheckedRadioButtonId();
        RadioButton classiBtn = findViewById(classiID);
        String classiOutput =  classiBtn.getText().toString();

        int thickID = thickRad.getCheckedRadioButtonId();
        RadioButton thickBtn = findViewById(thickID);
        String thickOutput = thickBtn.getText().toString();

        int lengthId = lengthRad.getCheckedRadioButtonId();
        RadioButton lengBtn = findViewById(lengthId);
        String lengOutput = lengBtn.getText().toString();

        byte[] imageOutput = getBytes(img.getDrawable());

        db = dbHelper.getWritableDatabase();

        sql = "INSERT INTO closet VALUES(null,'" + classiOutput + "','" + thickOutput + "','" + lengOutput + "', ?);";
        SQLiteStatement p = db.compileStatement(sql);
        p.bindBlob(1, imageOutput);
        p.executeInsert();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    //사진 가져오기

    /**
     * @brief Take a picture
     * @detail Take a picture using camera app and start onActivityResult()
     * @var Intent takePictureIntent
     * intent variance using camera app
     */
    private void captureCamera(){
        Log.i("check1","check");
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Log.i("check1","check");
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                Log.i("check1","check");
                File photoFile = null;
                try{
                    Log.i("check1","check");
                    photoFile = createImageFile();
                    Log.i("check1","check");
                }catch (IOException ex){
                    Log.e("capture Camera Error", ex.toString());
                }
                Uri provider = FileProvider.getUriForFile(add_cloth_Activity.this, "com.ok.o_k", photoFile);
                photoURI = provider;
                Log.i("photoURI", photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, provider);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }else{
            Log.i("알림", "저장공간에 접근 불가능");
            return;
        }
    }

    /**
     * @brief Create Image File to store image
     * @detail Set file name and path to store the picture file.
     * @var File image
     * Variance to store image file
     * @var String mCurrentPhotoPath
     *  File's path variance to store image
     * @return image
     * @throws IOException
     */
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp+".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "ok");

        if(!storageDir.exists()){
            Log.i("mCurrentPhotoPath", storageDir.toString());
            storageDir.mkdirs();
        }
        File image = new File(storageDir, imageFileName);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i("check", mCurrentPhotoPath);
        return image;
    }

    /**
     * @brief Get image to gallery
     * @detail Get image and start onActivityResult()
     */
    private void getAlbum(){
        Log.i("getAlbum", "call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    /**
     * @brief Store image file
     * @detail Save the image file using the path saved in mCurrentPhotoPath
     * @var String mCurrentPhotoPath
     * File's path variance to store image
     */
    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }


    /**
     * @brief Crop image that got from the gallery
     * @detail Obtain the permission and crop image
     */
    private void cropImage(){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");

        cropIntent.putExtra("outputX", 350);
        cropIntent.putExtra("outputY", 350);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI);
        Log.i("albumURI", albumURI.toString());
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    /**
     * @brief Crop image that toke a picture
     * @detail Obtain the permission and crop image
     */
    private void cropCamera(){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");

        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", cameraURI);
        startActivityForResult(cropIntent, REQUEST_CAMERA_CROP);
    }

    /**
     * @brief To use the results from startActivityForResult
     * @param requestCode
     * second argument value of startActivityForResult()
     * @param resultCode
     * success or failure value set in recalled activity
     * @param data
     * Results saved from invoked activity
     *
     *
     */
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        img = findViewById(R.id.image_view);
        switch (requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try{
                        File cameraFile = null;
                        cameraFile = createImageFile();
                        cameraURI = Uri.fromFile(cameraFile);
                        cropCamera();
                    }catch (Exception ignored){  }
                } else{
                    Toast.makeText(add_cloth_Activity.this, "사진찍기를 취소하였습니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if(resultCode == RESULT_OK){
                    if(data.getData() != null){
                        try{
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        } catch (Exception e){
                            Log.e("TAKE_ALBUM_SINGLE Error", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if(resultCode == Activity.RESULT_OK){
                    galleryAddPic();
                    try{
                        Log.i("check", albumURI.toString());
                        img.setImageURI(albumURI);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;

            case REQUEST_CAMERA_CROP:
                if(resultCode == Activity.RESULT_OK){
                    galleryAddPic();
                    try{
                        Log.i("check", cameraURI.toString());
                        img.setImageURI(cameraURI);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    /**
     * @brief Chage bitmap
     * @detail Conver from bitmap to byte array to store image file into database
     * @return stream.toByteArray()
     */
    private byte[] getBytes(Drawable d) {
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_CAMERA
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCamera= true;

        }

        if (requestCode == PERMISSIONS_EXTERNAL_STORAGE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessStorage = true;
        }

        if (isAccessCamera && isAccessStorage) {
            isPermission = true;
        }
    }

    // GPS 권한 요청
    private void callPermission() {
        /**
         *  @brief Check the SDK version and whether the permission is already granted or not.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSIONS_CAMERA);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_EXTERNAL_STORAGE);
        }

        isPermission = true;
    }
}

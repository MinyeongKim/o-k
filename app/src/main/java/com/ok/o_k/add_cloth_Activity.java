package com.ok.o_k;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class add_cloth_Activity extends AppCompatActivity {
    private boolean isSlideOpen = false;
    private Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;

    //이미지 선택 1)사진찍기 2)앨범에서 선택
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private static final int REQUEST_CAMERA_CROP = 5555;

    private ImageView img;
    private Button btn_add_image;

    private String mCurrentPhotoPath;
    private Uri imageURI;
    private Uri photoURI;
    private Uri albumURI;
    private Uri cameraURI;

    //라디오 버튼
    private RadioGroup classiRad;
    private RadioGroup thickRad;
    private RadioGroup lengthRad;

    //옷 추가 버튼
    private Button btnAdd;

    //SQLite
    private DBHelper dbHelper;

    /**
     * @brief onCreate() method add_cloth_Activity.java
     * @detail Can add clothe image and select each category info to Database
     * @date 2019.05.15
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_cloth);

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
        dbHelper = new DBHelper(add_cloth_Activity.this, "MyDB.db", null, 1);
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

    //사진 가져오기

    /**
     * @brief Take a picture
     * @detail Take a picture using camera app and start onActivityResult()
     * @var Intent takePictureIntent
     * intent variance using camera app
     */
    private void captureCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException ex){
                //Error occurred while creating the File
            }

            if(photoFile != null){
                Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                imageURI = providerURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
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
    private File createImageFile() throws  IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * @brief Get image to gallery
     * @detail Get image and start onActivityResult()
     */
    private void getAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
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
        cropIntent.setDataAndType(imageURI, "image/*");

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
        switch (requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    try{
                        File cameraFile = null;
                        cameraFile = createImageFile();
                        cameraURI = Uri.fromFile(cameraFile);
                        cropCamera();
                        img.setImageURI(imageURI);
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
                    img.setImageURI(albumURI);
                }
                break;

            case REQUEST_CAMERA_CROP:
                if(resultCode == Activity.RESULT_OK){
                    galleryAddPic();
                    img.setImageURI(cameraURI);
                }
                break;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch (requestCode){
            case MY_PERMISSION_CAMERA:
                for(int i = 0; i< grantResults.length; i++){
                    if(grantResults[i] < 0){
                        Toast.makeText(add_cloth_Activity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                break;
        }
    }

    //추가하기 버튼

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

    //Bitmap chage
    // convert from bitmap to byte array

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

}


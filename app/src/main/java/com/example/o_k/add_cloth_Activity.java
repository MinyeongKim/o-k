package com.example.o_k;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class add_cloth_Activity extends AppCompatActivity {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    ImageView view;
    Button btn_add_image;

    String mCurrentPhotoPath;

    Uri imageURI;
    Uri photoURI, albumURI;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_cloth);

        view = findViewById(R.id.image_view);
        btn_add_image = findViewById(R.id.add_image);

        registerForContextMenu(btn_add_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_clothe_image, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.takePicture:
                captureCamera();
            case R.id.gellery:
                getAlbum();
        }

        checkPermission();

        return false;
    }

    private void captureCamera(){
        String state = Environment.getExternalStorageState();

        //외장 메모리 검사
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                } catch (IOException ex){
                    Log.e("captureCamera Error", ex.toString());
                }

                if(photoFile != null){
                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageURI = providerURI;

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }else{
            Toast.makeText(this, "저장공간에 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File createImageFile() throws  IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures","gyeom");

        if(!storageDir.exists()){
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void getAlbum(){
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다", Toast.LENGTH_SHORT).show();
    }

    public void cropImage(){
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : "+photoURI+ " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");

        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode == Activity.RESULT_OK){
                    try{
                        Log.i("REQUEST_TAKE_PHOTO", "OK");
                        galleryAddPic();

                        view.setImageURI(imageURI);
                    } catch (Exception e){
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                } else{
                    Toast.makeText(add_cloth_Activity.this, "사진찍기를 취소하였습니다", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if(resultCode == Activity.RESULT_OK){

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
                    view.setImageURI(albumURI);
                }
                break;
        }
    }

    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))){
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:"+getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
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
}


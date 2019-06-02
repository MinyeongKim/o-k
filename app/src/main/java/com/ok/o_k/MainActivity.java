package com.ok.o_k;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean isSlideOpen = false;
    boolean isImageMenu = false;
    private Button btnAddClothe;
    private Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;
    private Spinner category;
    private ImageView img;
    String cate;
    private GridView grid;


    //카테고리 별 이미지 저장소
    private ArrayList<Bitmap> showImages = new ArrayList<Bitmap>();
    private  ArrayList<Integer> idxShowImages = new ArrayList<Integer>();
    private final ArrayList<Bitmap> allClothe = new ArrayList<Bitmap>();
    private final ArrayList<String> thickClothes = new ArrayList<String>();
    private final ArrayList<String> lengthClothes = new ArrayList<String>();
    private final ArrayList<String> cateClothes = new ArrayList<String>();

    //SQLite
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Show Slide Menu
        slideMenu = findViewById(R.id.slideMenu);
        non_showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        slideMenu.bringToFront();

        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();
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

        //옷 추가 버튼
        btnAddClothe = findViewById(R.id.BtnAddClothe);
        btnAddClothe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), add_cloth_Activity.class);
                startActivity(intent);
                finish();
            }
        });

        //Read Data
        readData();

        //Spinner
        spinner();

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
     * @brief Read data to database
     * @detail Categorize read data
     */
    private void readData(){
        dbHelper = new DBHelper(MainActivity.this, "MyDB.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM closet";
        Cursor cursor = db.rawQuery(sql, null);
        byte[] blob;
        String cate, thick, length;
        Bitmap bitmap;
        while(cursor.moveToNext()){
            cate = cursor.getString(1);
            thick = cursor.getString(2);
            length = cursor.getString(3);
            blob = cursor.getBlob(4);
            bitmap = getImage(blob);
            allClothe.add(bitmap);
            thickClothes.add(thick);
            lengthClothes.add(length);
            cateClothes.add(cate);
        }

        cursor.close();
    }

    /**
     * @brief Spinner
     * @datail Show images that fit the selected category
     */
    private void spinner(){
        category = (Spinner)findViewById(R.id.category_cloth);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if((parent.getItemAtPosition(position)).equals("전체")){
                    getClassiLength("전체");
                }
                if((parent.getItemAtPosition(position)).equals("상의")){
                    getClassiLength("상의");
                }
                if((parent.getItemAtPosition(position)).equals("하의")){
                    getClassiLength("하의");
                }
                if((parent.getItemAtPosition(position)).equals("외투")){
                    getClassiLength("외투");
                }
                if((parent.getItemAtPosition(position)).equals("기타")){
                    getClassiLength("기타");
                }

                //Create GridView
                grid = findViewById(R.id.gridView);
                MyGridAdapter adapter = new MyGridAdapter(MainActivity.this);
                grid.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void getClassiLength(String classi){
        showImages.clear();
        if(classi.equals("전체")) {
            showImages.addAll(allClothe);
        }else {
            for(int i = 0; i < allClothe.size(); i++){
                if((cateClothes.get(i)).equals(classi)){
                    showImages.add(allClothe.get(i));
                }
            }
        }
    }

    /**
     * @brief To use GridView
     * @detail Assign the image into the grid view
     */
    class MyGridAdapter extends BaseAdapter{
        final Context context;

        MyGridAdapter(Context c){
            context = c;
        }

        @Override
        public int getCount(){
            return showImages.size();
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public Object getItem(int position) {
            return showImages.get(position);
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2){
            ImageView imageView = new ImageView(context);
            imageView.setPadding(0,0,0,0);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imageView.setImageBitmap(showImages.get(position));

            return imageView;
        }
    }

    // convert from byte array to bitmap

    /**
     * @brief Change byte array
     * @detail Conver from byte array to bitmap
     * @param image
     * byte array to change bitmap
     * @return BitmapFactory.decodeByteArray(image, 0, image.length)
     */
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


}

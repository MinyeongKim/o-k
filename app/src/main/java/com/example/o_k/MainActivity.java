package com.example.o_k;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    boolean isSlideOpen = false;
    boolean isImageMenu = false;
    Button btnAddClothe;
    Button btnMenu;
    Button closetMenu, weatherMenu, coordiMenu, settingMenu;
    LinearLayout slideMenu;
    Animation showMenu;
    Animation non_showMenu;
    Spinner category;
    String cate;
    GridView grid;

    //카테고리 별 이미지 저장소
    ArrayList<Bitmap> showImages = new ArrayList<Bitmap>();
    ArrayList<Bitmap> allClothe = new ArrayList<Bitmap>();
    ArrayList<Bitmap> cateTop = new ArrayList<Bitmap>();
    ArrayList<Bitmap> cateBottom = new ArrayList<Bitmap>();
    ArrayList<Bitmap> cateOuter = new ArrayList<Bitmap>();
    ArrayList<Bitmap> cateEct = new ArrayList<Bitmap>();
    //SQLite
    DBHelper dbHelper;

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
        coordiMenu = findViewById(R.id.coordi_menu);
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
        coordiMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), coordinate_show_Activity.class);
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
        dbHelper = new DBHelper(MainActivity.this, "MyDB.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM closet";
        Cursor cursor = db.rawQuery(sql, null);
        byte[] blob;
        String cate;
        Bitmap bitmap;
        while(cursor.moveToNext()){
            cate = cursor.getString(1);
            blob = cursor.getBlob(4);
            bitmap = getImage(blob);
            allClothe.add(bitmap);
            if(cate.equals("상의")){
                cateTop.add(bitmap);
            }
            else if (cate.equals("하의")){
                cateBottom.add(bitmap);
            }
            else if(cate.equals("외투")){
                cateOuter.add(bitmap);
            }
            else {
                cateEct.add(bitmap);
            }
        }


        //Spinner
        category = findViewById(R.id.category_cloth);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if((parent.getItemAtPosition(position)).equals("전체")){
                    showImages = allClothe;
                }
                if((parent.getItemAtPosition(position)).equals("상의")){
                    showImages = cateTop;
                }
                if((parent.getItemAtPosition(position)).equals("하의")){
                    showImages = cateBottom;
                }
                if((parent.getItemAtPosition(position)).equals("외투")){
                    showImages = cateOuter;
                }
                if((parent.getItemAtPosition(position)).equals("기타")){
                    showImages = cateEct;
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

    //Slide menu animation
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

    public class MyGridAdapter extends BaseAdapter{
        Context context;

        public MyGridAdapter(Context c){
            context = c;
        }

        @Override
        public int getCount(){
            return showImages.size();
        }

        @Override
        public long getItemId(int index){
            return showImages.indexOf(index);
        }

        @Override
        public Object getItem(int index){
            return showImages.indexOf(index);
        }

        @Override
        public View getView(int index, View arg1, ViewGroup arg2){
            ImageView imageView = new ImageView(context);
            imageView.setPadding(0,0,0,0);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imageView.setImageBitmap(showImages.get(index));

            grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    PopupMenu popup = new PopupMenu(MainActivity.this, view);

                    MenuInflater inflater = popup.getMenuInflater();
                    Menu menu = popup.getMenu();

                    //byte[] temp = parent.getItemAtPosition(position);
                    inflater.inflate(R.menu.image_reove_popupmenu, menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.removeImage:
                                    int checked = grid.getCheckedItemPosition();
                                    showImages.remove(checked);
                                    //String sql2 = "DETETE FROM closet WHERE image = " + ";";
                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            return imageView;
        }
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}

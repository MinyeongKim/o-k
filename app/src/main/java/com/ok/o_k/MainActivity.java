package com.ok.o_k;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private ArrayList<Integer> idxImages = new ArrayList<Integer>();
    private ArrayList<Integer> idxClothes = new ArrayList<Integer>();
    private ArrayList<String> classification = new ArrayList<String>();
    private final ArrayList<Bitmap> allClothe = new ArrayList<Bitmap>();

    private final ArrayList<Integer> cateTop = new ArrayList<Integer>();
    private final ArrayList<Integer> cateBottom = new ArrayList<Integer>();
    private final ArrayList<Integer> cateOuter = new ArrayList<Integer>();
    private final ArrayList<Integer> cateEct = new ArrayList<Integer>();

    /*
    private final ArrayList<Bitmap> cateTop = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateBottom = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateOuter = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateEct = new ArrayList<Bitmap>();
    */
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
    public void readData(){
        dbHelper = new DBHelper(MainActivity.this, "MyDB.db", null, 1);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM closet";
        Cursor cursor = db.rawQuery(sql, null);
        byte[] blob;
        int id;
        String cate;
        Bitmap bitmap;
        while(cursor.moveToNext()){
            id = cursor.getInt(0);
            cate = cursor.getString(1);
            blob = cursor.getBlob(4);
            bitmap = getImage(blob);
            idxClothes.add(id);
            classification.add(cate);
            allClothe.add(bitmap);

            if(cate.equals("상의")){
                cateTop.add(id);
            }
            else if (cate.equals("하의")){
                cateBottom.add(id);
            }
            else if(cate.equals("외투")){
                cateOuter.add(id);
            }
            else if(cate.equals("기타")){
                cateEct.add(id);
            }
        }

        cursor.close();
    }

    /**
     * @brief Spinner
     * @datail Show images that fit the selected category
     */
    public void spinner(){
        category = findViewById(R.id.category_cloth);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if((parent.getItemAtPosition(position)).equals("전체")){
                    idxImages = idxClothes;
                }
                if((parent.getItemAtPosition(position)).equals("상의")){
                    idxImages = cateTop;
                }
                if((parent.getItemAtPosition(position)).equals("하의")){
                    idxImages = cateBottom;
                }
                if((parent.getItemAtPosition(position)).equals("외투")){
                    idxImages = cateOuter;
                }
                if((parent.getItemAtPosition(position)).equals("기타")){
                    idxImages = cateEct;
                }

                //Create GridView
                grid = findViewById(R.id.gridView);
                MyGridAdapter adapter = new MyGridAdapter(MainActivity.this);
                grid.setAdapter(adapter);

                grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    int pos;
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        PopupMenu popup = new PopupMenu(MainActivity.this, view);

                        MenuInflater inflater = popup.getMenuInflater();
                        Menu menu = popup.getMenu();

                        //byte[] temp = parent.getItemAtPosition(position);
                        inflater.inflate(R.menu.image_reove_popupmenu, menu);
                        pos = position;
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.removeImage:
                                        int temp = idxImages.get(pos) - 1;

                                        return true;
                                }
                                return false;
                            }
                        });
                        popup.show();
                        return false;
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
            return idxImages.size();
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public Object getItem(int position) {
            return idxImages.get(position);
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2){
            ImageView imageView = new ImageView(context);
            imageView.setPadding(0,0,0,0);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            imageView.setImageBitmap(allClothe.get(idxImages.get(position) - 1));

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

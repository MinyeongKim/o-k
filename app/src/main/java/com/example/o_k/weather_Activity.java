package com.example.o_k;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class weather_Activity extends AppCompatActivity {
    private boolean isSlideOpen = false;
    private Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button coordiMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;
    private TextView txtLat;
    private TextView txtLon;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private static final String TAG = "imagesearchexample";
    private static final int LOAD_SUCCESS = 101;

    //API 사용
    long mNow;
    Date mDate;
    Date mTime;
    private String base_date;
    private String base_time;
    private String lat;
    private String lon;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hhmm");



    private final String data_url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData";
    private final String serviceKey = "?serviceKey=%2FYxsvH0O0av8Q7Fd7H7sW2yctGe4Oqfd4MWXhgUrqvlLAf%2FeKhdAaKinxbcKH1kcpebfDxp96jjuW4E8dSLdog%3D%3D";
    private String rest_Url;



    // GPSTracker class
    private GpsInfo gps;
    private ProgressDialog progressDialog;
    private TextView textviewJSONText;

    private Spinner category;
    private GridView grid;

    //카테고리 별 이미지 저장소
    private ArrayList<Bitmap> showImages = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> allClothe = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateTop = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateBottom = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateOuter = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateEct = new ArrayList<Bitmap>();
    //SQLite
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);

        //Show Slide Menu
        slideMenu = findViewById(R.id.slideMenu);
        non_showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        slideMenu.bringToFront();

        weather_Activity.SlidingPageAnimationListener animationListener = new weather_Activity.SlidingPageAnimationListener();
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
                Intent intent = new Intent(getApplicationContext(), api.class);
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


        /**
         * 여기부터
         * 코드 넣기
         * */

        Button btnShowLocation = (Button) findViewById(R.id.refresh);
        //txtLat = (TextView) findViewById(R.id.tv_latitude);
        //txtLon = (TextView) findViewById(R.id.tv_longitude);
        textviewJSONText = findViewById(R.id.weatherBox);


        //Read Data
        dbHelper = new DBHelper(weather_Activity.this, "MyDB.db", null, 1);
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

        cursor.close();

        //Spinner
        category = (Spinner)findViewById(R.id.category_cloth);
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
                weather_Activity.MyGridAdapter adapter = new weather_Activity.MyGridAdapter(weather_Activity.this);
                grid.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // GPS 정보를 보여주기 위한 이벤트 클래스 등록
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // 권한 요청을 해야 함
                if (!isPermission) {
                    callPermission();
                    return;
                }

                gps = new GpsInfo(weather_Activity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    int i_lat = (int)latitude;
                    int i_lon = (int)longitude;

                    //txtLat.setText(String.valueOf(latitude));
                    //txtLon.setText(String.valueOf(longitude));

                    lat = Integer.toString(i_lat);
                    lon = Integer.toString(i_lon);
                    Log.i("This is lat " ,lat + "lon " +lon);


                    progressDialog = new ProgressDialog(weather_Activity.this);
                    progressDialog.setMessage("Please wait.....");
                    progressDialog.show();

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, -45);


                    base_date = String.format("%d%02d%02d",cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
                    base_time = String.format("%02d%02d",cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                    //base_time ->  지금은 현재시간을 받아온다.


                    int cal_time = Integer.parseInt(base_time);
                    int yesterday =Integer.parseInt(base_date);
                    yesterday = yesterday - 1;
                    Log.i("Cal_time is ",  Integer.toString(cal_time));
                    //cal로 빼고 다시 int로 바꿀 것

                    if (cal_time < 500 && cal_time >= 200){
                        //현재시간 - 45분 < 0630이면 5시가 base_time
                        base_time = "0200";
                    } else if (cal_time < 800 && cal_time >= 500){
                        base_time= "0500";
                    } else if (cal_time < 1100 && cal_time >= 800){
                        base_time= "0800";
                    } else if (cal_time < 1400 && cal_time >= 1100){
                        base_time= "1100";
                    } else if (cal_time < 1700 && cal_time >= 1400){
                        base_time= "1400";
                    } else if (cal_time < 2000 && cal_time >= 1700){
                        base_time= "1700";
                    } else if (cal_time < 2300 && cal_time >= 2000){
                        base_time= "2000";
                    } else if (( cal_time >= 0 && cal_time < 200) || ( cal_time < 2359 && cal_time >= 2300)){
                        base_date = Integer.toString(yesterday);
                        base_time= "2300";
                    }   else {
                        Log.d("Base Time is","ERROR!");
                    }



                    rest_Url = data_url + serviceKey + "&base_date="+ base_date + "&base_time=" + base_time + "&nx=" + lat + "&ny=" + lon +
                            "&numOfRows=13" + "&pageNo=1" + "&_type=json";
                    //보낼 API주소 정해주기

                    getJSON(rest_Url);

                    Toast.makeText(
                            getApplicationContext(),
                            "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude + "\nAPI 사용 날짜 :" + base_date + "\nAPI 사용 시간 :" + base_time,
                            Toast.LENGTH_LONG).show();




                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });

        callPermission();  // 권한 요청을 해야 함



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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // GPS 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }



    private final MyHandler mHandler = new MyHandler(weather_Activity.this);


    private static class MyHandler extends Handler {
        private final WeakReference<weather_Activity> weakReference;

        MyHandler(weather_Activity weather_activity) {
            weakReference = new WeakReference<weather_Activity>(weather_activity);
        }

        @Override
        public void handleMessage(Message msg) {

            weather_Activity weather_Activity = weakReference.get();

            if (weather_Activity != null) {
                switch (msg.what) {

                    case LOAD_SUCCESS:
                        weather_Activity.progressDialog.dismiss();

                        String jsonString = (String) msg.obj;

                        weather_Activity.textviewJSONText.setText(jsonString);
                        break;
                }
            }
        }
    }


    private void getJSON(final String rest_Url) {

        Thread thread = new Thread(new Runnable() {

            public void run() {

                String result="";

                try {
                    Log.d(TAG, rest_Url);
                    URL url = new URL(rest_Url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();


                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();

                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    //JSONArray jsonArray = new JSONArray(jsonHtml.toString());
                    JSONObject obj = new JSONObject(sb.toString());//parser.parse(sb);

                    JSONObject parse_response = (JSONObject) obj.get("response");
                    // response 로 부터 body 찾아옵니다.
                    JSONObject parse_body = (JSONObject) parse_response.get("body");
                    //body 로 부터 items 받아옵니다.
                    JSONObject parse_items = (JSONObject) parse_body.get("items");
                    // items로 부터 itemlist 를 받아오기 itemlist : 뒤에 [ 로 시작하므로 jsonarray이다
                    JSONArray parse_item = (JSONArray) parse_items.get("item");

                    String category;
                    JSONObject weather;
                    // parse_item은 배열형태이기 때문에 하나씩 데이터를 하나씩 가져올때 사용합니다.
                    // 필요한 데이터만 가져오려고합니다.
                    Log.i("Parse_item.length is ",Integer.toString(parse_item.length()));
                    for (int i = 0; i < parse_item.length(); i++) {
                        weather = (JSONObject) parse_item.get(i);
                        double fcst_Value = ((Number) weather.get("fcstValue")).doubleValue();
                        category = (String) weather.get("category");
                        // 출력합니다.
                        Log.i("category : ", category);
                        Log.i(" fcst_Value : " , Double.toString(fcst_Value));

                        bufferedReader.close();
                        httpURLConnection.disconnect();

                        if(category.equals("POP")){
                            result += "강수확률 " + fcst_Value + "%\n";
                        }
                        else if (category.equals("T3H")){
                            result += "기온: " + fcst_Value + "℃\n";
                        } else if (category.equals("SKY")){
                            if (fcst_Value == 1) { result += "맑음\n";}
                            else if (fcst_Value == 3) {result += "구름 많음\n";}
                            else if (fcst_Value == 4) {result += "흐림\n";}
                            else {
                                result += "";
                            }
                        } else {
                            result += "";
                        }

                    }
                } catch (Exception e) {
                    result = e.toString();
                }


                Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
                mHandler.sendMessage(message);
            }

        });
        thread.start();
    }

    class MyGridAdapter extends BaseAdapter {
        final Context context;

        MyGridAdapter(Context c){
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

            return imageView;
        }
    }

    // convert from byte array to bitmap
    private static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}

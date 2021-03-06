/**
 * @file weather_Activity.java
 * @date 2019/05/28
 * @author Anyeseu Oh / Team O-K
 * @brief Weather page of our application
 */


package com.ok.o_k;

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
import java.util.ArrayList;
import java.util.Calendar;

public class weather_Activity extends AppCompatActivity {
    /**
     * @var String base_time
     * Time to send to api
     *
     * @var String base_date
     * Date to send to api / If the current date and the base_date used for api are different, subtract one day.
     *
     * @var String lat
     * Remove the decimal point of received latitude and send it to api.
     *
     * @var String lon
     * Remove the decimal point of received longitude and send it to api.
     *
     * @var data_url
     * Address of api to use
     *
     * @var seviceKey
     * Private api authentication key assigned to the team
     *
     * @var rest_Url
     * Full URL address to send to api
     *
     * @var TextView textviewJSONText
     * textView to show results received as api
     */

    private boolean isSlideOpen = false;
    private Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;
    private ImageView weather_icon;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;

    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private static final String TAG = "imagesearchexample";

    //API 사용
    private String base_date;
    private String base_time;
    private String lat;
    private String lon;

    private final String data_url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData";
    private final String serviceKey = "?serviceKey=%2FYxsvH0O0av8Q7Fd7H7sW2yctGe4Oqfd4MWXhgUrqvlLAf%2FeKhdAaKinxbcKH1kcpebfDxp96jjuW4E8dSLdog%3D%3D";
    private String rest_Url;

    // GPSTracker class
    private GpsInfo gps;
    private ProgressDialog progressDialog;
    private TextView textviewJSONText;

    private Spinner category;
    private static GridView grid;

    //카테고리 별 이미지 저장소
    private ArrayList<Bitmap> showImages = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> allClothe = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateTop = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateBottom = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateOuter = new ArrayList<Bitmap>();
    private final ArrayList<Bitmap> cateEct = new ArrayList<Bitmap>();
    private final ArrayList<String> thickClothes = new ArrayList<String>();
    private final ArrayList<String> lengthClothes = new ArrayList<String>();
    private final ArrayList<String> cateClothes = new ArrayList<String>();

    private double currentTemp = 9999;

    //SQLite
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * @brief Menu animation
         * @detail If you click menu_button, the menu is visible or invisible
         * @var boolean isSlide
         * menu flag visible = true, invisible = false
         */
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


        /**
         * 여기부터
         * 코드 넣기
         * */

        Button btnShowLocation = (Button) findViewById(R.id.refresh);
        textviewJSONText = findViewById(R.id.weatherBox);
        weather_icon = findViewById(R.id.weather_icon);

        //Read Data
        readData();


        // GPS 정보를 보여주기 위한 이벤트 클래스 등록
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                /**
                 * @brief
                 * request permission and receive the curent position
                 *
                 * @detail
                 * Press the refresh button to request permission or to receive the current position in gps.
                 *
                 * @var int cal_time
                 * Changed to int for time comparison calculation
                 */

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

                    lat = Integer.toString(i_lat);
                    lon = Integer.toString(i_lon);
                    Log.i("This is lat " ,lat + "lon " +lon);


                    progressDialog = new ProgressDialog(weather_Activity.this);
                    progressDialog.setMessage("Please wait.....");
                    progressDialog.show();

                    Calendar cal = Calendar.getInstance();
                    //cal.add(Calendar.MINUTE, -240);


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
                    } else if (cal_time < 2359 && cal_time >= 2300) {
                        base_time= "2300";
                    }
                    else if ( cal_time >= 0 && cal_time < 200){
                        base_date = Integer.toString(yesterday);
                        base_time= "2300";
                    } else {
                        Log.d("Base Time is","ERROR!");
                    }



                    rest_Url = data_url + serviceKey + "&base_date="+ base_date + "&base_time=" + base_time + "&nx=" + lat + "&ny=" + lon +
                            "&numOfRows=20" + "&pageNo=1" + "&_type=json";
                    //보낼 API주소 정해주기

                    getJSON(rest_Url);

                    Toast.makeText(
                            getApplicationContext(),
                            "현재 위치 - \n위도: " + latitude + "\n경도: " + longitude + "\nAPI 사용 날짜 :" + base_date + "\nAPI 사용 시간 :" + base_time,
                            Toast.LENGTH_LONG).show();


                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });

        //Spinner
        spinner();


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
        /**
         *  @brief Check the SDK version and whether the permission is already granted or not.
         */

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
            String jsonString;


            if (weather_Activity != null) {
                Log.i("SKY Value is ", String.valueOf(msg.what));
                if (msg.what == 1) {
                    weather_Activity.progressDialog.dismiss();

                    jsonString = (String) msg.obj;

                    weather_Activity.weather_icon.setImageResource(R.drawable.sunny);
                    weather_Activity.textviewJSONText.setText(jsonString);

                }

                if (msg.what == 3 || msg.what == 2) {
                    weather_Activity.progressDialog.dismiss();

                    jsonString = (String) msg.obj;

                    weather_Activity.weather_icon.setImageResource(R.drawable.cloud_sun);
                    weather_Activity.textviewJSONText.setText(jsonString);

                }

                if (msg.what == 4) {
                    weather_Activity.progressDialog.dismiss();

                    jsonString = (String) msg.obj;

                    weather_Activity.weather_icon.setImageResource(R.drawable.cloud);
                    weather_Activity.textviewJSONText.setText(jsonString);

                }

            }

        }
    }


    private void getJSON(final String rest_Url) {
        /**
         * @brief
         *  Receive the result through api.
         *
         * @detail
         * Connect to the Internet, and you get a line of results.
         * Afterwards, select the desired value from the data received and pass it over to textView to print it out.
         */


        Thread thread = new Thread(new Runnable() {

            public void run() {

                String result = "";
                int sky = 1;

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
                    JSONObject weather_2 = (JSONObject) parse_item.get(0);


                    if (weather_2.get("fcstTime") instanceof String) {

                        // parse_item은 배열형태이기 때문에 하나씩 데이터를 하나씩 가져올때 사용합니다.
                        // 필요한 데이터만 가져오려고합니다.
                        Log.i("Parse_item.length is ", Integer.toString(parse_item.length()));

                        Log.i("weather", "" + weather_2);

                        Log.i("weather1", "Check for error");

                        String temp = (String) weather_2.get("fcstTime");
                        //double temp = ((Number) weather_2.get("fcstTime")).doubleValue();
                        Log.i("weather2", "Check for error" + temp);

                        double fcst_Value;
                        String fcst_time; //Time 자체를 "" 상태로 받아오기 때문에 String 선언이 필요함.

                        for (int i = 0; i < parse_item.length(); i++) {

                            weather = (JSONObject) parse_item.get(i);
                            Log.i("weather", "" + weather);

                            fcst_time = (String) weather.get("fcstTime");
                            //double fcst_time = ((Number) weather.get("fcstTime")).doubleValue();
                            Log.i("weather 3", "Check for error");


                            fcst_Value = ((Number) weather.get("fcstValue")).doubleValue();
                            //double fcst_Value = ((Number) weather.get("fcstValue")).doubleValue();
                            Log.i("weather 4", "Check for error");


                            category = (String) weather.get("category");
                            Log.i("fcst_time is ", "" + fcst_time);
                            Log.i("first fcst_time is ", "" + temp);

                            if (!fcst_time.equals(temp)) {
                                break;
                            }

                            // 출력합니다.
                            Log.i("category : ", category);
                            Log.i("fcst_Value : ", Double.toString(fcst_Value));

                            bufferedReader.close();
                            httpURLConnection.disconnect();


                            if (category.equals("SKY")) {
                                if (fcst_Value == 1) {
                                    result += "맑음\n";
                                    sky = 1;
                                } else if (fcst_Value == 3 || fcst_Value == 2) {
                                    result += "구름 많음\n";
                                    sky = 3;
                                } else if (fcst_Value == 4) {
                                    result += "흐림\n";
                                    sky = 4;
                                } else {
                                    result += "";
                                }
                            } else if (category.equals("T3H")) {
                                currentTemp = fcst_Value;
                                result += "기온: " + fcst_Value + "℃\n";
                                Log.i("weather temperature", "Check for error\n" + result);
                                break;
                            } else if (category.equals("POP")) {
                                result += ("강수확률 " + fcst_Value + "%\n");
                                Log.i("weather rain", "Check for error\n" + result);
                            } else {
                                result += "";
                            }

                        }

                    }//if fcst_time 을 String으로 받아올 때 <- 확인해본 결과 시간마다 fsctTime 변수가 "1200" 일때도 있고 1200 일때도 있음.


                    else if ((weather_2.get("fcstTime") instanceof Integer)) {

                        // parse_item은 배열형태이기 때문에 하나씩 데이터를 하나씩 가져올때 사용합니다.
                        // 필요한 데이터만 가져오려고합니다.
                        Log.i("Parse_item.length is ", Integer.toString(parse_item.length()));

                        Log.i("weather", "" + weather_2);

                        Log.i("weather1", "Check for error");

                        double temp = ((Number) weather_2.get("fcstTime")).doubleValue();
                        //double temp = ((Number) weather_2.get("fcstTime")).doubleValue();
                        Log.i("weather2", "Check for error" + temp);

                        double fcst_Value;
                        double fcst_time; //Time 자체를 double 상태로 받아오기 때문에 double 선언이 필요함.

                        for (int i = 0; i < parse_item.length(); i++) {

                            weather = (JSONObject) parse_item.get(i);
                            Log.i("weather", "" + weather);

                            fcst_time = ((Number) weather.get("fcstTime")).doubleValue();
                            //double fcst_time = ((Number) weather.get("fcstTime")).doubleValue();
                            Log.i("weather 3", "Check for error");


                            fcst_Value = ((Number) weather.get("fcstValue")).doubleValue();
                            //double fcst_Value = ((Number) weather.get("fcstValue")).doubleValue();
                            Log.i("weather 4", "Check for error");


                            category = (String) weather.get("category");
                            Log.i("fcst_time is ", "" + fcst_time);
                            Log.i("first fcst_time is ", "" + temp);

                            if (fcst_time != temp) {
                                break;
                            }

                            // 출력합니다.
                            Log.i("category : ", category);
                            Log.i("fcst_Value : ", Double.toString(fcst_Value));

                            bufferedReader.close();
                            httpURLConnection.disconnect();


                            if (category.equals("SKY")) {
                                if (fcst_Value == 1) {
                                    result += "맑음\n";
                                    sky = 1;
                                } else if (fcst_Value == 3 || fcst_Value == 2) {
                                    result += "구름 많음\n";
                                    sky = 3;
                                } else if (fcst_Value == 4) {
                                    result += "흐림\n";
                                    sky = 4;
                                } else {
                                    result += "";
                                }
                            } else if (category.equals("T3H")) {
                                result += "기온: " + fcst_Value + "℃\n";
                                Log.i("weather temperature", "Check for error\n" + result);
                                break;
                            } else if (category.equals("POP")) {
                                result += ("강수확률 " + fcst_Value + "%\n");
                                Log.i("weather rain", "Check for error\n" + result);
                            } else {
                                result += "";
                            }


                        }



                    }

                    else{

                    }

                } catch (Exception e) {
                    result = e.toString();
                }

                Log.i("result is ", result);
                Message message = mHandler.obtainMessage(sky, result);
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
        /**
         *   @brief convert from byte array to bitmap
         */
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    private void spinner(){
        Log.i("Spinner start", "ready");
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
                Log.i("Create grid", "check");
                grid = findViewById(R.id.gridView);
                weather_Activity.MyGridAdapter adapter = new weather_Activity.MyGridAdapter(weather_Activity.this);
                grid.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String checkTemperature(Double temp){
        String checkTemp = "";
        if(temp == 9999){
            checkTemp = "noneData";
        }else if(temp >= 27){
            checkTemp = "veryHot";
        }else if(temp<27 && temp >=22){
            checkTemp = "hot";
        }else if(temp<22 && temp >=19){
            checkTemp = "littleHot";
        }else if(temp<19 && temp >=16){
            checkTemp = "mild";
        }else if(temp<16 && temp >=9){
            checkTemp = "littleCold";
        }else if(temp<9 && temp >=5){
            checkTemp = "cold";
        }else{
            checkTemp = "veryCold";
        }

        return checkTemp;
    }

    private void readData(){
        dbHelper = new DBHelper(weather_Activity.this, "MyDB.db", null, 1);
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
    }

    //현재 온도에 맞는 옷의 두께와 길이의 옷 리스트 반환
    private void getClassiLength(String classi){
        showImages.clear();
        String getTemp = checkTemperature(currentTemp);
        if(classi.equals("전체")) {
            if (getTemp.equals("noneData")){
            } else if(getTemp.equals("veryHot")) {
                for (int i = 0; i < allClothe.size(); i++) {
                    if ((thickClothes.get(i)).equals("얇음") && (lengthClothes.get(i).equals("짧다"))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            } else if (getTemp.equals("hot")) {
                for (int i = 0; i < allClothe.size(); i++) {
                    if ((thickClothes.get(i)).equals("얇음") && ((lengthClothes.get(i).equals("짧다")) || (lengthClothes.get(i).equals("중간/7부~9부")))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            } else if (getTemp.equals("littleHot")) {
                for (int i = 0; i < allClothe.size(); i++) {
                    if (((thickClothes.get(i)).equals("얇음") || (thickClothes.get(i)).equals("보통")) && ((lengthClothes.get(i).equals("짧다")) || (lengthClothes.get(i).equals("중간/7부~9부")))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            } else if (getTemp.equals("mild")) {
                for (int i = 0; i < allClothe.size(); i++) {
                    if (((thickClothes.get(i)).equals("얇음") || (thickClothes.get(i)).equals("보통")) && ((lengthClothes.get(i).equals("길다")) || (lengthClothes.get(i).equals("중간/7부~9부")))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            } else if (getTemp.equals("littleCold")) {
                for (int i = 0; i < allClothe.size(); i++) {
                    if (((thickClothes.get(i)).equals("두꺼움") || ((thickClothes.get(i)).equals("보통"))) && (lengthClothes.get(i).equals("길다"))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            } else {
                for (int i = 0; i < allClothe.size(); i++) {
                    if ((thickClothes.get(i)).equals("두꺼움") && (lengthClothes.get(i).equals("길다"))) {
                        showImages.add(allClothe.get(i));
                    }
                }
            }
        }else{
            if(getTemp.equals("noneData")){
            } else if(getTemp.equals("veryHot")){
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if((thickClothes.get(i)).equals("얇음") && (lengthClothes.get(i).equals("짧다"))){
                            showImages.add(allClothe.get(i));
                        }
                    }
                }
            }else if(getTemp.equals("hot")){
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if((thickClothes.get(i)).equals("얇음") && ((lengthClothes.get(i).equals("짧다")) || (lengthClothes.get(i).equals("중간/7부~9부")))){
                            showImages.add(allClothe.get(i));
                        }
                    }
                }
            }else if(getTemp.equals("littleHot")){
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if(((thickClothes.get(i)).equals("얇음") || (thickClothes.get(i)).equals("보통")) && ((lengthClothes.get(i).equals("짧다")) || (lengthClothes.get(i).equals("중간/7부~9부")))){
                            showImages.add(allClothe.get(i));
                        }
                    }
                }
            }else if(getTemp.equals("mild")){
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if(((thickClothes.get(i)).equals("얇음") || (thickClothes.get(i)).equals("보통")) && ((lengthClothes.get(i).equals("길다")) || (lengthClothes.get(i).equals("중간/7부~9부")))){
                            showImages.add(allClothe.get(i));
                        }
                    }
                }
            }else if(getTemp.equals("littleCold")){
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if(((thickClothes.get(i)).equals("두꺼움") || ((thickClothes.get(i)).equals("보통"))) && (lengthClothes.get(i).equals("길다"))){
                            showImages.add(allClothe.get(i));
                        }
                    }
                }
            }else{
                for (int i = 0; i < allClothe.size(); i++){
                    if((cateClothes.get(i)).equals(classi)){
                        if((thickClothes.get(i)).equals("두꺼움") && (lengthClothes.get(i).equals("길다"))){
                            showImages.add(allClothe.get(i));
                        }
                    }

                }
            }
        }
    }

}

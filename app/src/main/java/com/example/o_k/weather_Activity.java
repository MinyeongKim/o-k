package com.example.o_k;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class weather_Activity extends AppCompatActivity {
    boolean isSlideOpen = false;
    Button btnMenu;
    Button closetMenu, weatherMenu, coordiMenu, settingMenu;
    LinearLayout slideMenu;
    Animation showMenu;
    Animation non_showMenu;
    private Button btnShowLocation;
    private TextView txtLat;
    private TextView txtLon;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private static final String TAG = "imagesearchexample";
    public static final int LOAD_SUCCESS = 101;

    //API 사용
    long mNow;
    Date mDate;
    Date mTime;
    String base_date;
    String base_time;
    String lat;
    String lon;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hhmm");



    String data_url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData";
    String serviceKey = "?serviceKey=%2FYxsvH0O0av8Q7Fd7H7sW2yctGe4Oqfd4MWXhgUrqvlLAf%2FeKhdAaKinxbcKH1kcpebfDxp96jjuW4E8dSLdog%3D%3D";
    String rest_Url;



    // GPSTracker class
    private GpsInfo gps;
    private ProgressDialog progressDialog;
    private TextView textviewJSONText;

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

        btnShowLocation = (Button) findViewById(R.id.refresh);
        //txtLat = (TextView) findViewById(R.id.tv_latitude);
        //txtLon = (TextView) findViewById(R.id.tv_longitude);
        textviewJSONText = findViewById(R.id.weatherBox);




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

                    base_date = String.format("%d%02d%02d",cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
                    base_time = String.format("%02d00",cal.get(Calendar.HOUR_OF_DAY));

                    rest_Url = data_url + serviceKey + "&base_date="+ base_date + "&base_time=" + base_time + "&nx=" + lat + "&ny=" + lon + "&_type=json";
                    //보낼 API주소 정해주기

                    getJSON(rest_Url);

                    Toast.makeText(
                            getApplicationContext(),
                            "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude + "\n현재날짜 :" + base_date + "\n현재시간 :" + base_time,
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


    private String getDate() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return dateFormat.format(mDate);
    }

    private String getTime() {
        mNow = System.currentTimeMillis();
        mTime = new Date(mNow);
        return timeFormat.format(mTime);
    }


    private final MyHandler mHandler = new MyHandler(weather_Activity.this);


    private static class MyHandler extends Handler {
        private final WeakReference<weather_Activity> weakReference;

        public MyHandler(weather_Activity weather_activity) {
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


    public void getJSON(final String rest_Url) {

        Thread thread = new Thread(new Runnable() {

            public void run() {

                String result;

                try {

                    Log.d(TAG, rest_Url);
                    URL url = new URL(rest_Url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    //httpURLConnection.setDoOutput(true);
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


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();


                } catch (Exception e) {
                    result = e.toString();
                }


                Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
                mHandler.sendMessage(message);
            }

        });
        thread.start();
    }

    private Calendar getLastBaseTime(Calendar calBase) {

        int t = calBase.get(Calendar.HOUR_OF_DAY);

        if (t < 2) {

            calBase.add(Calendar.DATE, -1);

            calBase.set(Calendar.HOUR_OF_DAY, 23);

        } else {

            calBase.set(Calendar.HOUR_OF_DAY, t - (t + 1) % 3);

        }

        return calBase;

    }

}

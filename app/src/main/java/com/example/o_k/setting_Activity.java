/**
 * @file setting_Activity.java
 * @date 2019/05/05
 * @author Anyeseu Oh / Team O-K
 * @brief Setting menu of our application
 */


package com.example.o_k;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

public class setting_Activity extends AppCompatActivity {
    /**
     * @var NotificationManager notificationManager
     * Variable for push notificaiton
     *
     * @var PendingIntent intent
     * PendingIntent means the intent to display notification in the status notification window.
     */

    private boolean isSlideOpen = false;
    private Button btnMenu;
    private Button closetMenu;
    private Button weatherMenu;
    private Button coordiMenu;
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;
    private RadioGroup rg;

    private NotificationManager notificationManager;
    private PendingIntent intent ;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * @brief Menu animation
         * @detail If you click menu_button, the menu is visible or invisible
         * @var boolean isSlide
         * menu flag visible = true, invisible = false
         */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Show Slide Menu
        slideMenu = findViewById(R.id.slideMenu);
        non_showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        showMenu = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        slideMenu.bringToFront();

        setting_Activity.SlidingPageAnimationListener animationListener = new setting_Activity.SlidingPageAnimationListener();
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

        //Activity 이동
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

        intent = PendingIntent.getActivity(this, 0,

                new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);



        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.refresh) // 아이콘 설정하지 않으면 오류남
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("알림 제목") // 제목 설정
                .setContentText("알림 내용") // 내용 설정
                .setTicker("한줄 출력") // 상태바에 표시될 한줄 출력
                .setAutoCancel(true)
                .setContentIntent(intent);



        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());


/**
 Button sw = (Button)findViewById(R.id.clean_push);
 //스위치의 체크 이벤트를 위한 리스너 등록
 sw.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
// TODO Auto-generated method stub
//Toast.makeText(setting_Activity.this, "체크상태 = " + isChecked, Toast.LENGTH_SHORT).show();

//if (isChecked == true) {
intent = PendingIntent.getActivity(setting_Activity.this, 0,
new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

Notification.Builder builder = new Notification.Builder(setting_Activity.this)
.setSmallIcon(R.drawable.ic_launcher_background) // 아이콘 설정하지 않으면 오류남
.setDefaults(Notification.DEFAULT_ALL)
.setContentTitle("알림 제목") // 제목 설정
.setContentText("알림 내용") // 내용 설정
.setTicker("한줄 출력") // 상태바에 표시될 한줄 출력
.setAutoCancel(true)
.setContentIntent(intent);

notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
notificationManager.notify(0, builder.build());
Log.i("Is checked is true is ", "근데 왜 출력안돼 ㅡㅡ");
}

//  }

});

 */



        rg = (RadioGroup) findViewById(R.id.radGroupTheme);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                /**
                 * @brief onCheckedChanged() is changing the state of notification method
                 * @detail
                 * if checked ID is radBtnGreen -> change theme to Green
                 * if checked ID is radBtnLightGreen -> change theme to Light Green
                 * if checked ID is radBtnYellow -> change theme to Yellow
                 * if checked ID is radBtnLightPink -> change theme to Light Pink
                 * if checked ID is radBtnPink -> change theme to Pink
                 */
                switch (checkedId) {
                    case R.id.radBtnGreen:
                        Toast.makeText(getApplicationContext(), "현재 테마 : 초록색", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.radBtnLightGreen:
                        Toast.makeText(getApplicationContext(), "현재 테마 : 옅은 초록색", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radBtnYellow:
                        Toast.makeText(getApplicationContext(), "현재 테마 : 노란색", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.radBtnLightPink:
                        Toast.makeText(getApplicationContext(), "현재 테마 : 연한 분홍색", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.radBtnPink:
                        Toast.makeText(getApplicationContext(), "현재 테마 : 분홍색", Toast.LENGTH_SHORT).show();
                        break;
                }
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
}
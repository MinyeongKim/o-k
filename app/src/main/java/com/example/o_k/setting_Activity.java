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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.o_k.service.JobSchedulerStart;

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
    private Button settingMenu;
    private LinearLayout slideMenu;
    private Animation showMenu;
    private Animation non_showMenu;
    private RadioGroup rg;
    private SwitchCompat alarm;


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
          여기에쓰세용
         * **/

        alarm = (SwitchCompat)findViewById(R.id.clean_push);
        final SharedPreferences sh_Pref = getSharedPreferences("Time", Context.MODE_PRIVATE);
        if(sh_Pref != null && sh_Pref.getInt("state", 0) == 1) {
            alarm.setChecked(true);
        }

        alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton button, boolean isChecked){
                final SharedPreferences sh_Pref = getSharedPreferences("Time", Context.MODE_PRIVATE);
                final SharedPreferences.Editor toEdit = sh_Pref.edit();
                if (isChecked){ //Switch button ON
                    Toast.makeText(getApplicationContext(), "알람 설정 켜짐", Toast.LENGTH_SHORT).show();
                    toEdit.putInt("state", 1);
                    if(sh_Pref.getInt("alert", 0) == 0){
                        JobSchedulerStart.start(getApplicationContext());
                    }
                }
                else { //Switch button OFF
                    Toast.makeText(getApplicationContext(), "알람 설정 꺼짐", Toast.LENGTH_SHORT).show();
                    toEdit.remove("state");
                    if(sh_Pref.getInt("alert", 1) == 1){
                        JobSchedulerStart.destroy();
                    }
                }
                toEdit.apply();
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
package com.example.o_k;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

public class setting_Activity extends AppCompatActivity {
    boolean isSlideOpen = false;
    Button btnMenu;
    Button closetMenu, weatherMenu, coordiMenu, settingMenu;
    LinearLayout slideMenu;
    Animation showMenu;
    Animation non_showMenu;
    RadioGroup rg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        rg = (RadioGroup) findViewById(R.id.radGroupTheme);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
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
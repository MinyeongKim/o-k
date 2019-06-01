package com.ok.o_k;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
    /**
     * @file SplashActivity.java
     * @date 2019/05/02
     * @author Anyeseu Oh / Team O-K
     * @brief Show Loading Screen of our application before implement
     */

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        startActivity(new Intent(this,MainActivity.class));
        finish();

    }
}

package com.example.o_k;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    Spinner category;
    Button btnMenu;
    GridView grid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //슬라이드 메뉴 버튼
        btnMenu = findViewById(R.id.btnMenu);

        //Grid View
        //Create custom adapter


        //Spinner
        category = findViewById(R.id.category_cloth);
        grid = findViewById(R.id.gellery)
        category.setOnItemClickListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
    }
}

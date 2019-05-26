package com.example.o_k;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    Spinner category;
    Button btnMenu, btnAddClothe;
    GridView grid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //슬라이드 메뉴 버튼

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //옷 추가 버튼
        btnAddClothe = findViewById(R.id.BtnAddClothe);
        btnAddClothe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), add_cloth_Activity.class);
                startActivity(intent);
            }
        });

        //Grid View
        //Create custom adapter


        //Spinner
        /*
        category = findViewById(R.id.category_cloth);
        grid = findViewById(R.id.gellery)
        category.setOnItemClickListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        });
        */
    }
}

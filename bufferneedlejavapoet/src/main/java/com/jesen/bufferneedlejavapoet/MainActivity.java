package com.jesen.bufferneedlejavapoet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jesen.needle.annotation_javapoet.JBindView;
import com.jesen.needle.annotation_javapoet.JOnClick;
import com.jesen.needle_library_javapoet.JButterNeedle;

public class MainActivity extends AppCompatActivity {

    @JBindView(R.id.tv)
    Button tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JButterNeedle.bind(this);
    }

    @JOnClick(R.id.tv)
    public void clickit(View view){
        Toast.makeText(this,tv.getText(),Toast.LENGTH_SHORT).show();
    }
}
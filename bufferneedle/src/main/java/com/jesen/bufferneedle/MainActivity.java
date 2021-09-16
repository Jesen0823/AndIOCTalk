package com.jesen.bufferneedle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jesen.needle.annotation.BindView;
import com.jesen.needle.annotation.OnClick;
import com.jesen.needle_library.ButterNeedle;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 接口 = 接口实现类，调用接口bind方法
        ButterNeedle.bind(this);
        // ButterKnife.bind(this) = new MainActivity$ViewBinder().bind(this);

    }

    @OnClick(R.id.tv1)
    public void click(View view) {
        Toast.makeText(this,tv1.getText(),Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tv2)
    public void click2() {
        Toast.makeText(this,tv2.getText(),Toast.LENGTH_SHORT).show();
    }
}
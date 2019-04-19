package com.example.walle.testinput;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

/**
 * 测试填空项
 * Created by bobo on 2019-04-18.
 */
public class MainActivity extends AppCompatActivity {

    TextWordLayout textInput;
    TextWordLayout numInput;
    TextView tvTextInput;
    TextView tvNumInput;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //文本输入
        textInput = findViewById(R.id.twl_text);
        tvTextInput = findViewById(R.id.tv_text_input);
        //数字输入
        numInput = findViewById(R.id.twl_num);
        tvNumInput = findViewById(R.id.tv_num_input);
        //按钮
        btn = findViewById(R.id.btn);

        //输入监听
        textInput.setTextChangeListener(new TextWordLayout.textChangeListener() {
            @Override
            public void onChange(String pwd) {
                tvTextInput.setText("输入内容：" + pwd);
            }

            @Override
            public void onNull() {
                tvTextInput.setText("");
            }

            @Override
            public void onFinished(String pwd) {
                tvTextInput.setText("输入内容：" + pwd);
            }
        });

        numInput.setTextChangeListener(new TextWordLayout.textChangeListener() {
            @Override
            public void onChange(String pwd) {
                tvNumInput.setText("输入内容：" + pwd);
            }

            @Override
            public void onNull() {
                tvNumInput.setText("");
            }

            @Override
            public void onFinished(String pwd) {
                tvNumInput.setText("输入内容：" + pwd);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int i = random.nextInt(10) + 1;
                textInput.setMaxLength(i);
            }
        });
    }
}

package com.example.giftyfy.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giftyfy.MainActivity;
import com.example.giftyfy.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPw;
    private Button btnLogin;
    private TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etId = findViewById(R.id.et_email);      // 아이디
        etPw = findViewById(R.id.et_password);  // 비밀번호
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);

        // ✅ 로그인 버튼 → 바로 메인 화면 이동
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();   // 뒤로가기 눌러도 로그인으로 안 돌아오게
        });

        // ✅ 회원가입 버튼 → 회원가입 화면 이동
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
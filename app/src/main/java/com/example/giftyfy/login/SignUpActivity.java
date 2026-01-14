package com.example.giftyfy.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private EditText etId, etPw, etPwConfirm, etName;
    private TextView tvPwCheck;
    private Spinner spYear, spMonth, spDay;
    private Button btnSignUp;
    private ImageButton btnBack;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        etId = findViewById(R.id.et_id);
        etPw = findViewById(R.id.et_pw);
        etPwConfirm = findViewById(R.id.et_pw_confirm);
        etName = findViewById(R.id.et_name);
        tvPwCheck = findViewById(R.id.tv_pw_check);
        spYear = findViewById(R.id.sp_year);
        spMonth = findViewById(R.id.sp_month);
        spDay = findViewById(R.id.sp_day);
        btnSignUp = findViewById(R.id.btn_signup);
        btnBack = findViewById(R.id.btn_back);

        setupSpinners();
        setupPwCheck();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSignUp.setOnClickListener(v -> performSignUp());
    }

    private void setupSpinners() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int i = currentYear; i >= currentYear - 100; i--) years.add(String.valueOf(i));
        
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(String.format("%02d", i));
        
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add(String.format("%02d", i));

        spYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));
        spDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));
    }

    private void setupPwCheck() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String p1 = etPw.getText().toString();
                String p2 = etPwConfirm.getText().toString();
                if (!p1.isEmpty() && p1.equals(p2)) {
                    tvPwCheck.setVisibility(View.VISIBLE);
                } else {
                    tvPwCheck.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etPw.addTextChangedListener(watcher);
        etPwConfirm.addTextChangedListener(watcher);
    }

    private void performSignUp() {
        String email = etId.getText().toString().trim();
        String pw = etPw.getText().toString().trim();
        String pw2 = etPwConfirm.getText().toString().trim();
        String name = etName.getText().toString().trim();

        if (email.isEmpty() || pw.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pw.equals(pw2)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String birthday = spYear.getSelectedItem() + "-" + spMonth.getSelectedItem() + "-" + spDay.getSelectedItem();

        auth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = email.split("@")[0];
                            FirebaseManager.getInstance().saveMyProfile(userId, name, birthday, new ArrayList<>());
                            Toast.makeText(SignUpActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

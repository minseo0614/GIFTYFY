package com.example.giftyfy.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giftyfy.FirebaseManager;
import com.example.giftyfy.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private EditText etId, etName, etPw, etPwConfirm;
    private TextView tvPwCheck;
    private Spinner spYear, spMonth, spDay;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        etId = findViewById(R.id.et_id);
        etName = findViewById(R.id.et_name);
        etPw = findViewById(R.id.et_pw);
        etPwConfirm = findViewById(R.id.et_pw_confirm);
        tvPwCheck = findViewById(R.id.tv_pw_check);

        spYear = findViewById(R.id.sp_year);
        spMonth = findViewById(R.id.sp_month);
        spDay = findViewById(R.id.sp_day);

        setupBirthSpinners();
        setupPwMatchCheck();

        Button btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(v -> performSignUp());
    }

    private void performSignUp() {
        String userId = etId.getText().toString().trim(); // 사용자가 입력한 아이디
        String name = etName.getText().toString().trim();
        String password = etPw.getText().toString().trim();
        String passwordConfirm = etPwConfirm.getText().toString().trim();

        if (userId.isEmpty() || name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ 가상 이메일 생성
        String fakeEmail = userId + "@giftify.com";

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 생일 문자열 생성
                        String birthday = spYear.getSelectedItem().toString() + "-" +
                                String.format("%02d", spMonth.getSelectedItemPosition()) + "-" +
                                String.format("%02d", spDay.getSelectedItemPosition());

                        // ✅ [수정된 부분] userId를 첫 번째 인자로 추가하여 4개의 인자를 전달합니다.
                        FirebaseManager.getInstance().saveMyProfile(userId, name, birthday, new ArrayList<String>());

                        Toast.makeText(SignUpActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "회원가입 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupBirthSpinners() {
        List<String> years = new ArrayList<>();
        years.add("년도");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1950; y--) years.add(String.valueOf(y));
        spYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));

        List<String> months = new ArrayList<>();
        months.add("월");
        for (int m = 1; m <= 12; m++) months.add(String.valueOf(m));
        spMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        List<String> days = new ArrayList<>();
        days.add("일");
        for (int d = 1; d <= 31; d++) days.add(String.valueOf(d));
        spDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));
    }

    private void setupPwMatchCheck() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String pw = etPw.getText().toString();
                String pw2 = etPwConfirm.getText().toString();
                boolean match = !pw.isEmpty() && pw.equals(pw2);
                tvPwCheck.setVisibility(match ? View.VISIBLE : View.GONE);
            }
        };
        etPw.addTextChangedListener(watcher);
        etPwConfirm.addTextChangedListener(watcher);
    }
}

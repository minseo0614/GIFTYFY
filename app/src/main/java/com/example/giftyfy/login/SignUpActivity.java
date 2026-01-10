package com.example.giftyfy.login;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.giftyfy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private EditText etPw, etPwConfirm;
    private TextView tvPwCheck;          // ✅ ImageView 말고 TextView(✓)

    private Spinner spYear, spMonth, spDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText etId = findViewById(R.id.et_id);
        EditText etName = findViewById(R.id.et_name);

        // (선택) 아이디/이름 눌렀을 때 키보드 강제
        etId.setOnClickListener(v -> showKeyboard(etId));
        etName.setOnClickListener(v -> showKeyboard(etName));

        etPw = findViewById(R.id.et_pw);
        etPwConfirm = findViewById(R.id.et_pw_confirm);

        tvPwCheck = findViewById(R.id.tv_pw_check);  // ✅ 이게 핵심!

        spYear = findViewById(R.id.sp_year);
        spMonth = findViewById(R.id.sp_month);
        spDay = findViewById(R.id.sp_day);

        setupBirthSpinners();
        setupPwMatchCheck();

        Button btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void showKeyboard(EditText et) {
        et.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setupBirthSpinners() {
        // ===== 년도 =====
        List<String> years = new ArrayList<>();
        years.add("년도");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear; y >= 1950; y--) years.add(String.valueOf(y));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, years
        );
        spYear.setAdapter(yearAdapter);
        spYear.setSelection(0);

        // ===== 월 =====
        List<String> months = new ArrayList<>();
        months.add("월");
        for (int m = 1; m <= 12; m++) months.add(String.valueOf(m));
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, months
        );
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(0);

        // ===== 일 =====
        List<String> days = new ArrayList<>();
        days.add("일");
        for (int d = 1; d <= 31; d++) days.add(String.valueOf(d));
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, days
        );
        spDay.setAdapter(dayAdapter);
        spDay.setSelection(0);
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

                // ✅ 일치하면 ✓ 보여주기
                tvPwCheck.setVisibility(match ? View.VISIBLE : View.GONE);
            }
        };

        etPw.addTextChangedListener(watcher);
        etPwConfirm.addTextChangedListener(watcher);
    }
}
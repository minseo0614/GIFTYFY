package com.example.giftyfy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.giftyfy.friend.Friend;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPageFragment extends Fragment {

    private TextView tvMonthTitle;
    private RecyclerView rvCalendar;
    private RecyclerView rvReceivedGifts;
    private Calendar selectedDate;
    private ChipGroup cgMyInterests;
    private Button btnAddInterest;

    private final String[] availableTags = {
            "디저트러버", "애주가", "상품권애호가", "카페돌이", "고기진심러",
            "빵지순례자", "편의점단골", "배달앱VIP", "집순이", "집돌이",
            "향기컬렉터", "캠핑매니아", "프로직장인", "운동매니아", "영양제신봉자",
            "피부관리진심러", "다이어터", "귀여운게최고", "댕냥이집사", "독서가",
            "게임덕후", "보드게이머", "러닝크루", "요리꿈나무", "패션피플"
    };

    public MyPageFragment() {
        super(R.layout.fragment_mypage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = Calendar.getInstance();
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvReceivedGifts = view.findViewById(R.id.rvReceivedGifts);

        // 월 이동 버튼 설정
        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        updateCalendar();
        setupReceivedGifts();

        // 취향 태그 설정
        cgMyInterests = view.findViewById(R.id.cgMyInterests);
        btnAddInterest = view.findViewById(R.id.btnAddInterest);
        btnAddInterest.setOnClickListener(v -> showTagSelectDialog());
    }

    private void setupReceivedGifts() {
        // 가로 스크롤 설정
        rvReceivedGifts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        // 가상 데이터 생성 (팀원이 만든 Product 클래스 사용)
        List<Product> dummyProducts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Product p = new Product();
            p.id = i;
            p.title = "받은 선물 " + i;
            p.price = 20000 + (i * 5000);
            p.category = "선물";
            dummyProducts.add(p);
        }

        // 팀원이 만든 ProductAdapter 사용
        ProductAdapter adapter = new ProductAdapter(dummyProducts);
        rvReceivedGifts.setAdapter(adapter);
    }

    private void showTagSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("나의 취향 키워드 선택");
        builder.setItems(availableTags, (dialog, which) -> {
            String selectedTag = availableTags[which];
            addTagToGroup(selectedTag);
        });
        builder.show();
    }

    private void addTagToGroup(String tag) {
        for (int i = 0; i < cgMyInterests.getChildCount(); i++) {
            Chip chip = (Chip) cgMyInterests.getChildAt(i);
            if (chip.getText().toString().equals("#" + tag)) return;
        }

        Chip chip = new Chip(getContext());
        chip.setText("#" + tag);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(android.R.color.white);
        chip.setChipStrokeColorResource(R.color.black);
        chip.setChipStrokeWidth(1f);
        chip.setOnCloseIconClickListener(v -> cgMyInterests.removeView(chip));
        cgMyInterests.addView(chip);
    }

    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월", Locale.KOREA);
        tvMonthTitle.setText(sdf.format(selectedDate.getTime()));

        List<String> daysInMonth = generateDaysInMonth(selectedDate);
        List<Friend> dummyFriends = getDummyFriends();

        CalendarAdapter adapter = new CalendarAdapter(daysInMonth, dummyFriends, (Calendar) selectedDate.clone());
        rvCalendar.setAdapter(adapter);
    }

    private List<String> generateDaysInMonth(Calendar calendar) {
        List<String> dayList = new ArrayList<>();
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            dayList.add("");
        }

        for (int i = 1; i <= lastDayOfMonth; i++) {
            dayList.add(String.valueOf(i));
        }

        return dayList;
    }

    private List<Friend> getDummyFriends() {
        List<Friend> friends = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            friends.add(new Friend("친구" + i, "01-" + (i % 28 + 1), "미설정", Arrays.asList("태그")));
        }
        return friends;
    }
}

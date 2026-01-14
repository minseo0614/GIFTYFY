package com.example.giftyfy;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.giftyfy.friend.Friend;
import com.example.giftyfy.login.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyPageFragment extends Fragment {

    private TextView tvMonthTitle;
    private TextView tvMyInterestsTitle;
    private TextView tvBirthdayNoticeLabel; 
    private TextView tvNoBirthdayFriend;    
    private ChipGroup cgBirthdayFriends;
    private RecyclerView rvCalendar;
    private RecyclerView rvReceivedGifts;
    private RecyclerView rvWishlist;
    private Calendar selectedDate;

    private ChipGroup cgMyInterests;
    private Button btnAddInterest;
    private TextView btnAddAnniversary;
    private Button btnLogout;
    private View layoutEmptyWishlist; 

    private final String[] availableTags = {
            "디저트러버", "애주가", "상품권애호가", "카페돌이", "고기진심러",
            "빵지순례자", "편의점단골", "배달앱VIP", "집순이", "집돌이",
            "향기컬렉터", "캠핑매니아", "프로직장인", "운동매니아", "영양제신봉자",
            "피부관리진심러", "다이어터", "귀여운게최고", "댕냥이집사", "독서가",
            "게임덕후", "보드게이머", "요리꿈나무", "패션피플", "주얼리수집가", "문구가좋아"
    };

    private String myName = "";
    private String myBirthday = "";
    private List<String> myInterests = new ArrayList<>();
    private List<Friend> realFriends = new ArrayList<>();

    private ReceivedGiftAdapter receivedAdapter; 
    private ProductAdapter wishAdapter;

    public MyPageFragment() {
        super(R.layout.fragment_mypage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = Calendar.getInstance();

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvMyInterestsTitle = view.findViewById(R.id.tvMyInterestsTitle);

        tvBirthdayNoticeLabel = view.findViewById(R.id.tvBirthdayNoticeLabel);
        tvNoBirthdayFriend = view.findViewById(R.id.tvNoBirthdayFriend);
        cgBirthdayFriends = view.findViewById(R.id.cgBirthdayFriends);
        
        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvReceivedGifts = view.findViewById(R.id.rvReceivedGifts);
        rvWishlist = view.findViewById(R.id.rvWishlist);
        layoutEmptyWishlist = view.findViewById(R.id.layoutEmptyWishlist);
        cgMyInterests = view.findViewById(R.id.cgMyInterests);
        btnAddInterest = view.findViewById(R.id.btnAddInterest);
        btnAddAnniversary = view.findViewById(R.id.btnAddAnniversary);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupRecyclerViews();
        loadMyProfileFromServer();
        loadRealFriendsFromServer();
        setupCalendarButtons(view);

        btnAddInterest.setOnClickListener(v -> showTagSelectBottomSheet());
        if (btnAddAnniversary != null) {
            btnAddAnniversary.setOnClickListener(v -> showAddAnniversaryDialog());
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupRecyclerViews() {
        rvReceivedGifts.setLayoutManager(new LinearLayoutManager(getContext()));
        receivedAdapter = new ReceivedGiftAdapter();
        rvReceivedGifts.setAdapter(receivedAdapter);

        rvWishlist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        wishAdapter = new ProductAdapter();
        rvWishlist.setAdapter(wishAdapter);
    }

    private void showAddAnniversaryDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_anniversary, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        Spinner spPerson = dialogView.findViewById(R.id.spPerson);
        List<String> friendNames = new ArrayList<>();
        friendNames.add("없음");
        for (Friend f : realFriends) friendNames.add(f.getName());
        spPerson.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, friendNames));

        Spinner spMonth = dialogView.findViewById(R.id.spMonth);
        Spinner spDay = dialogView.findViewById(R.id.spDay);
        List<String> months = new ArrayList<>(); for (int i = 1; i <= 12; i++) months.add(String.valueOf(i));
        spMonth.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, months));
        List<String> days = new ArrayList<>(); for (int i = 1; i <= 31; i++) days.add(String.valueOf(i));
        spDay.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, days));

        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            EditText etTitle = dialogView.findViewById(R.id.etAnniversaryTitle);
            if (etTitle.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "기념일 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "기념일이 추가되었습니다!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadMyProfileFromServer() {
        FirebaseManager.getInstance().listenToMyProfile(data -> {
            if (data != null && isAdded()) {
                myName = (String) data.get("name");
                myBirthday = (String) data.get("birthday");
                
                if (myName != null && !myName.isEmpty()) {
                    tvMyInterestsTitle.setText(myName + "님의 취향 키워드");
                }

                List<String> loadedInterests = (List<String>) data.get("interests");
                if (loadedInterests != null) {
                    myInterests = new ArrayList<>(loadedInterests);
                    updateInterestsUI();
                }

                List<String> wishIds = (List<String>) data.get("wishlist");
                if (wishIds == null) wishIds = new ArrayList<>();

                if (wishAdapter != null) wishAdapter.setWishlistIds(wishIds);

                if (wishIds.isEmpty()) {
                    rvWishlist.setVisibility(View.GONE);
                    if (layoutEmptyWishlist != null) layoutEmptyWishlist.setVisibility(View.VISIBLE);
                } else {
                    rvWishlist.setVisibility(View.VISIBLE);
                    if (layoutEmptyWishlist != null) layoutEmptyWishlist.setVisibility(View.GONE);
                    FirebaseManager.getInstance().getProductsByIds(wishIds, products -> {
                        if (isAdded()) wishAdapter.setItems(products);
                    });
                }

                List<String> receivedIds = (List<String>) data.get("receivedGifts");
                if (receivedIds != null && !receivedIds.isEmpty()) {
                    List<ReceivedGift> giftItems = new ArrayList<>();
                    for (String id : receivedIds) {
                        giftItems.add(new ReceivedGift(id, "민서", "2025.12.25"));
                    }
                    if (isAdded()) receivedAdapter.setItems(giftItems);
                }
            }
        });
    }

    private void loadRealFriendsFromServer() {
        FirebaseManager.getInstance().fetchAllUsersAsFriends(new FirebaseManager.OnFriendsLoadedListener() {
            @Override
            public void onLoaded(List<Friend> friends) {
                if (friends != null && isAdded()) {
                    realFriends = friends;
                    updateCalendar();
                    updateBirthdayNotice(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                }
            }
        });
    }

    private void updateBirthdayNotice(int day) {
        if (cgBirthdayFriends == null || tvBirthdayNoticeLabel == null || tvNoBirthdayFriend == null) return;
        cgBirthdayFriends.removeAllViews();

        Calendar todayCal = Calendar.getInstance();
        boolean isToday = (selectedDate.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                           selectedDate.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH) &&
                           day == todayCal.get(Calendar.DAY_OF_MONTH));

        String datePrefix = isToday ? "오늘" : day + "일";

        if (isToday) {
            tvBirthdayNoticeLabel.setText("오늘 생일인 친구");
        } else {
            tvBirthdayNoticeLabel.setText(day + "일 생일인 친구");
        }

        tvNoBirthdayFriend.setText(datePrefix + " 생일인 친구가 없습니다!");

        String mmdd = String.format(Locale.KOREA, "%02d-%02d", selectedDate.get(Calendar.MONTH) + 1, day);

        boolean found = false;
        for (Friend f : realFriends) {
            if (f.getBirthday() != null && f.getBirthday().contains(mmdd)) {
                addBirthdayFriendChip(f);
                found = true;
            }
        }

        if (found) {
            tvBirthdayNoticeLabel.setVisibility(View.VISIBLE);
            tvNoBirthdayFriend.setVisibility(View.GONE);
        } else {
            tvBirthdayNoticeLabel.setVisibility(View.GONE);
            tvNoBirthdayFriend.setVisibility(View.VISIBLE);
        }
    }

    private void addBirthdayFriendChip(Friend friend) {
        Chip chip = new Chip(getContext());
        chip.setText(friend.getName());
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FBF7F9")));
        chip.setTextColor(ColorStateList.valueOf(Color.BLACK));
        chip.setChipStrokeWidth(0f);
        chip.setChipCornerRadius(999f);
        chip.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onFriendGiftClick(friend.getId(), friend.getName());
            }
        });
        cgBirthdayFriends.addView(chip);
    }

    private void updateInterestsUI() {
        cgMyInterests.removeAllViews();
        for (String tag : myInterests) {
            addTagChipToUI(tag);
        }
    }

    private void addTagChipToUI(String tag) {
        Chip chip = new Chip(getContext());
        chip.setText("#" + tag); 
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D080B6"))); 
        chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
        chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
        
        chip.setChipStrokeWidth(0f);
        chip.setChipCornerRadius(999f);

        chip.setOnCloseIconClickListener(v -> {
            myInterests.remove(tag);
            saveProfileToServer();
            cgMyInterests.removeView(chip);
        });
        cgMyInterests.addView(chip);
    }

    private void showTagSelectBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_tag_selector, null);
        dialog.setContentView(view);

        View btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        ChipGroup cg = view.findViewById(R.id.cgAvailableTags);
        List<String> tempSelected = new ArrayList<>(myInterests);

        for (String tag : availableTags) {
            Chip chip = new Chip(getContext());
            chip.setText(tag); 
            chip.setCheckable(true);
            chip.setChecked(tempSelected.contains(tag));
            
            updateSelectableChipStyle(chip, chip.isChecked());

            chip.setOnCheckedChangeListener((v, isChecked) -> {
                if (isChecked) {
                    if (!tempSelected.contains(tag)) tempSelected.add(tag);
                } else {
                    tempSelected.remove(tag);
                }
                updateSelectableChipStyle(chip, isChecked);
            });
            cg.addView(chip);
        }

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            myInterests = new ArrayList<>(tempSelected);
            saveProfileToServer();
            updateInterestsUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateSelectableChipStyle(Chip chip, boolean isChecked) {
        if (isChecked) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D080B6")));
            chip.setTextColor(Color.WHITE);
            chip.setChipStrokeWidth(0f);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FBF7F9")));
            chip.setTextColor(Color.parseColor("#333333"));
            chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            chip.setChipStrokeWidth(1f * getResources().getDisplayMetrics().density);
        }
    }

    private void saveProfileToServer() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String userId = email.split("@")[0];
        FirebaseManager.getInstance().saveMyProfile(userId, myName, myBirthday, myInterests);
    }

    private void setupCalendarButtons(View view) {
        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            selectedDate.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void updateCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월", Locale.KOREA);
        tvMonthTitle.setText(sdf.format(selectedDate.getTime()));
        List<String> daysInMonth = generateDaysInMonth(selectedDate);
        
        CalendarAdapter adapter = new CalendarAdapter(daysInMonth, realFriends, (Calendar) selectedDate.clone(), (day, birthdayNames) -> {
            updateBirthdayNotice(day);
        });
        rvCalendar.setAdapter(adapter);
    }

    private List<String> generateDaysInMonth(Calendar calendar) {
        List<String> dayList = new ArrayList<>();
        Calendar cal = (Calendar) calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < firstDayOfWeek; i++) dayList.add("");
        for (int i = 1; i <= lastDay; i++) dayList.add(String.valueOf(i));
        return dayList;
    }
}

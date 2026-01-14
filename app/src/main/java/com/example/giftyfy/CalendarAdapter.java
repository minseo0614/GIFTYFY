package com.example.giftyfy;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.giftyfy.friend.Friend;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(int day, String birthdayNames);
    }

    private final List<String> daysOfMonth;
    private final List<Friend> friends;
    private final Calendar calendar;
    private final Calendar today;
    private OnDateClickListener dateClickListener;

    public CalendarAdapter(List<String> daysOfMonth, List<Friend> friends, Calendar calendar, OnDateClickListener listener) {
        this.daysOfMonth = daysOfMonth;
        this.friends = friends;
        this.calendar = calendar;
        this.today = Calendar.getInstance();
        this.dateClickListener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String dayText = daysOfMonth.get(position);
        holder.tvDay.setText(dayText);

        holder.viewTodayBg.setVisibility(View.GONE);
        holder.viewDot.setVisibility(View.GONE);
        holder.tvDay.setTextColor(Color.parseColor("#333333"));

        if (dayText.isEmpty()) return;

        int day = Integer.parseInt(dayText);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // 1. 오늘 날짜 강조
        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) == day) {
            holder.viewTodayBg.setVisibility(View.VISIBLE);
            holder.tvDay.setTextColor(Color.parseColor("#C08497"));
        }

        // 2. 생일 체크 및 핑크 점
        String mmdd = String.format("%02d-%02d", month + 1, day);
        StringBuilder namesBuilder = new StringBuilder();
        boolean hasBirthday = false;

        for (Friend f : friends) {
            String bday = f.getBirthday();
            if (bday != null && bday.contains(mmdd)) {
                if (hasBirthday) namesBuilder.append(", ");
                namesBuilder.append(f.getName());
                hasBirthday = true;
            }
        }

        String birthdayNames = namesBuilder.toString();
        if (hasBirthday) {
            holder.viewDot.setVisibility(View.VISIBLE);
        }

        // 클릭 리스너
        holder.itemView.setOnClickListener(v -> {
            if (dateClickListener != null) {
                dateClickListener.onDateClick(day, birthdayNames);
            }
        });

        if (today.get(Calendar.DAY_OF_MONTH) == day && today.get(Calendar.MONTH) == month && dateClickListener != null) {
            // 메인에서 초기화 시 호출하도록 처리하거나 어댑터 내에서 선별적 호출
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        View viewTodayBg, viewDot;

        public CalendarViewHolder(@NonNull View v) {
            super(v);
            tvDay = v.findViewById(R.id.tvDay);
            viewTodayBg = v.findViewById(R.id.viewTodayBg);
            viewDot = v.findViewById(R.id.viewDot);
        }
    }
}

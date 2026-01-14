package com.example.giftyfy;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.giftyfy.friend.Friend;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(int day, String eventNames);
    }

    private final List<String> daysOfMonth;
    private final List<Friend> friends;
    private final List<Anniversary> anniversaries; // ✅ 추가
    private final Calendar calendar;
    private final Calendar today;
    private OnDateClickListener dateClickListener;

    public CalendarAdapter(List<String> daysOfMonth, List<Friend> friends, List<Anniversary> anniversaries, Calendar calendar, OnDateClickListener listener) {
        this.daysOfMonth = daysOfMonth;
        this.friends = friends;
        this.anniversaries = (anniversaries != null) ? anniversaries : new ArrayList<>();
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
        int month = calendar.get(Calendar.MONTH) + 1; // 1-12
        int year = calendar.get(Calendar.YEAR);

        // 1. 오늘 날짜 강조
        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == (month - 1) && today.get(Calendar.DAY_OF_MONTH) == day) {
            holder.viewTodayBg.setVisibility(View.VISIBLE);
            holder.tvDay.setTextColor(Color.parseColor("#C08497"));
        }

        // 2. 이벤트(생일 + 기념일) 체크 및 점 표시
        StringBuilder namesBuilder = new StringBuilder();
        boolean hasEvent = false;

        // 친구 생일 체크
        String mmdd = String.format("%02d-%02d", month, day);
        for (Friend f : friends) {
            if (f.getBirthday() != null && f.getBirthday().contains(mmdd)) {
                if (hasEvent) namesBuilder.append(", ");
                namesBuilder.append(f.getName()).append(" 생일");
                hasEvent = true;
            }
        }

        // 추가된 기념일 체크
        for (Anniversary a : anniversaries) {
            if (a.getMonth() == month && a.getDay() == day) {
                if (hasEvent) namesBuilder.append(", ");
                namesBuilder.append(a.getTitle());
                hasEvent = true;
            }
        }

        if (hasEvent) {
            holder.viewDot.setVisibility(View.VISIBLE);
        }

        final String eventNames = namesBuilder.toString();
        holder.itemView.setOnClickListener(v -> {
            if (dateClickListener != null) {
                dateClickListener.onDateClick(day, eventNames);
            }
        });
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

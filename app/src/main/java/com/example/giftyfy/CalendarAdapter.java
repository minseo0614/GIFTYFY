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
    private final List<Anniversary> anniversaries;
    private final Calendar calendar;
    private final Calendar today;
    private OnDateClickListener dateClickListener;
    private int selectedDay = -1;

    public CalendarAdapter(List<String> daysOfMonth, List<Friend> friends, List<Anniversary> anniversaries, Calendar calendar, OnDateClickListener listener) {
        this.daysOfMonth = daysOfMonth;
        this.friends = friends;
        this.anniversaries = (anniversaries != null) ? anniversaries : new ArrayList<>();
        this.calendar = calendar;
        this.today = Calendar.getInstance();
        this.dateClickListener = listener;
        
        if (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && 
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            this.selectedDay = today.get(Calendar.DAY_OF_MONTH);
        }
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
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        if (today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == (month - 1) && today.get(Calendar.DAY_OF_MONTH) == day) {
            holder.tvDay.setTextColor(Color.parseColor("#D080B6"));
        }

        if (day == selectedDay) {
            holder.viewTodayBg.setVisibility(View.VISIBLE);
            holder.viewTodayBg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FAF4F6")));
        }

        // 이벤트(생일 + 기념일)
        StringBuilder namesBuilder = new StringBuilder();
        boolean hasBirthday = false;
        boolean hasAnniversary = false;

        String mmdd = String.format("%02d-%02d", month, day);
        for (Friend f : friends) {
            if (f.getBirthday() != null && f.getBirthday().contains(mmdd)) {
                if (namesBuilder.length() > 0) namesBuilder.append(", ");
                namesBuilder.append(f.getName()).append(" 생일");
                hasBirthday = true;
            }
        }

        for (Anniversary a : anniversaries) {
            if (a.getMonth() == month && a.getDay() == day) {
                if (namesBuilder.length() > 0) namesBuilder.append(", ");
                namesBuilder.append(a.getTitle());
                hasAnniversary = true;
            }
        }

        if (hasBirthday || hasAnniversary) {
            holder.viewDot.setVisibility(View.VISIBLE);
            // 생일은 핑크색 점, 기념일만 있는 경우는 하늘색 점
            if (hasBirthday) {
                holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D080B6")));
            } else {
                holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#80B6D0")));
            }
        }

        final String eventNames = namesBuilder.toString();
        holder.itemView.setOnClickListener(v -> {
            int oldSelected = selectedDay;
            selectedDay = day;
            notifyItemChanged(position);
            for(int i=0; i<daysOfMonth.size(); i++) {
                if(!daysOfMonth.get(i).isEmpty() && Integer.parseInt(daysOfMonth.get(i)) == oldSelected) {
                    notifyItemChanged(i);
                    break;
                }
            }
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

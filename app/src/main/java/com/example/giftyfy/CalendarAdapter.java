package com.example.giftyfy;

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

    private List<String> daysOfMonth;
    private List<Friend> friends;
    private Calendar calendar;

    public CalendarAdapter(List<String> daysOfMonth, List<Friend> friends, Calendar calendar) {
        this.daysOfMonth = daysOfMonth;
        this.friends = friends;
        this.calendar = calendar;
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

        if (dayText.isEmpty()) {
            holder.layoutBirthdayInfo.setVisibility(View.INVISIBLE);
            return;
        }

        int month = calendar.get(Calendar.MONTH) + 1;
        String currentFormattedDate = String.format("%02d-%02d", month, Integer.parseInt(dayText));

        String birthdayNames = "";
        boolean hasBirthday = false;

        for (Friend friend : friends) {
            String friendBday = friend.getBirthday();
            if (friendBday.length() >= 5) {
                String friendMonthDay = friendBday.substring(friendBday.length() - 5);
                if (friendMonthDay.equals(currentFormattedDate)) {
                    hasBirthday = true;
                    // 이름들을 콤마로 연결 (여러 명일 경우 대비)
                    if (birthdayNames.isEmpty()) {
                        birthdayNames = friend.getName();
                    } else {
                        birthdayNames += ", " + friend.getName();
                    }
                }
            }
        }

        if (hasBirthday) {
            holder.layoutBirthdayInfo.setVisibility(View.VISIBLE);
            holder.tvBirthdayName.setText(birthdayNames);
        } else {
            holder.layoutBirthdayInfo.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvBirthdayName;
        View layoutBirthdayInfo;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvBirthdayName = itemView.findViewById(R.id.tvBirthdayName);
            layoutBirthdayInfo = itemView.findViewById(R.id.layoutBirthdayInfo);
        }
    }
}

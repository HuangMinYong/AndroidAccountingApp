package com.example.water;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterAdapter extends RecyclerView.Adapter<WaterAdapter.ViewHolder> {

    private final List<WaterRecord> recordList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    /**
     * 设置数据并刷新列表
     */
    public void setRecords(List<WaterRecord> records) {
        recordList.clear();
        if (records != null) {
            recordList.addAll(records);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_water_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaterRecord record = recordList.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    /**
     * ViewHolder — 持有 item 视图引用并绑定数据
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textAmount;
        private final TextView textTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAmount = itemView.findViewById(R.id.text_amount);
            textTime = itemView.findViewById(R.id.text_time);
        }

        /**
         * 将 WaterRecord 数据绑定到 UI 控件
         */
        void bind(WaterRecord record) {
            // 显示饮水量，例如 "250 ml"
            textAmount.setText(record.getAmount() + " ml");

            // 将时间戳格式化为 "HH:mm" 显示
            String formattedTime = timeFormat.format(new Date(record.getDate()));
            textTime.setText(formattedTime);
        }
    }
}

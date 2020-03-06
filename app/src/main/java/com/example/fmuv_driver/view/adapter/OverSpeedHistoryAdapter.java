package com.example.fmuv_driver.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.pojo.OverSpeed;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OverSpeedHistoryAdapter extends RecyclerView.Adapter<OverSpeedHistoryAdapter.ViewHolder> {

    private List<OverSpeed> overSpeedList;
    private Context context;

    public OverSpeedHistoryAdapter(Context context, List<OverSpeed> overSpeedList) {
        this.overSpeedList = overSpeedList;
        this.context = context;
    }

    @NonNull
    @Override
    public OverSpeedHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.over_speed_history, parent, false);
        return new OverSpeedHistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OverSpeedHistoryAdapter.ViewHolder holder, int position) {
        final OverSpeed overSpeed = overSpeedList.get(position);
        holder.txtDate.setText(overSpeed.getDate());
        holder.txtRoute.setText(overSpeed.getRoute());
        holder.txtTime.setText(overSpeed.getTime());
        holder.txtSpeed.setText(overSpeed.getSpeed());
    }

    @Override
    public int getItemCount() {
        return overSpeedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtRoute, txtDate, txtTime, txtSpeed;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtSpeed = itemView.findViewById(R.id.txtSpeed);

        }
    }
}
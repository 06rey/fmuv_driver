package com.example.fmuv_driver.view.adapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.handler.ServiceServerEventResponseHandler;
import com.example.fmuv_driver.model.BackgroundHttpRequest;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.model.pojo.Trip;
import com.example.fmuv_driver.service.Speedometer;
import com.example.fmuv_driver.view.activity.SeatingActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TripRecyclerViewAdapter extends RecyclerView.Adapter<TripRecyclerViewAdapter.ViewHolder> {

    private List<Trip> tripList;
    private Context context;
    private SharedPref driverPref;
    private String status = "";
    private String mode;
    private Activity activity;

    public TripRecyclerViewAdapter(Context context, List<Trip> tripList, String mode, Activity activity) {
        this.tripList = tripList;
        this.context = context;
        driverPref = new SharedPref(context, "loginSession");
        this.mode = mode;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trip_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Trip trip = tripList.get(position);
        final String tripId = trip.getTripId();
        final String destination =  trip.getDestination();
        holder.date.setText(trip.getDate());
        holder.from.setText(trip.getFrom());
        holder.to.setText(trip.getTo());
        holder.company.setText(trip.getCompany());
        holder.departure.setText(trip.getDeparture());
        holder.passNo.setText(trip.getPassNo());
        holder.plate.setText(trip.getPlate());

        if (mode.equals("current")) {
            if (trip.getStatus().equals("Traveling")) {
                holder.btnStart.setText("Arrived");
                holder.btnStart.setBackground(context.getResources().getDrawable(R.drawable.rec_success));

                holder.btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                        alertBuilder.setTitle("Confirm")
                                .setMessage("Are you sure you already arrived in "+ trip.getTo() +"?")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Map<String, String> data = new HashMap<>();
                                        data.put("resp", "1");
                                        data.put("main", "trip");
                                        data.put("sub", "trip_arrived");
                                        data.put("trip_id", tripId);

                                        final Map<String, String> finalData = data;

                                        ServiceServerEventResponseHandler serviceServerEventResponseHandler = new ServiceServerEventResponseHandler();
                                        serviceServerEventResponseHandler.setServiceHttpResponseListener(new ServiceServerEventResponseHandler.ServiceServerEventResponseListener() {
                                            @Override
                                            public void onServiceServerEventResponse(List<Map<String, String>> list) {
                                                String status = list.get(0).get("status");
                                                if (status.equals("success")) {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            holder.btnStart.setEnabled(false);
                                                            holder.btnStart.setText("Arrived");
                                                            holder.btnStart.setBackground(context.getResources().getDrawable(R.drawable.rec_success));

                                                            SharedPref sharedPref = new SharedPref(context, "loginSession");
                                                            sharedPref.setValue("tripId", "");
                                                            sharedPref.setValue("tripState", "");
                                                        }
                                                    });
                                                } else {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            AlertDialog.Builder alertBuilder2 = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                                                            alertBuilder2.setTitle("Server connection fail")
                                                                    .setMessage("Please check your internet connection and try again.")
                                                                    .setPositiveButton("OK", null);
                                                            AlertDialog dialog2 = alertBuilder2.create();
                                                            dialog2.show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        new BackgroundHttpRequest(serviceServerEventResponseHandler).okHttpRequest(context ,data, "GET", "");

                                    }
                                })
                                .setNegativeButton("NO", null);

                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }
                });

            } else {
                holder.btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                        alertBuilder.setTitle("Confirm")
                                .setMessage("Are you sure you want to start your trip to "+ trip.getTo() +"?")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Map<String, String> data = new HashMap<>();
                                        data.put("resp", "1");
                                        data.put("main", "trip");
                                        data.put("sub", "start_trip");
                                        data.put("trip_id", tripId);
                                        status = "Traveling";

                                        final Map<String, String> finalData = data;

                                        ServiceServerEventResponseHandler serviceServerEventResponseHandler = new ServiceServerEventResponseHandler();
                                        serviceServerEventResponseHandler.setServiceHttpResponseListener(new ServiceServerEventResponseHandler.ServiceServerEventResponseListener() {
                                            @Override
                                            public void onServiceServerEventResponse(List<Map<String, String>> list) {
                                                String status = list.get(0).get("status");
                                                if (status.equals("success")) {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            holder.btnStart.setText("Arrived");
                                                            holder.btnStart.setBackground(context.getResources().getDrawable(R.drawable.rec_success));
                                                            Intent intent = new Intent(context, Speedometer.class);
                                                            intent.putExtra("tripId", trip.getTripId());
                                                            intent.putExtra("destination", trip.getDestination());
                                                            SharedPref sharedPref = new SharedPref(context, "loginSession");
                                                            sharedPref.setValue("tripId", finalData.get("trip_id"));
                                                            sharedPref.setValue("tripState", finalData.get("Traveling"));
                                                            context.startService(intent);
                                                        }
                                                    });
                                                } else {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            AlertDialog.Builder alertBuilder2 = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                                                            alertBuilder2.setTitle("Server connection fail")
                                                                    .setMessage("Please check your internet connection and try again.")
                                                                    .setPositiveButton("OK", null);
                                                            AlertDialog dialog2 = alertBuilder2.create();
                                                            dialog2.show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        new BackgroundHttpRequest(serviceServerEventResponseHandler).okHttpRequest(context ,data, "GET", "");

                                    }
                                })
                        .setNegativeButton("NO", null);

                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }
                });
            }

            status = trip.getStatus();
            if (status.equals("Arrived")) {
                holder.btnSeat.setEnabled(false);
            }
            holder.btnSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SeatingActivity.class);
                    intent.putExtra("trip_id", tripId);
                    intent.putExtra("status", status);
                    context.startActivity(intent);
                }
            });
        } else {
            holder.btnLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, from, to, company, departure, passNo, plate;
        Button btnStart, btnSeat;
        LinearLayout btnLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            from = itemView.findViewById(R.id.from);
            to = itemView.findViewById(R.id.to);
            company = itemView.findViewById(R.id.company);
            departure = itemView.findViewById(R.id.departure);
            passNo = itemView.findViewById(R.id.passNo);
            plate = itemView.findViewById(R.id.plate);

            btnSeat = itemView.findViewById(R.id.btnSeat);
            btnStart = itemView.findViewById(R.id.btnStart);

            btnLayout = itemView.findViewById(R.id.btnLayout);
        }
    }
}



package com.minew.beaconplusdemo;


import static com.minew.beaconplus.sdk.enums.FrameType.FrameiBeacon;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.minew.beaconplus.sdk.MTFrameHandler;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.frames.IBeaconFrame;
import com.minew.beaconplus.sdk.frames.MinewFrame;

import java.util.ArrayList;
import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<MTPeripheral> mData;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        viewHolder = new ViewHolder(View.inflate(parent.getContext(), R.layout.item, null));
        return viewHolder;
    }

    public MTPeripheral getData(int position) {
        return mData.get(position);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).setDataAndUi(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MTPeripheral> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView b_mac;
        TextView b_battery;
        TextView b_rssi;
        TextView b_uuid;
        TextView b_major;
        TextView b_minor;

        public ViewHolder(View itemView) {
            super(itemView);
            b_mac = (TextView) itemView.findViewById(R.id.mac);
            b_battery = (TextView) itemView.findViewById(R.id.battery);
            b_rssi = (TextView) itemView.findViewById(R.id.rssi);
            b_uuid = (TextView) itemView.findViewById(R.id.uuid);
            b_major = (TextView) itemView.findViewById(R.id.major);
            b_minor = (TextView) itemView.findViewById(R.id.minor);
        }

        @SuppressLint("SetTextI18n")
        public void setDataAndUi(MTPeripheral mtPeripheral) {
            b_mac.setText("Mac: "+ mtPeripheral.mMTFrameHandler.getMac());
            b_battery.setText("Battery: "+mtPeripheral.mMTFrameHandler.getBattery()+"%");
            b_rssi.setText("Rssi: "+ mtPeripheral.mMTFrameHandler.getRssi()+"dBm");
            ArrayList<MinewFrame> advFrames = mtPeripheral.mMTFrameHandler.getAdvFrames();
            for (MinewFrame minewFrame : advFrames) {
                if(minewFrame.getFrameType()==FrameiBeacon){
                    IBeaconFrame iBeaconFrame = (IBeaconFrame) minewFrame;
                    b_uuid.setText("Uuid: "+iBeaconFrame.getUuid());
                    b_major.setText("Major: "+iBeaconFrame.getMajor());
                    b_minor.setText("Minor: "+iBeaconFrame.getMinor());
                }
            }
        }

    }
}

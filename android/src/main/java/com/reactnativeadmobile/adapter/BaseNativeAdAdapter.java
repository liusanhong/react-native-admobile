package com.reactnativeadmobile.adapter;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseNativeAdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public abstract void removeData(Object adSuyiNativeAdInfo);

    public abstract void clearData();

    public abstract void addData(List<Object> datas);
}

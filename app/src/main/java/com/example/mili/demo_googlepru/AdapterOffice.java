package com.example.mili.demo_googlepru;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AdapterOffice extends BaseAdapter {
    private Context context;
    private int resource;
    private ArrayList<Office> officeArrayList;

    public AdapterOffice(Context context, int resource, ArrayList<Office> officeArrayList) {
        this.context = context;
        this.resource = resource;
        this.officeArrayList = officeArrayList;
    }

    @Override
    public int getCount() {
        return officeArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_lvgooglemap,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.txtName = convertView.findViewById(R.id.txtName);
            viewHolder.txtAddress = convertView.findViewById(R.id.txtAdress);
            viewHolder.txtSDT = convertView.findViewById(R.id.txtNumberphone);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Office office =  officeArrayList.get(position);
        viewHolder.txtName.setText(office._mName);
        viewHolder.txtAddress.setText(office._mAdress);
        viewHolder.txtSDT.setText(office._mNumberphone);

        return convertView;
    }

    public class ViewHolder{
        TextView txtName, txtAddress, txtSDT;
    }
}

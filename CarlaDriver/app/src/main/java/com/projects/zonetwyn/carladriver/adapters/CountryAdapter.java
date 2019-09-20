package com.projects.zonetwyn.carladriver.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.projects.zonetwyn.carladriver.R;

public class CountryAdapter extends BaseAdapter implements SpinnerAdapter {

    public class ViewHolder {
        public ImageView icon;
        public TextView name;

        public ViewHolder(View itemView) {
            icon = itemView.findViewById(R.id.itemCountryIcon);
            name = itemView.findViewById(R.id.itemCountryName);
        }
    }

    private Context context;
    private int[] names;
    private int[] icons;

    public CountryAdapter(Context context, int[] names, int[] icons) {
        this.context = context;
        this.names = names;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return names.length;
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
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder")
        View countryView = inflater.inflate(R.layout.item_country, null);
        ViewHolder holder = new ViewHolder(countryView);
        holder.icon.setImageResource(icons[position]);
        holder.name.setText(names[position]);
        return countryView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View countryView = inflater.inflate(R.layout.item_country_dropdown, null);
        ViewHolder holder = new ViewHolder(countryView);
        holder.icon.setImageResource(icons[position]);
        holder.name.setText(names[position]);
        return countryView;
    }

}



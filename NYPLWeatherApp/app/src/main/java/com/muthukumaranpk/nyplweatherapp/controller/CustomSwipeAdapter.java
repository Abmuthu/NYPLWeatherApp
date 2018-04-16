package com.muthukumaranpk.nyplweatherapp.controller;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muthukumaranpk.nyplweatherapp.R;
import com.muthukumaranpk.nyplweatherapp.model.Temperature;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by muthukumaran on 4/14/18.
 */

public class CustomSwipeAdapter extends PagerAdapter {
    private List<Temperature> temperatureList;
    private Context context;
    private Resources resources;

    CustomSwipeAdapter(Context context, List<Temperature> temperatureList) {
        this.context = context;
        this.temperatureList = temperatureList;
        this.resources = context.getResources();
    }

    @Override
    public int getCount() {
        return temperatureList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Temperature currentTemperature = temperatureList.get(position);
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View itemView = layoutInflater.inflate(R.layout.swipe_layout, container, false);
        final TextView dateTextView = itemView.findViewById(R.id.tv_date);
        final TextView maxTempTextView =  itemView.findViewById(R.id.tv_high_temp);
        final TextView minTempTextView =  itemView.findViewById(R.id.tv_low_temp);
        final Date date = new Date(temperatureList.get(position).getDateLong());
        final DateFormat formatter = new SimpleDateFormat(resources.getString(R.string.date_format));
        final String dateFormatted = formatter.format(date);
        dateTextView.setText(dateFormatted);
        maxTempTextView.setText(resources.getString(R.string.max_value, currentTemperature.getMaxTemp()));
        minTempTextView.setText(resources.getString(R.string.min_value, currentTemperature.getMinTemp()));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    void updateTemperatures(List<Temperature> temperatureList) {
        this.temperatureList = temperatureList;
        notifyDataSetChanged();
    }
}

package com.muthukumaranpk.nyplweatherapp.model;

import java.sql.Date;

/**
 * Created by muthukumaran on 4/14/18.
 */

public class Temperature {
    private double maxTemp;
    private double minTemp;
    private long dateLong;

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Temperature that = (Temperature) o;

        Date thisDate = new Date(dateLong);
        Date thatDate = new Date(that.getDateLong());
        return thisDate.toString().equals(thatDate.toString());
    }
}

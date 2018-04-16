package com.muthukumaranpk.nyplweatherapp.controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.muthukumaranpk.nyplweatherapp.R;
import com.muthukumaranpk.nyplweatherapp.model.Temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private CustomSwipeAdapter customSwipeAdapter;
    private List<Temperature> temperatureListByDay;
    private OkHttpClient okHttpClient;
    private Request request;
    private Toast toast;
    private boolean isLoading = false;
    private static final int MILLI_SECOND_FACTOR = 1000;
    private static final String ZIP_CODE = "10018";
    private static final String APP_ID = "b6907d289e10d714a6e88b30761fae22";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperatureListByDay = new ArrayList<>();
        loadWeatherData();
        viewPager = (ViewPager) findViewById(R.id.weather_view_pager);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        customSwipeAdapter = new CustomSwipeAdapter(this, temperatureListByDay);
        viewPager.setAdapter(customSwipeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                loadWeatherData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem refreshButton = menu.findItem(R.id.refresh);
        refreshButton.setEnabled(!isLoading);
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadWeatherData() {
        if (!isNetworkAvailable()) {
            showAToast("Internet not available!");
            return;
        }
        temperatureListByDay = new ArrayList<>();
        okHttpClient = new OkHttpClient();
        request = new Request.Builder().url(getUrl()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Network problem!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isLoading = true;
                invalidateOptionsMenu();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);

                    }
                });

                final List<Temperature> allTemperatureList = parseJsonData(response);
                temperatureListByDay = getTemperatureListByDay(allTemperatureList);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isLoading = false;
                invalidateOptionsMenu();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        viewPager.setVisibility(View.VISIBLE);
                        customSwipeAdapter.updateTemperatures(temperatureListByDay);
                    }
                });


            }
        });
    }

    private List<Temperature> parseJsonData(Response response) {
        final List<Temperature> allTemperatureList = new ArrayList<>();
        try {
            final String jsonData = response.body().string();
            final JSONObject rootJSON = new JSONObject(jsonData);
            final JSONArray listJSONArray = rootJSON.getJSONArray(getResources().getString(R.string.list_key_json));

            for (int i = 0; i < listJSONArray.length(); i++) {
                final JSONObject weatherJSON = listJSONArray.getJSONObject(i);
                final long longDate = weatherJSON.getLong(getResources().getString(R.string.date_key_json));
                final JSONObject mainJSON = weatherJSON.getJSONObject(getResources().getString(R.string.main_key_json));
                final double tempMax = mainJSON.getDouble(getResources().getString(R.string.temp_max_key_json));
                final double tempMin = mainJSON.getDouble(getResources().getString(R.string.temp_min_key_json));
                final Temperature newTemperature = new Temperature();
                newTemperature.setDateLong(longDate * MILLI_SECOND_FACTOR);
                newTemperature.setMaxTemp(tempMax);
                newTemperature.setMinTemp(tempMin);
                allTemperatureList.add(newTemperature);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allTemperatureList;
    }

    // method that find max and min temperatures in a single day and creates a new list out of it
    private List<Temperature> getTemperatureListByDay(List<Temperature> allTemperaturList) {
        final List<Temperature> temperaturesListByDay = new ArrayList<>();
        Temperature currentTemp = allTemperaturList.get(0);
        double maxTemp = currentTemp.getMaxTemp();
        double minTemp = currentTemp.getMinTemp();
        for (int i = 0; i < allTemperaturList.size(); i++) {
            Temperature temperature = allTemperaturList.get(i);
            if (currentTemp.equals(temperature)) {
                if (temperature.getMaxTemp() > maxTemp ) {
                    maxTemp = temperature.getMaxTemp();
                }
                if (temperature.getMinTemp() < minTemp ) {
                    minTemp = temperature.getMinTemp();
                }
                if (i == allTemperaturList.size() - 1) {
                    Temperature newTemperature = new Temperature();
                    newTemperature.setDateLong(currentTemp.getDateLong());
                    newTemperature.setMaxTemp(maxTemp);
                    newTemperature.setMinTemp(minTemp);
                    temperaturesListByDay.add(newTemperature);
                }
            } else {
                final Temperature newTemperature = new Temperature();
                newTemperature.setDateLong(currentTemp.getDateLong());
                newTemperature.setMaxTemp(maxTemp);
                newTemperature.setMinTemp(minTemp);
                temperaturesListByDay.add(newTemperature);
                currentTemp = temperature;
                maxTemp = currentTemp.getMaxTemp();
                minTemp = currentTemp.getMinTemp();
            }
        }
        return temperaturesListByDay;
    }

    private String getUrl() {
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme(getResources().getString(R.string.url_scheme))
                .authority(getResources().getString(R.string.url_authority))
                .appendPath(getResources().getString(R.string.url_path_data))
                .appendPath(getResources().getString(R.string.url_path_version))
                .appendPath(getResources().getString(R.string.url_path_forecast))
                .appendQueryParameter(getResources().getString(R.string.url_query_zip), ZIP_CODE)
                .appendQueryParameter(getResources().getString(R.string.url_query_id), APP_ID);
        return builder.build().toString();
    }

    private boolean isNetworkAvailable() {
        final ConnectivityManager cm = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void showAToast (String message){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}

package com.example.coolweather.android.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.coolweather.android.gson.Weather;
import com.example.coolweather.android.util.HttpUtil;
import com.example.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoupdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager =(AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=  8 * 60 * 60 * 1000;//这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoupdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    /*
    *  更新天气信息
    * */
    private  void updateWeather(){
        SharedPreferences perf = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherString = perf.getString("weather",null);
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weatherId = Utility.handleWeatherResponse(weatherString);
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=c3092ed51eb546008aee4272c51b0345";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().toString();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoupdateService.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                    }
                }
            });
        }
    }



}

package com.example.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.android.gson.Forecast;
import com.example.coolweather.android.gson.Weather;
import com.example.coolweather.android.service.AutoupdateService;
import com.example.coolweather.android.util.HttpUtil;
import com.example.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private Button navButton;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;//添加刷新
    private String mWeatherId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //初始化各个控件
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forcast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        drawerLayout =findViewById(R.id.drawer_layout);
        navButton =findViewById(R.id.nav_button);

        swipeRefreshLayout =findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        //轻量级储存，共享参数
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            mWeatherId =getIntent().getStringExtra("weather_id");
            //String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);//可见度  隐藏
            requestWeather(mWeatherId);
            //requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        //加载图片
//        String bingPic = prefs.getString("bing_pic",null);
//        if (bingPicImg != null){
//            Glide.with(this).load(bingPic).into(bingPicImg);
//        }else {
//            loadBingPic();
//        }
        //手动切换城市
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);//弹出
            }
        });

    }


    /*
    *根据天气id请求城市的天气信息 在服务器中查询
    * */
     public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=c3092ed51eb546008aee4272c51b0345";
        Log.e("___weatherId",weatherId) ;
        //loadBingPic();//加载每日一图
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
             @Override
             public void onResponse(Call call, Response response) throws IOException {
                 final String responseText = response.body().string();
                 final Weather weather = Utility.handleWeatherResponse(responseText);
                 runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         if (weather != null && "ok".equals(weather.status)){
                             //共享
                             SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                             editor.putString("weather",responseText);
                             editor.apply();
                             mWeatherId = weather.basic.weatherId;
                             showWeatherInfo(weather);
                         }else {
                             Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                         }
                         swipeRefreshLayout.setRefreshing(false);//
                     }
                 });
             }

             @Override
             public void onFailure(Call call, IOException e) {
                 e.printStackTrace();
                 Toast.makeText(WeatherActivity.this,"onFailure",Toast.LENGTH_SHORT).show();
                 swipeRefreshLayout.setRefreshing(false);
             }
         });

     }

    /*
    *处理并展示weather实体类中的数据
    * */
    private void  showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree  = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        //填充布局
        for (Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);

            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoupdateService.class);
        startService(intent);
    }


    /*
    * 加载每日一图
    * */
    private void loadBingPic(){
        String requestBingPic = "http://cn.bing.com/th?id=OHR.ThePando_ROW0824984643_1920x1080.jpg&rf=LaDigue_1920x1081920x1080.jpg";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("___loadBingPic",e.toString());

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic = response.body().toString();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}

package com.example.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    //@SerializedName()注解让json字段跟Java字段建立映射关系
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;

    public Update update;
    public  class  Update{
        @SerializedName("loc")
        public String updateTime;
    }
}

package com.example.coolweather.android.util;

import android.text.TextUtils;

import com.example.coolweather.android.dbname.City;
import com.example.coolweather.android.dbname.County;
import com.example.coolweather.android.dbname.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /*
    * 解析和处理服务器返回的省级数据
    * */
    public static  boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces= new JSONArray(response);
                for (int i=1;i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    /*
    * 解析和处理服务器返回的市级数据
    * */
    public static boolean handleCityResponse(String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCitse = new JSONArray(response);
                for (int i = 0 ; i<allCitse.length();i++){
                    JSONObject cityObject = allCitse.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器返回的县级数据
    * */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i=0;i<allCounties.length();i++){
                    JSONObject countyObjece = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObjece.getString("name"));
                    county.setWeatherId(countyObjece.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


}

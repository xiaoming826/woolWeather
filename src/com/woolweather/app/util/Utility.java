package com.woolweather.app.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.woolweather.app.db.WoolWeatherDB;
import com.woolweather.app.model.City;
import com.woolweather.app.model.County;
import com.woolweather.app.model.Province;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			WoolWeatherDB WoolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray array = new JSONArray(response);
				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = (JSONObject) array.get(i);
						Province province = new Province();
						province.setProvinceCode(object.getString("id"));
						province.setProvinceName(object.getString("name"));
						// 将解析出来的数据存储到Province表
						WoolWeatherDB.saveProvince(province);
					}
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesResponse(WoolWeatherDB WoolWeatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray array = jsonObject.getJSONArray("retData");

				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {

						JSONObject object = (JSONObject) array.get(i);
						City city = new City();
						city.setCityCode(object.getString("area_id"));
						city.setCityName(object.getString("name_cn"));
						city.setProvinceId(provinceId);
						// 将解析出来的数据存储到City表
						WoolWeatherDB.saveCity(city);
					}
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse(WoolWeatherDB WoolWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray array = jsonObject.getJSONArray("retData");

				if (array != null && array.length() > 0) {
					for (int i = 0; i < array.length(); i++) {

						JSONObject object = (JSONObject) array.get(i);
						County county = new County();
						county.setCountyCode(object.getString("area_id"));
						county.setCountyName(object.getString("name_cn"));
						county.setCityId(cityId);
						// 将解析出来的数据存储到County表
						WoolWeatherDB.saveCounty(county);
					}
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}

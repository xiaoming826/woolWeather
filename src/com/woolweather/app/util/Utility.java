package com.woolweather.app.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

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

	/**
	 * 解析服务器返回的JSON数据，并将解析出的数据存储到本地。
	 */
	public static void handleWeatherResponse(Context context, String response) {
		String pm25 = "", qlty = "", city = "", latitude = "", longitude = "", date = "", time = "", daily_forecast = "", weather = "", hum = "", pcpn = "", vis = "", tmp = "", pres = "", wind_dir = "", wind_sc = "", wind_spd = "", comf = "", drsg = "", flu = "", sport = "", uv = "";
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray jsonArray = jsonObject
					.getJSONArray("HeWeather data service 3.0");
			jsonObject = jsonArray.getJSONObject(0);

			
			city = jsonObject.getJSONObject("basic").getString("city");
			Log.v("save...", city);
			latitude = jsonObject.getJSONObject("basic").getString("lat");
			longitude = jsonObject.getJSONObject("basic").getString("lon");
			date = jsonObject.getJSONObject("basic").getJSONObject("update")
					.getString("loc").split(" ")[0];
			time = jsonObject.getJSONObject("basic").getJSONObject("update")
					.getString("loc").split(" ")[1];

			daily_forecast = jsonObject.getJSONArray("daily_forecast")
					.toString();

			weather = jsonObject.getJSONObject("now").getJSONObject("cond")
					.getString("txt");
			hum = jsonObject.getJSONObject("now").getString("hum");
			pcpn = jsonObject.getJSONObject("now").getString("pcpn");
			vis = jsonObject.getJSONObject("now").getString("vis");
			tmp = jsonObject.getJSONObject("now").getString("tmp");
			pres = jsonObject.getJSONObject("now").getString("pres");
			wind_dir = jsonObject.getJSONObject("now").getJSONObject("wind")
					.getString("dir");
			wind_sc = jsonObject.getJSONObject("now").getJSONObject("wind")
					.getString("sc");
			wind_spd = jsonObject.getJSONObject("now").getJSONObject("wind")
					.getString("spd");
			comf = jsonObject.getJSONObject("suggestion").getJSONObject("comf")
					.getString("txt");
			drsg = jsonObject.getJSONObject("suggestion").getJSONObject("drsg")
					.getString("txt");
			flu = jsonObject.getJSONObject("suggestion").getJSONObject("flu")
					.getString("txt");
			sport = jsonObject.getJSONObject("suggestion")
					.getJSONObject("sport").getString("txt");
			uv = jsonObject.getJSONObject("suggestion").getJSONObject("uv")
					.getString("txt");
			pm25 = jsonObject.getJSONObject("aqi").getJSONObject("city")
					.getString("pm25");
			qlty = jsonObject.getJSONObject("aqi").getJSONObject("city")
					.getString("qlty");
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			saveWeatherInfo(context, pm25, qlty, city, latitude, longitude,
					date, time, daily_forecast, weather, hum, pcpn, vis, tmp,
					pres, wind_dir, wind_sc, wind_spd, comf, drsg, flu, sport,
					uv);
		}
	}

	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
	 */
	public static void saveWeatherInfo(Context context, String pm25,
			String qlty, String city, String latitude, String longitude,
			String date, String time, String daily_forecast, String weather,
			String hum, String pcpn, String vis, String tmp, String pres,
			String wind_dir, String wind_sc, String wind_spd, String comf,
			String drsg, String flu, String sport, String uv) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("pm25", pm25);
		editor.putString("qlty", qlty);
		editor.putString("city", city);
		editor.putString("latitude", latitude);
		editor.putString("longitude", longitude);
		editor.putString("date", date);
		editor.putString("time", time);
		editor.putString("daily_forecast", daily_forecast);
		editor.putString("weather", weather);
		editor.putString("hum", hum);
		editor.putString("pcpn", pcpn);
		editor.putString("vis", vis);
		editor.putString("tmp", tmp);
		editor.putString("pres", pres);
		editor.putString("wind_dir", wind_dir);
		editor.putString("wind_sc", wind_sc);
		editor.putString("wind_spd", wind_spd);
		editor.putString("comf", comf);
		editor.putString("drsg", drsg);
		editor.putString("flu", flu);
		editor.putString("sport", sport);
		editor.putString("uv", uv);
		editor.commit();
	}
}

package com.woolweather.app.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.woolweather.app.R;
import com.woolweather.app.util.HttpCallbackListener;
import com.woolweather.app.util.HttpUtil;
import com.woolweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	private LinearLayout weather_text_forecast;

	private TextView pm25;
	private TextView qlty;
	private TextView city;
	private TextView latitude;
	private TextView longitude;
	private TextView date;
	private TextView time;
	private TextView weather;
	private TextView hum;
	private TextView pcpn;
	private TextView vis;
	private TextView tmp;
	private TextView pres;
	private TextView wind_dir;
	private TextView wind_sc;
	private TextView wind_spd;
	private TextView comf;
	private TextView drsg;
	private TextView flu;
	private TextView sport;
	private TextView uv;

	/**
	 * 点击次数
	 */
	private int clickTime = 0;
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		// 初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		weather_text_forecast = (LinearLayout) findViewById(R.id.weather_text_forecast);
		pm25 = (TextView) findViewById(R.id.pm25);
		qlty = (TextView) findViewById(R.id.qlty);
		city = (TextView) findViewById(R.id.city_name);
		latitude = (TextView) findViewById(R.id.latitude_text);
		longitude = (TextView) findViewById(R.id.longitude_text);
		date = (TextView) findViewById(R.id.current_date);
		time = (TextView) findViewById(R.id.publish_text);
		weather = (TextView) findViewById(R.id.weather_desp);
		hum = (TextView) findViewById(R.id.hum);
		pcpn = (TextView) findViewById(R.id.pcpn);
		vis = (TextView) findViewById(R.id.vis);
		hum = (TextView) findViewById(R.id.hum);
		tmp = (TextView) findViewById(R.id.temp1);
		pres = (TextView) findViewById(R.id.pres);
		wind_dir = (TextView) findViewById(R.id.wind_dir);
		wind_sc = (TextView) findViewById(R.id.wind_sc);
		wind_spd = (TextView) findViewById(R.id.wind_spd);
		comf = (TextView) findViewById(R.id.comf);
		drsg = (TextView) findViewById(R.id.drsg);
		flu = (TextView) findViewById(R.id.flu);
		sport = (TextView) findViewById(R.id.sport);
		uv = (TextView) findViewById(R.id.uv);

		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		weatherInfoLayout.setOnClickListener(this);
		String countyName = getIntent().getStringExtra("county_Name");
		if (!TextUtils.isEmpty(countyName)) {
			// 有县级代号时就去查询天气
			time.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			city.setVisibility(View.INVISIBLE);
			queryWeatherInfo(countyName);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			time.setText("同步中...");
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String cityName = prefs.getString("city", "");
			if (!TextUtils.isEmpty(cityName)) {
				queryWeatherInfo(cityName);
			}
			break;
		case R.id.weather_info_layout:
			if (clickTime >= 9) {
				Intent intent2 =new Intent(WeatherActivity.this, LoveActivity.class);
				startActivity(intent2);
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {
						clickTime++;
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						clickTime--;
					}
				}).start();
			}
		default:
			break;
		}
	}

	/**
	 * 查询城市名所对应的天气。
	 */
	private void queryWeatherInfo(String countyName) {
		Log.v(this.getClass().getSimpleName() + ",countyName", countyName);
		try {
			countyName = URLEncoder.encode(countyName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String address = "http://apis.baidu.com/heweather/weather/free?city="
				+ countyName;
		queryFromServer(address, "weatherCode");
	}

	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(final String response) {
				if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						time.setText("同步失败");
					}
				});
			}
		});
	}

	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。 String pm25, String qlty, String
	 * city, String latitude, String longitude, String date, String time, String
	 * daily_forecast, String weather, String hum, String vis, String pres,
	 * String wind_dir, String wind_sc, String wind_spd, String comf, String
	 * drsg, String flu, String sport, String uv
	 */
	private void showWeather() {
		weatherInfoLayout.setVisibility(View.INVISIBLE);
		city.setVisibility(View.INVISIBLE);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (TextUtils.isEmpty(prefs.getString("pm25", ""))) {
			findViewById(R.id.ll_pm25).setVisibility(View.GONE);
		} else {
			pm25.setText(prefs.getString("pm25", ""));
		}
		if (TextUtils.isEmpty(prefs.getString("qlty", ""))) {
			findViewById(R.id.ll_qlty).setVisibility(View.GONE);
		} else {
			qlty.setText(prefs.getString("qlty", ""));
		}
		city.setText(prefs.getString("city", ""));
		Log.v(this.getClass().getSimpleName(), prefs.getString("city", ""));
		latitude.setText(prefs.getString("latitude", ""));
		longitude.setText(prefs.getString("longitude", ""));
		date.setText(prefs.getString("date", ""));
		time.setText("今天" + prefs.getString("time", "") + "发布");
		weather.setText(prefs.getString("weather", ""));
		hum.setText(prefs.getString("hum", ""));
		pcpn.setText(prefs.getString("pcpn", ""));
		vis.setText(prefs.getString("vis", ""));
		tmp.setText(prefs.getString("tmp", ""));
		pres.setText(prefs.getString("pres", ""));
		wind_dir.setText(prefs.getString("wind_dir", ""));
		wind_sc.setText(prefs.getString("wind_sc", ""));
		wind_spd.setText(prefs.getString("wind_spd", ""));
		comf.setText(prefs.getString("comf", ""));
		drsg.setText(prefs.getString("drsg", ""));
		flu.setText(prefs.getString("flu", ""));
		sport.setText(prefs.getString("sport", ""));
		uv.setText(prefs.getString("uv", ""));
		String daily_forecast = prefs.getString("daily_forecast", "");
		JSONArray array;
		try {
			array = new JSONArray(daily_forecast);
			for (int i = 0; i < array.length(); i++) {
				String date = array.getJSONObject(i).getString("date");
				String tmp_max = array.getJSONObject(i).getJSONObject("tmp")
						.getString("max");
				String tmp_min = array.getJSONObject(i).getJSONObject("tmp")
						.getString("min");
				String weather_d = array.getJSONObject(i).getJSONObject("cond")
						.getString("txt_d");
				String weather_n = array.getJSONObject(i).getJSONObject("cond")
						.getString("txt_n");
				String sr = array.getJSONObject(i).getJSONObject("astro")
						.getString("sr");
				String ss = array.getJSONObject(i).getJSONObject("astro")
						.getString("ss");
				String wind_dir = array.getJSONObject(i).getJSONObject("wind")
						.getString("dir");
				String wind_sc = array.getJSONObject(i).getJSONObject("wind")
						.getString("sc");

				View view = LayoutInflater.from(this).inflate(
						R.layout.daily_forecast, null);
				view.setMinimumWidth(260);
				TextView date_text = (TextView) view.findViewById(R.id.date);
				TextView weather_d_text = (TextView) view
						.findViewById(R.id.weather_d);
				TextView weather_n_text = (TextView) view
						.findViewById(R.id.weather_n);
				TextView tmp_max_text = (TextView) view
						.findViewById(R.id.tmp_max);
				TextView tmp_min_text = (TextView) view
						.findViewById(R.id.tmp_min);
				TextView wind_dir_text = (TextView) view
						.findViewById(R.id.wind_dir);
				TextView wind_sc_text = (TextView) view
						.findViewById(R.id.wind_sc);
				TextView sr_text = (TextView) view.findViewById(R.id.sr);
				TextView ss_text = (TextView) view.findViewById(R.id.ss);
				date_text.setText(date);
				weather_d_text.setText(weather_d);
				weather_n_text.setText(weather_n);
				tmp_max_text.setText(tmp_max);
				tmp_min_text.setText(tmp_min);
				wind_dir_text.setText(wind_dir);
				wind_sc_text.setText(wind_sc);
				sr_text.setText(sr);
				ss_text.setText(ss);
				weather_text_forecast.addView(view);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		weatherInfoLayout.setVisibility(View.VISIBLE);
		city.setVisibility(View.VISIBLE);
	}
}

package com.woolweather.app.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.woolweather.app.R;
import com.woolweather.app.db.WoolWeatherDB;
import com.woolweather.app.model.City;
import com.woolweather.app.model.County;
import com.woolweather.app.model.Province;
import com.woolweather.app.util.HttpCallbackListener;
import com.woolweather.app.util.HttpUtil;
import com.woolweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WoolWeatherDB woolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	private List<Province> provinceList;

	private List<City> cityList;

	private List<County> countyList;

	private Province selectedProvince;

	private City selectedCity;

	private County selectedCounty;

	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		woolWeatherDB = WoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}

		});
		queryProvinces();
	}

	private void queryProvinces() {
		provinceList = woolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer("http://apis.baidu.com/netpopo/zipcode/area",
					"province");
		}

	}

	protected void queryCities() {
		cityList = woolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			String encode = null;
			try {
				encode = URLEncoder.encode(selectedProvince.getProvinceName(),
						"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			queryFromServer(
					"http://apis.baidu.com/apistore/weatherservice/citylist?cityname="
							+ encode, "city");
		}
	}

	protected void queryCounties() {
		countyList = woolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			String encode = null;
			try {
				encode = URLEncoder.encode(selectedCity.getCityName(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			queryFromServer(
					"http://apis.baidu.com/apistore/weatherservice/citylist?cityname="
							+ encode, "county");
		}

	}

	private void queryFromServer(String address, final String type) {
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(woolWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(woolWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(woolWeatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}

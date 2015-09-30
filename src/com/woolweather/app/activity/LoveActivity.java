package com.woolweather.app.activity;

import com.woolweather.app.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class LoveActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_love);
		
	}
	
	
	
}

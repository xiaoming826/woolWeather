package com.woolweather.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("apikey",
							"97de9f11bda0568bc54fa4b8cd3674e6");
					connection.setReadTimeout(8000);
					connection.setConnectTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in, "UTF-8"));
					StringBuilder response = new StringBuilder();
					String line;

					while ((line = reader.readLine()) != null) {
						response.append(line);
					}

					if (listener != null) {
						// 回掉onFinish 方法
						listener.onFinish(response.toString());
					}

				} catch (IOException e) {
					e.printStackTrace();
					if (listener != null) {
						// 回掉onError 方法
						listener.onError(e);
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

}

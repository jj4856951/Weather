package com.zjw.weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.air.AirData;
import com.thinkland.juheapi.data.weather.WeatherData;
import com.zjw.weather.bean.FutureWeatherBean;
import com.zjw.weather.bean.HoursWeatherBean;
import com.zjw.weather.bean.PmWeatherBean;
import com.zjw.weather.bean.WeatherBean;
import com.zjw.weather.service.WeatherService;
import com.zjw.weather.service.WeatherService.CallBack;
import com.zjw.weather.service.WeatherService.WeatherServiceBinder;
import com.zjw.weather.swiperefresh.PullToRefreshBase;
import com.zjw.weather.swiperefresh.PullToRefreshBase.OnRefreshListener;
import com.zjw.weather.swiperefresh.PullToRefreshScrollView;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWeather extends Activity {
	private PullToRefreshScrollView mPullToRefreshScrollView;
	private ScrollView mScrollView;
	private WeatherService mService;
	
	private TextView tv_city,
	tv_release,
	tv_now_weather,
	tv_today_temp,
	tv_now_temp,
	tv_aqi,
	tv_quality,
	tv_next_3,
	tv_next_6,
	tv_next_9,
	tv_next_12,
	tv_next_15,
	tv_next_3_tmp,
	tv_next_6_tmp,
	tv_next_9_tmp,
	tv_next_12_tmp,
	tv_next_15_tmp,
	tv_today_temp_a,
	tv_today_temp_b,
	tv_tommorrow,
	tv_tommorrow_temp_a,
	tv_tommorrow_temp_b,
	tv_3rd_day,
	tv_3rd_temp_a,
	tv_3rd_temp_b,
	tv_4th_day,
	tv_4th_temp_a,
	tv_4th_temp_b,
	tv_felt_air_temp,
	tv_humidity,
	tv_wind,
	tv_uv_index,
	tv_dressing_index;
	
	private ImageView iv_now_weather,
	iv_next_3,
	iv_next_6,
	iv_next_9,
	iv_next_12,
	iv_next_15,
	iv_today_weather,
	iv_tommorrow_weather,
	iv_3rd_weather,
	iv_4th_weather;
	
	private RelativeLayout rl_city;
	

	ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService.removeCallback();
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			WeatherServiceBinder binder = (WeatherServiceBinder) service;
			mService = binder.getService();
//			mService.test();
			mService.setCallBack(new CallBack() {
				
				@Override
				public void onParseComplete(List<HoursWeatherBean> list,
						PmWeatherBean pmBean, WeatherBean weatherBean) {
					mPullToRefreshScrollView.onRefreshComplete();
					if (list != null && list.size() >= 5) {
						set3HourForcastView(list);
					}
					if (pmBean!= null) {
						setPmView(pmBean);
					}
					if (weatherBean!= null) {
						setView(weatherBean);
					}
				}
			});
			mService.getCityWeather();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mian);
		init();
//		getCityWeather();
		initService();
	}
	
	private void initService(){
		Intent intent = new Intent(this, WeatherService.class);
		startService(intent);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	

	private void setPmView(PmWeatherBean bean) {
		tv_aqi.setText(bean.getAqi());
		tv_quality.setText(bean.getQuality());
		
	}



	private void set3HourForcastView(List<HoursWeatherBean> list) {
		set3HourForcastData(tv_next_3, iv_next_3, tv_next_3_tmp, list.get(0));
		set3HourForcastData(tv_next_6, iv_next_6, tv_next_6_tmp, list.get(1));
		set3HourForcastData(tv_next_9, iv_next_9, tv_next_9_tmp, list.get(2));
		set3HourForcastData(tv_next_12, iv_next_12, tv_next_12_tmp, list.get(3));
		set3HourForcastData(tv_next_15, iv_next_15, tv_next_15_tmp, list.get(4));
	}
	private void set3HourForcastData(TextView tv1, ImageView iv, TextView tv2, HoursWeatherBean bean) {
		String preFix;
		int hour = Integer.valueOf(bean.getTime());
		if (hour >=6 && hour<18) {
			preFix = "d";
		}else {
			preFix = "n";
		}
		tv1.setText(bean.getTime());
		iv.setImageResource(getResources().getIdentifier(preFix+bean.getWeather_id(), "drawable", "com.zjw.weather"));
		tv2.setText(bean.getTemp()+"°");
	}
	



	private void setView(WeatherBean weatherBean) {
		tv_city.setText(weatherBean.getCity());
		tv_release.setText(weatherBean.getRelease());
		tv_now_weather.setText(weatherBean.getWeather_str());
		String replace = weatherBean.getTemp().replace("℃", "");
		String[] split = replace.split("~");
		String temp_str_a = split[0];
		String temp_str_b = split[1];
		tv_today_temp.setText("↓"+temp_str_a+"°"+" "+"↑"+temp_str_b+"°");//↓↑°   4℃~8℃
		tv_now_temp.setText(weatherBean.getNow_temp());
		
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		String preFix;
		if (hour >=6 && hour<18) {
			preFix = "d";
		}else {
			preFix = "n";
		}
		iv_today_weather.setImageResource(getResources().getIdentifier(preFix+weatherBean.getWeather_id(), "drawable", "com.zjw.weather"));
		tv_today_temp_a.setText(temp_str_a+"°");
		tv_today_temp_b.setText(temp_str_b+"°");
		List<FutureWeatherBean> futureList = weatherBean.getFutureLists();
		if (futureList.size() == 3) {
			setFutureData(tv_tommorrow, iv_tommorrow_weather, tv_tommorrow_temp_a, tv_tommorrow_temp_b, futureList.get(0));
			setFutureData(tv_3rd_day, iv_3rd_weather, tv_3rd_temp_a, tv_3rd_temp_b, futureList.get(1));
			setFutureData(tv_4th_day, iv_4th_weather, tv_4th_temp_a, tv_4th_temp_b, futureList.get(2));
		}

		iv_now_weather.setImageResource(getResources().getIdentifier(preFix+weatherBean.getWeather_id(), "drawable", "com.zjw.weather"));
		tv_humidity.setText(weatherBean.getHumidity());
		tv_dressing_index.setText(weatherBean.getDressing_index());
		tv_uv_index.setText(weatherBean.getUv_index());
		tv_wind.setText(weatherBean.getWind());
//		tv_felt_air_temp.setText(weatherBean.getFelt_temp());
	}
	
	public void setFutureData(TextView tv_week, ImageView iv_weather, TextView tv_temp_a,TextView tv_temp_b, FutureWeatherBean bean) {
		tv_week.setText(bean.getWeek());
		iv_weather.setImageResource(getResources().getIdentifier("d"+bean.getWeather_id(), "drawable", "com.zjw.weather"));
		String replace = bean.getTemp().replace("℃", "");
		String[] split = replace.split("~");
		String temp_str_a = split[0];
		String temp_str_b = split[1];
		tv_temp_a.setText(temp_str_a+"°");
		tv_temp_b.setText(temp_str_b+"°");
	}
	

	
	public void init(){
		mPullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
		mPullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
//				getCityWeather();
				mService.getCityWeather();
			}
		});
		
		//通过第三方控件获取mScrollView
		mScrollView = mPullToRefreshScrollView.getRefreshableView();
		
		rl_city = (RelativeLayout) findViewById(R.id.rl_city);
		rl_city.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityWeather.this, CityActivity.class);
				startActivityForResult(intent, 1);
			}
		});
		
		tv_city = (TextView) findViewById(R.id.tv_city);
		tv_release = (TextView) findViewById(R.id.tv_release);
		tv_now_weather = (TextView) findViewById(R.id.tv_now_weather);
		tv_today_temp = (TextView) findViewById(R.id.tv_today_temp);
		tv_now_temp = (TextView) findViewById(R.id.tv_now_temp);
		tv_aqi = (TextView) findViewById(R.id.tv_aqi);
		tv_quality = (TextView) findViewById(R.id.tv_quality);
		tv_next_3 = (TextView) findViewById(R.id.tv_next_3);
		tv_next_6 = (TextView) findViewById(R.id.tv_next_6);
		tv_next_9 = (TextView) findViewById(R.id.tv_next_9);
		tv_next_12 = (TextView) findViewById(R.id.tv_next_12);
		tv_next_15 = (TextView) findViewById(R.id.tv_next_15);
		tv_next_3_tmp = (TextView) findViewById(R.id.tv_next_3_tmp);
		tv_next_6_tmp = (TextView) findViewById(R.id.tv_next_6_tmp);
		tv_next_9_tmp = (TextView) findViewById(R.id.tv_next_9_tmp);
		tv_next_12_tmp = (TextView) findViewById(R.id.tv_next_12_tmp);
		tv_next_15_tmp = (TextView) findViewById(R.id.tv_next_15_tmp);
		tv_today_temp_a = (TextView) findViewById(R.id.tv_today_temp_a);
		tv_today_temp_b = (TextView) findViewById(R.id.tv_today_temp_b);
		tv_tommorrow = (TextView) findViewById(R.id.tv_tommorrow);
		tv_tommorrow_temp_a = (TextView) findViewById(R.id.tv_tommorrow_temp_a);
		tv_tommorrow_temp_b = (TextView) findViewById(R.id.tv_tommorrow_temp_b);
		tv_3rd_day = (TextView) findViewById(R.id.tv_3rd_day);
		tv_3rd_temp_a = (TextView) findViewById(R.id.tv_3rd_temp_a);
		tv_3rd_temp_b = (TextView) findViewById(R.id.tv_3rd_temp_b);
		tv_4th_day = (TextView) findViewById(R.id.tv_4th_day);
		tv_4th_temp_a = (TextView) findViewById(R.id.tv_4th_temp_a);
		tv_4th_temp_b = (TextView) findViewById(R.id.tv_4th_temp_b);
		tv_felt_air_temp = (TextView) findViewById(R.id.tv_felt_air_temp);
		tv_humidity = (TextView) findViewById(R.id.tv_humidity);
		tv_wind = (TextView) findViewById(R.id.tv_wind);
		tv_uv_index = (TextView) findViewById(R.id.tv_uv_index);
		tv_dressing_index  = (TextView) findViewById(R.id.tv_dressing_index);
		
		iv_now_weather = (ImageView) findViewById(R.id.iv_now_weather);
		iv_next_3 = (ImageView) findViewById(R.id.iv_next_3);
		iv_next_6 = (ImageView) findViewById(R.id.iv_next_6);
		iv_next_9 = (ImageView) findViewById(R.id.iv_next_9);
		iv_next_12 = (ImageView) findViewById(R.id.iv_next_12);
		iv_next_15 = (ImageView) findViewById(R.id.iv_next_15);
		iv_today_weather = (ImageView) findViewById(R.id.iv_today_weather);
		iv_tommorrow_weather = (ImageView) findViewById(R.id.iv_tommorrow_weather);
		iv_3rd_weather = (ImageView) findViewById(R.id.iv_3rd_weather);
		iv_4th_weather = (ImageView) findViewById(R.id.iv_4th_weather);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode ==1 && resultCode == 1) {
			String city = data.getStringExtra("city");
			mService.getCityWeather(city);
		}
	}
	
	@Override
	protected void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}
}

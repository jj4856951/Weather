package com.zjw.weather.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WeatherService extends Service {
	protected static final int REPEAT_MSG = 0;
	protected static final int CALLBACK_OK = 1;
	protected static final int CALLBACK_ERROR = 2;

	private String city;
	private static final String TAG = "WeatherService";

	private WeatherServiceBinder binder = new WeatherServiceBinder();
	//这两个的作用是判断回调函数的执行
	private boolean isRunning = false;
	private int count = 0;
	
	private List<HoursWeatherBean> list;
	private PmWeatherBean pmBean;
	private WeatherBean weatherBean;
	
	private CallBack callBack;
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REPEAT_MSG:
				getCityWeather();
				sendEmptyMessageDelayed(REPEAT_MSG, 30 * 60 * 1000);
				break;
			case CALLBACK_OK:
				if (callBack != null) {
					callBack.onParseComplete(list, pmBean, weatherBean);
				}
				isRunning = false;
				break;
			case CALLBACK_ERROR:
				Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		};
	};
	
	public interface CallBack {
		public void onParseComplete(List<HoursWeatherBean> list, PmWeatherBean pmBean, WeatherBean weatherBean);
	}
	
	public void setCallBack(CallBack callBack) {
		this.callBack = callBack;
	}
	public void removeCallback() {
		this.callBack = null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	public void test() {
		Log.i(TAG, "test方式执行了");
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		city = "北京";
		handler.sendEmptyMessage(REPEAT_MSG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
	}
	
	public class WeatherServiceBinder extends Binder {
		public WeatherService getService() {
			return WeatherService.this;
		}
	}
	
	public void getCityWeather(String city) {
		this.city = city;
		getCityWeather();
	}
	
	/**
	 * 获取城市天气预报
	 */
	
	public void getCityWeather() {
		if (isRunning) {
			return;
		}
		isRunning = true;
		final CountDownLatch countDownLatch = new CountDownLatch(3);
		
		WeatherData weatherData = WeatherData.getInstance();
		weatherData.getByCitys(city, 2, new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				weatherBean = parseWeather(arg0);
				countDownLatch.countDown();
			}
		});
		weatherData.getForecast3h(city, new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				list = parse3HoursWrather(arg0);
				countDownLatch.countDown();
			}
		});
		AirData airData = AirData.getInstance();
		airData.cityAir(city, new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject arg0) {
				pmBean = parsePM(arg0);
				countDownLatch.countDown();
			}
		});
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					countDownLatch.await();
					handler.sendEmptyMessage(CALLBACK_OK);
				} catch (InterruptedException e) {
					handler.sendEmptyMessage(CALLBACK_ERROR);
					return;
				}
				
			}
		}).start();
	}
	/**
	 * 解析未来每隔3小时的天气
	 */
	private List<HoursWeatherBean> parse3HoursWrather(JSONObject json) {
		List<HoursWeatherBean> list = null;
		Date date = new Date(System.currentTimeMillis());//获取当前的时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (code == 200 && error_code == 0) {
				list = new ArrayList<HoursWeatherBean>();
				JSONArray resultArray = json.getJSONArray("result");
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject hObject = resultArray.getJSONObject(i);
					Date dateH = sdf.parse(hObject.getString("sfdate"));
					if (!dateH.after(date)) {
						continue;
					}
					HoursWeatherBean bean = new HoursWeatherBean();
					bean.setWeather_id(hObject.getString("weatherid"));
					bean.setTemp(hObject.getString("temp1"));
					Calendar c = Calendar.getInstance();
					c.setTime(dateH);
					bean.setTime(c.get(Calendar.HOUR_OF_DAY)+"");
					list.add(bean);
					if (list.size() == 5) {
						break;
					}
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 解析城市天气预报
	 * @param json
	 * @return
	 */
	public WeatherBean parseWeather(JSONObject json) {
		WeatherBean bean = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (code == 200 && error_code == 0) {
				JSONObject resultObject = json.getJSONObject("result");
				bean = new WeatherBean();
				
				//today
				JSONObject todayObj = resultObject.getJSONObject("today");
				bean.setCity(todayObj.getString("city"));
				bean.setUv_index(todayObj.getString("uv_index"));
				bean.setTemp(todayObj.getString("temperature"));
				bean.setWeather_str(todayObj.getString("weather"));
				bean.setDressing_index(todayObj.getString("dressing_index"));
				bean.setWeather_id(todayObj.getJSONObject("weather_id").getString("fa"));
				
				//sk
				JSONObject skObj = resultObject.getJSONObject("sk");
				bean.setWind(skObj.getString("wind_direction")+skObj.getString("wind_strength"));
				bean.setNow_temp(skObj.getString("temp"));
				bean.setRelease(skObj.getString("time"));
				bean.setHumidity(skObj.getString("humidity"));
				Date date = new Date(System.currentTimeMillis());//当前系统的时间
				
				List<FutureWeatherBean> list = new ArrayList<FutureWeatherBean>();
				//future
				JSONArray futureArr = resultObject.getJSONArray("future");
				for(int i = 0; i< futureArr.length(); i++){
					JSONObject futureObj = futureArr.getJSONObject(i);
//					futureBean.setDate(futureObj.getString("date"));
					Date dateF = sdf.parse(futureObj.getString("date"));
					if (!dateF.after(date)) {//判断时间的前后
						continue;
					}
					FutureWeatherBean futureBean = new FutureWeatherBean();
					futureBean.setTemp(futureObj.getString("temperature"));
					futureBean.setWeather_id(futureObj.getJSONObject("weather_id").getString("fa"));
					futureBean.setWeek(futureObj.getString("week"));
					list.add(futureBean);
					if(list.size() == 3){
						break;//list只需要取3个就行
					}
				}
				bean.setFutureLists(list);
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return bean;
	}
	
	/**
	 * 解析城市空气质量
	 * @param json
	 * @return
	 */
	private PmWeatherBean parsePM(JSONObject json) {
		PmWeatherBean bean = null;
		try {
			int code = json.getInt("resultcode");
			int error_code = json.getInt("error_code");
			if (code == 200 && error_code == 0) {
				bean = new PmWeatherBean();
				JSONObject pmObj = json.getJSONArray("result").getJSONObject(0).getJSONObject("citynow");
				bean.setAqi(pmObj.getString("AQI"));
				bean.setQuality(pmObj.getString("quality"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

}

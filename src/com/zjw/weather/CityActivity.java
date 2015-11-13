package com.zjw.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thinkland.juheapi.common.JsonCallBack;
import com.thinkland.juheapi.data.weather.WeatherData;
import com.zjw.weather.adapter.CityListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class CityActivity extends Activity {
	
	private ListView lv_city;
	private List<String>  list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		getCityData();
		
		lv_city = (ListView) findViewById(R.id.lv_city);
		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	public void getCityData() {
		WeatherData data = WeatherData.getInstance();
		data.getCities(new JsonCallBack() {
			
			@Override
			public void jsonLoaded(JSONObject json) {
				try {
					int code = json.getInt("resultcode");
					int error_code = json.getInt("error_code");
					if (code == 200 && error_code == 0) {
						Set<String> set = new HashSet<String>();
						list = new ArrayList<String>();
						
						JSONArray cityArr = json.getJSONArray("result");
						for (int i = 0; i < cityArr.length(); i++) {
							String cityName = cityArr.getJSONObject(i).getString("city");
							set.add(cityName);
						}
						list.addAll(set);
						CityListAdapter adapter = new CityListAdapter(CityActivity.this, list);
						lv_city.setAdapter(adapter);
						lv_city.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								setResult(1, new Intent().putExtra("city", list.get(position)));
								finish();
							}
						});
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}

package com.zjw.weather.adapter;

import java.util.List;

import com.zjw.weather.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListAdapter extends BaseAdapter {
	private Context context;
	private List<String> list;
	
	public CityListAdapter(Context context, List<String> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public String getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = LayoutInflater.from(context).inflate(R.layout.item_city_list, null);
			
		}else {
			view = convertView;
		}
		TextView tv_city = (TextView) view.findViewById(R.id.tv_city);
		tv_city.setText(list.get(position));
		return view;
	}

}

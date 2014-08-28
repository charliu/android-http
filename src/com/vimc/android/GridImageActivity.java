package com.vimc.android;

import com.vimc.ahttp.R;
import com.vimc.ahttp.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GridImageActivity extends Activity {
	private GridView gridView;

	public static final String[] IMAGES = ImgUrls.getBig(150);
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid);
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(new MyAdapter());
	}

	private class MyAdapter extends BaseAdapter {

		private MyAdapter() {
		};

		@Override
		public int getCount() {
			return IMAGES.length;
		}

		@Override
		public Object getItem(int position) {
			return IMAGES[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(GridImageActivity.this).inflate(R.layout.grid_item, null);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.img);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String requestUrl = IMAGES[position];
			ImageLoader.getInstance().load(requestUrl, holder.image, R.drawable.android);
			return convertView;
		}

		class ViewHolder {
			ImageView image;
		}

	}

}

package com.vimc.android;

import com.vimc.ahttp.R;
import com.vimc.ahttp.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListImageActivity extends Activity {
	private ListView listView;

	public static String[] IMAGES = ImgUrls.getSmall(100);
	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		listView = (ListView) findViewById(R.id.list_view);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);
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
//			HLog.i("GetView position:" + position + " URL:" + IMAGES[position]);
			if (convertView == null) {
				convertView = LayoutInflater.from(ListImageActivity.this).inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image_view);
				holder.text = (TextView) convertView.findViewById(R.id.text_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String requestUrl = IMAGES[position];
			ImageLoader.getInstance().load(requestUrl, holder.image, R.drawable.android, R.drawable.error96);

			holder.text.setText("hello world " + position);
			return convertView;
		}

		class ViewHolder {
			ImageView image;
			TextView text;
		}

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case 1:
			IMAGES = ImgUrls.getSmall(100);
			adapter.notifyDataSetChanged();
			break;
		case 2:
			IMAGES = ImgUrls.IMAGES;
			adapter.notifyDataSetChanged();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, 1, "Small Image");
		menu.add(Menu.NONE, 2, 1, "Big Image");
		return super.onCreateOptionsMenu(menu);
	}

}

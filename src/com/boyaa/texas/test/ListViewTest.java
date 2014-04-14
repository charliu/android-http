package com.boyaa.texas.test;

import java.util.List;

import com.boyaa.texas.http.ImageLoader;
import com.boyaa.texas.http.ImageLruCache;
import com.boyaa.texas.http.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListViewTest extends Activity {
	private ListView listView;

	 private ImageLoader loader = new ImageLoader(new ImageLruCache());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(new MyAdapter(ImgUrls.getSmall(80)));
	}

	private class MyAdapter extends BaseAdapter {
		private List<String> urls;

		private MyAdapter(List<String> list) {
			urls = list;
		};

		@Override
		public int getCount() {
			return urls.size();
		}

		@Override
		public Object getItem(int position) {
			return urls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(ListViewTest.this).inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image_view);
				holder.text = (TextView) convertView.findViewById(R.id.text_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String requestUrl = urls.get(position);
			loader.load(requestUrl, holder.image);
			
			holder.text.setText("hello world " + position);
			return convertView;
		}

		class ViewHolder {
			ImageView image;
			TextView text;
		}

	}

}

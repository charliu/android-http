package com.boyaa.texas.test;

import java.util.ArrayList;
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

public class ListViewTest extends Activity{
	private ListView listView;
	ImageLoader loader = new ImageLoader(new ImageLruCache());
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(new MyAdapter(getList()));
	}
	
	private List<String> getList() {
		List<String> list = new ArrayList<String>();
		list.add("http://cdn-img.easyicon.net/png/11414/1141416.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141418.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141419.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141420.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141421.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141422.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141423.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141424.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141425.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141426.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141427.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141428.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141429.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141430.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141431.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141432.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141433.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141434.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141435.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141436.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141437.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141438.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141439.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141440.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141441.gif");
		list.add("http://cdn-img.easyicon.net/png/11414/1141442.gif");
		
		return list;
	}
	
	private class MyAdapter extends BaseAdapter {
		private List<String> urls;
		private MyAdapter(List<String> list) {
			urls = list;
		}
		
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
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(ListViewTest.this).inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image_view);
				holder.text = (TextView) convertView.findViewById(R.id.text_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			loader.load(urls.get(position), ImageLoader.getImageHandler(holder.image, R.drawable.ic_launcher, 0));
			holder.text.setText("hello world " + position);
			return convertView;
		}
		
		class ViewHolder {
			ImageView image;
			TextView text;
		}
		
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}

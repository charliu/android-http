package com.vimc.android;

import com.vimc.ahttp.HLog;
import com.vimc.ahttp.R;
import com.vimc.ahttp.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListImageActivity extends Activity {
	private ListView listView;

	private ImageLoader loader = ImageLoader.getInstance();

	public static final String[] IMAGES = ImgUrls.getSmall(20);
	
	private ImageView img;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		listView = (ListView) findViewById(R.id.list_view);
		listView.setAdapter(new MyAdapter());
		img = (ImageView) findViewById(R.id.img);
		String u = "http://cdn.urbanislandz.com/wp-content/uploads/2011/10/MMSposter-large.jpg";
		String ur = "http://image.donga.com/mlbpark/fileUpload/201401/52dde15113c53b01da3b.jpg";
		ImageLoader.getInstance().load(ur, img, R.drawable.ps_96);
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
			HLog.i("GetView position:" + position + " URL:" + IMAGES[position]);
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
			ImageLoader.getInstance().load(requestUrl, holder.image, R.drawable.ps_96);

			holder.text.setText("hello world " + position);
			return convertView;
		}

		class ViewHolder {
			ImageView image;
			TextView text;
		}

	}

}

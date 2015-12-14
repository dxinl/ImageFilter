package com.mx.dengxinliang.imageeffects;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private static final int CHOSE_PIC_REQ_CODE = 1111;
	private static final String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ImageFilter";
	private static final String IMAGE = ".jpg";
	private static final String[] FILTER_NAMES = new String[]{"_GRAY", "_BAW", "_BACKSHEET", "_RELIEF"};

	private FloatingActionButton fab;
	private RecyclerView filters;
	private Bitmap bitmap;
	private ImageView img;

	private String path;
	private int fabMargin;
	private int curFilter;
	private boolean showFilter;

	static {
		System.loadLibrary("imageeffects");
	}

	private native boolean toGray(Bitmap bitmap);

	private native boolean toBAW(Bitmap bitmap);

	private native boolean toBackSheet(Bitmap bitmap);

	private native boolean toRelief(Bitmap bitmap);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fabMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

		fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(this);

		filters = (RecyclerView) findViewById(R.id.filters);
		filters.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
		filters.setAdapter(new FilterAdapter());
		filters.setTranslationY(Utils.getScreenHeight(MainActivity.this));
		filters.setAlpha(0f);
		filters.setVisibility(View.VISIBLE);

		img = (ImageView) findViewById(R.id.image);
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
		img.setImageBitmap(bitmap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.chose_pic:
				if (showFilter) {
					hideFilterAndShowFab();
				}

				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				MainActivity.this.startActivityForResult(intent, CHOSE_PIC_REQ_CODE);
				break;
			case R.id.save_pic:
				if (curFilter == 0) {
					Toast.makeText(MainActivity.this, getString(R.string.not_modified), Toast.LENGTH_SHORT).show();
				} else {
					File dir = new File(DIR_PATH);
					if (!dir.exists() && !dir.isDirectory()) {
						if (!dir.mkdir()) {
							Toast.makeText(MainActivity.this, getString(R.string.create_failed), Toast.LENGTH_SHORT).show();
							break;
						}
					}
					if (path == null) {
						path = "/default";
					}
					StringBuilder sb = new StringBuilder(path);
					int index1 = sb.lastIndexOf("/");
					int index2 = sb.lastIndexOf(".");
					if (index1 == -1) {
						index1 = 0;
					}
					if (index2 == -1) {
						index2 = sb.length();
					}
					if (index2 < index1) {
						index2 = sb.length();
					}
					String fileName = sb.substring(index1, index2);
					sb = new StringBuilder(DIR_PATH);
					StringBuilder fullName = new StringBuilder(fileName);
					fullName.append(FILTER_NAMES[curFilter - 1]).append(IMAGE);
					sb.append(fullName);
					Log.d("MainActivity", sb.toString());

					File file = new File(sb.toString());
					if (!file.exists() && !file.isFile()) {
						try {
							if (!file.createNewFile()) {
								Toast.makeText(MainActivity.this, getString(R.string.not_modified), Toast.LENGTH_SHORT).show();
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
							Toast.makeText(MainActivity.this, getString(R.string.not_modified), Toast.LENGTH_SHORT).show();
							break;
						}
					}

					try {
						ContentValues values = new ContentValues();
						ContentResolver resolver = MainActivity.this.getContentResolver();
						values.put(MediaStore.Images.ImageColumns.DATA, sb.toString());
						values.put(MediaStore.Images.ImageColumns.TITLE, fullName.toString());
						values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fullName.toString());
						values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis() / 1000);
						values.put(MediaStore.Images.ImageColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
						values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
						values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
						values.put(MediaStore.Images.ImageColumns.WIDTH, bitmap.getWidth());
						values.put(MediaStore.Images.ImageColumns.HEIGHT, bitmap.getHeight());
						Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

						if (uri == null) {
							Toast.makeText(MainActivity.this, getString(R.string.write_failed), Toast.LENGTH_SHORT).show();
						} else {
							OutputStream stream = resolver.openOutputStream(uri);
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

							values.clear();
							values.put(MediaStore.Images.ImageColumns.SIZE, file.length());
							resolver.update(uri, values, null, null);

							if (stream != null) {
								stream.flush();
								stream.close();
							}

							Toast.makeText(MainActivity.this, getString(R.string.write_succeed), Toast.LENGTH_SHORT).show();
						}
					} catch (IOException e) {
						e.printStackTrace();
						Toast.makeText(MainActivity.this, getString(R.string.write_failed), Toast.LENGTH_SHORT).show();
					}
				}
				break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOSE_PIC_REQ_CODE && data != null) {
			Uri uri = data.getData();
			path = getRealPathFromURI(uri);
			bitmap = BitmapFactory.decodeFile(path);

			if (bitmap == null) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
			}
			img.setImageBitmap(bitmap);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && showFilter) {
			hideFilterAndShowFab();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fab) {
			showFilter = true;
			ObjectAnimator fabAnim = ObjectAnimator.ofFloat(fab, "translationY", fab.getHeight() + fabMargin);
			ObjectAnimator filtersAnim1 = ObjectAnimator.ofFloat(filters, "translationY", 0f);
			ObjectAnimator filtersAnim2 = ObjectAnimator.ofFloat(filters, "alpha", 1f);
			AnimatorSet set = new AnimatorSet();
			set.playTogether(fabAnim, filtersAnim1, filtersAnim2);
			set.start();
		}
	}

	private void hideFilterAndShowFab() {
		ObjectAnimator fabAnim = ObjectAnimator.ofFloat(fab, "translationY", 0f);
		ObjectAnimator filtersAnim1 = ObjectAnimator.ofFloat(filters, "translationY", Utils.getScreenHeight(this));
		ObjectAnimator filtersAnim2 = ObjectAnimator.ofFloat(filters, "alpha", 0f);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(fabAnim, filtersAnim1, filtersAnim2);
		set.start();
		showFilter = false;
	}

	private void onFilterItemClicked(int position) {
		hideFilterAndShowFab();
		new PictureProcess().execute(position);
	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = {MediaStore.Images.Media.DATA};
		CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
		Cursor cursor = cursorLoader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private final class PictureProcess extends AsyncTask<Integer, Object, Boolean> {
		private AlertDialog dialog;
		private int position;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new AlertDialog.Builder(MainActivity.this)
					.setMessage(getString(R.string.processing))
					.setCancelable(false)
					.create();
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Integer[] params) {
			position = params[0];
			switch (params[0]) {
				case 0:
					if (path != null) {
						bitmap = BitmapFactory.decodeFile(path);
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
						}
					} else {
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
					}
					return true;
				case 1:
					if (path != null) {
						bitmap = BitmapFactory.decodeFile(path);
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
						}
					} else {
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
					}
					return toGray(bitmap);
				case 2:
					if (path != null) {
						bitmap = BitmapFactory.decodeFile(path);
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
						}
					} else {
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
					}
					return toBAW(bitmap);
				case 3:
					if (path != null) {
						bitmap = BitmapFactory.decodeFile(path);
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
						}
					} else {
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
					}
					return toBackSheet(bitmap);
				case 4:
					if (path != null) {
						bitmap = BitmapFactory.decodeFile(path);
						if (bitmap == null) {
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
						}
					} else {
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.meinvtupianbizhi_813_100);
					}
					return toRelief(bitmap);
				default:
					return true;
			}
		}

		@Override
		protected void onPostExecute(Boolean flag) {
			img.setImageBitmap(bitmap);
			dialog.dismiss();
			if (flag) {
				Toast.makeText(MainActivity.this, getString(R.string.process_succeed), Toast.LENGTH_SHORT).show();
				curFilter = position;
			} else {
				Toast.makeText(MainActivity.this, getString(R.string.process_failed), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private final class FilterAdapter extends RecyclerView.Adapter<FilterHolder> {
		private final String[] filterNames = new String[]{
				getString(R.string.normal), getString(R.string.gray), getString(R.string.baw),
				getString(R.string.back_sheet), getString(R.string.relief)};
		private final int[] resIds = new int[]{
				R.mipmap.preview_normal, R.mipmap.preview_gray,
				R.mipmap.preview_baw, R.mipmap.preview_backsheet, R.mipmap.preview_relief};

		@Override
		public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new FilterHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_filters, parent, false));
		}

		@Override
		public void onBindViewHolder(FilterHolder holder, final int position) {
			holder.declare.setText(filterNames[position]);
			holder.preview.setImageBitmap(BitmapFactory.decodeResource(getResources(), resIds[position]));

			holder.preview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MainActivity.this.onFilterItemClicked(position);
				}
			});
		}

		@Override
		public int getItemCount() {
			return filterNames.length;
		}
	}

	private final class FilterHolder extends RecyclerView.ViewHolder {
		ImageView preview;
		TextView declare;

		public FilterHolder(View itemView) {
			super(itemView);
			preview = (ImageView) itemView.findViewById(R.id.preview);
			declare = (TextView) itemView.findViewById(R.id.declare);
		}
	}
}

package com.mx.dengxinliang.imageeffects;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by DengXinliang on 2015/12/8.
 */
public class Utils {
	public static int getScreenWidth(Activity activity) {
		return getScreenMetrics(activity).widthPixels;
	}

	public static int getScreenHeight(Activity activity) {
		return getScreenMetrics(activity).heightPixels;
	}

	private static DisplayMetrics getScreenMetrics(Activity activity) {
		WindowManager wm = activity.getWindowManager();
		DisplayMetrics metrics = new DisplayMetrics();
		Display display = wm.getDefaultDisplay();
		display.getMetrics(metrics);
		return metrics;
	}
}

package org.api;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * description：
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ：27674569
 * Email: 27674569@qq.com
 * Version：1.0
 */

public class ViewUtils {

    private ViewUtils() {
        throw new IllegalStateException("is private constructor.");
    }

    public static ViewBinder sViewBinder = new ViewBinder() {
        @Override
        protected ViewFinder getViewFinder(Object target, View view) {
            return new ViewFinder(target, view);
        }
    };

    public static void setViewBinder(ViewBinder viewBinder) {
        sViewBinder = viewBinder;
    }

    public static void bindActivity(Activity activity) {
        sViewBinder.bindActivity(activity);
    }

    public static void bindFragment(Fragment fragment, View rootView) {
        sViewBinder.bindObject(fragment, rootView);
    }

    public static void bindTarget(Object target, View rootView) {
        sViewBinder.bindObject(target, rootView);
    }

    public static void bindView(View view) {
        sViewBinder.bindObject(view, null);
    }
}

package org.api;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

/**
 * description：
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ：27674569
 * Email: 27674569@qq.com
 * Version：1.0
 */

public class ViewFinder {

    private Activity mActivity;
    private Fragment mFragment;
    private View mView;
    private Context mContext;

    public ViewFinder(Object object, View view) {

        if (object instanceof Activity) {
            mActivity = (Activity) object;
            mContext = mActivity;
        } else if (object instanceof Fragment) {
            mFragment = (Fragment) object;
            mContext = mFragment.getContext();
        } else if (object instanceof View) {
            mView = (View) object;
            mContext = mView.getContext();
        }

        if (view != null) {
            mView = view;
        }
    }

    public final <T extends View> T findViewById(int viewId) {
        return (T) (mView != null ? mView.findViewById(viewId) : mActivity != null ? mActivity.findViewById(viewId) : null);
    }

    /**
     * Intent  bundle传参数
     */
    public final <T> T getExtra(String key) {
        if (mActivity != null) {
            return (T) mActivity.getIntent().getExtras().get(key);
        }
        if (mFragment != null) {
            return (T) mFragment.getArguments().get(key);
        }
        return null;
    }

    /**
     * 判断是否有网络
     */
    public final boolean isOpenNetWork(String toast) {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isAvailable()) {
            return true;
        }
        showToast(toast);
        return false;
    }


    public final boolean isFirstClick(long time) {
        return Utils.isFirstClick(time);
    }

    /**
     * Toast 样式 重写 可以自定义
     */
    protected void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
}

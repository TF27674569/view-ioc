package org.api;

import android.app.Activity;
import android.view.View;

import org.api.exception.CompilerEcxeption;
import org.compiler.IOCHelper;

import java.lang.reflect.Constructor;

/**
 * description：
 * <p/>
 * Created by TIAN FENG on 2018/1/26.
 * QQ：27674569
 * Email: 27674569@qq.com
 * Version：1.0
 */

public abstract class ViewBinder {


    public ViewBinder() {

    }

    public void bindActivity(Activity activity) {
        try {
            getConstructor(activity).newInstance(activity, getViewFinder(activity, null));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void bindObject(Object object,View view) {
        try {
            getConstructor(object).newInstance(object, getViewFinder(object, view));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private Constructor getConstructor(Object object) {
        try {
            Class<?> targetClazz = Class.forName(object.getClass().getName() + IOCHelper.IOC_TAG);
            return targetClazz.getConstructor(object.getClass(), ViewFinder.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CompilerEcxeption("类 " + object.getClass().getName() + IOCHelper.IOC_TAG + " 是否存在，或者是否存在对应的构造器.");
        }
    }


    protected abstract ViewFinder getViewFinder(Object target, View view);


}

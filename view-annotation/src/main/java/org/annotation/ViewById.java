package org.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * description:初始化控件
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ??27674569
 * Email: 27674569@qq.com
 * Version??1.0
 */
@Target(ElementType.FIELD) // 放在成员变量上
@Retention(RetentionPolicy.CLASS) // 编译时期存在
public @interface ViewById {
    int value();// 注解里面可以放 int 类型的值（必须是final的值）
}

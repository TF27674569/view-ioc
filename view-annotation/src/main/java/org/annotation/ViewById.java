package org.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * description:��ʼ���ؼ�
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ??27674569
 * Email: 27674569@qq.com
 * Version??1.0
 */
@Target(ElementType.FIELD) // ���ڳ�Ա������
@Retention(RetentionPolicy.CLASS) // ����ʱ�ڴ���
public @interface ViewById {
    int value();// ע��������Է� int ���͵�ֵ��������final��ֵ��
}

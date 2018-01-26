package org.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����¼�
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Event {
    int[] value();
}

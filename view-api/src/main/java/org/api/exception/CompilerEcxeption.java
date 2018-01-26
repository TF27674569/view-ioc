package org.api.exception;

/**
 * description：
 * <p/>
 * Created by TIAN FENG on 2018/1/26.
 * QQ：27674569
 * Email: 27674569@qq.com
 * Version：1.0
 */

public class CompilerEcxeption extends RuntimeException{

    public CompilerEcxeption() {
    }

    public CompilerEcxeption(String message) {
        super(message);
    }

    public CompilerEcxeption(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerEcxeption(Throwable cause) {
        super(cause);
    }

    public CompilerEcxeption(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

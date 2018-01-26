package org.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import org.annotation.CheckNet;
import org.annotation.EchoEnable;
import org.annotation.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * description: 提供生成.java源文件的辅助类
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ:27674569
 * Email: 27674569@qq.com
 * Version:1.0
 */
public class IOCHelper {
    public static final String IOC_TAG = "_IOC";

    // 注解所在类累的Element
    private TypeElement mClassElement;

    // 包名
    private String mPackageName;

    //  类的绝对名称
    private String mClassSimpleName;

    // ViewById 的 VariableElement集合
    Map<Integer, VariableElement> viewByIdElements = new HashMap<>();

    // Event 的 ExecutableElement集合
    Map<int[], ExecutableElement> eventElements = new HashMap<>();

    // Extra 的 VariableElement集合
    Map<String, VariableElement> extraElements = new HashMap<>();


    /**
     * @param classElement classElement
     * @param elementUtils elementUtils
     */
    public IOCHelper(TypeElement classElement, Elements elementUtils) {
        mClassElement = classElement;

        // 拿这个类型的包名的Element
        PackageElement packageElement = elementUtils.getPackageOf(classElement);

        // 获取package的全名称
        mPackageName = packageElement.getQualifiedName().toString();

        // 辅助类类名
        mClassSimpleName = classElement.getSimpleName().toString() + IOC_TAG;
    }


    /**
     * 注解所在类累的Element
     */
    public TypeElement getTypeElement() {
        return mClassElement;
    }

    /**
     * 获取包名
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * 类名
     */
    public String getClassSimpleName() {
        return mClassSimpleName;
    }

    /**
     * 全类名
     */
    public String getClassName() {
        return mPackageName + mClassSimpleName;
    }

    // 获取创建.java文件的TypeSpec对象
    public TypeSpec getTypeSpec() {
        // 组装类
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(getClassSimpleName())
                .addModifiers(PUBLIC, FINAL);

        // 执行在主线程注解
        ClassName uiThreadClassName = ClassName.get("android.support.annotation", "UiThread");

        // 注入对象的类名
        ClassName parameterClassName = ClassName.bestGuess(mClassElement.asType().toString());
        // 组装构造函数
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(uiThreadClassName)
                .addModifiers(PUBLIC)
                .addParameter(parameterClassName, "target", FINAL)
                .addParameter(ClassName.get("org.api", "ViewFinder"), "finder", FINAL);


        // 处理Extra 处理 参数的传递
        for (Map.Entry<String, VariableElement> extraVariableElementEntry : extraElements.entrySet()) {
            // intent 传参的Key
            String key = extraVariableElementEntry.getKey();
            // 注解对应的成员变量
            VariableElement extraElement = extraVariableElementEntry.getValue();
            // 拿到注解成员变量的名称
            String filedName = extraElement.getSimpleName().toString();
            // 调用 ViewFinder的函数
            constructorBuilder.addStatement("target.$L = finder.getExtra($L)", filedName, key);
        }


        // 处理ViewById的事件 viewByIdElements
        for (Map.Entry<Integer, VariableElement> viewByIdVariableElementEntry : viewByIdElements.entrySet()) {
            // 控件Id
            int viewId = viewByIdVariableElementEntry.getKey();
            // 注解对应的成员变量
            VariableElement extraElement = viewByIdVariableElementEntry.getValue();
            // 拿到注解成员变量的名称
            String filedName = extraElement.getSimpleName().toString();
            // 调用 ViewFinder的函数
            constructorBuilder.addStatement("target.$L = finder.findViewById($L)", filedName, viewId);
        }

        // 处理点击事件
        for (Map.Entry<int[], ExecutableElement> eventElementEntry : eventElements.entrySet()) {
            // 获取注解上面的数组
            int[] viewIds = eventElementEntry.getKey();
            // 获取点击事件函数对应的Element
            ExecutableElement eventElement = eventElementEntry.getValue();
            // 拿到函数的名称
            String methodName = eventElement.getSimpleName().toString();

            // 拿到函数需要参数的成员
            List<? extends VariableElement> parameterElements = eventElement.getParameters();

            // 拿到仅有的参数
            VariableElement parameterElement = null;
            // 判断参数个数
            if (parameterElements != null && parameterElements.size() > 0) {

                // 判断是否只有一个参数
                if (parameterElements.size() > 1) {
                    throw new IllegalArgumentException("method " + methodName + "  parameter size only one, and  parameter type must android.view.View .");
                }

                // 拿到仅有的参数
                parameterElement = eventElement.getParameters().get(0);
            }


            // 判断是否需要处理 重复点击事件
            EchoEnable echoEnable = eventElement.getAnnotation(EchoEnable.class);
            // 判断是否需要处理网络检测功能
            CheckNet checkNet = eventElement.getAnnotation(CheckNet.class);

            // 遍历对应ViewId 并设置点击事件
            for (int viewId : viewIds) {

                // 点击事件的View类名
                ClassName viewClassName = ClassName.get("android.view", "View");
                // 组键点击事件
                constructorBuilder.beginControlFlow(" finder.findViewById($L).setOnClickListener(new $L.OnClickListener() ", viewId, viewClassName);
                // Override注解
                constructorBuilder.addCode("@Override\n");
                // OnClickListener 接口函数
                constructorBuilder.beginControlFlow(" public void onClick($L v) ", viewClassName);

                // 组建 判断是否需要检测网络
                if (checkNet != null) {
                    constructorBuilder.beginControlFlow(" if (finder.isOpenNetWork($S))", checkNet.value());
                }

                // 组建 判断是否需要处理重复点击
                if (echoEnable != null) {
                    constructorBuilder.beginControlFlow(" if (finder.isFirstClick($L))", echoEnable.value());
                }

                // 组建 判断是否需要处理函数的参数
                if (parameterElement == null) {
                    constructorBuilder.addStatement("target.$L()", methodName);
                } else {
                    constructorBuilder.addStatement("target.$L(($L)v)", methodName, ClassName.bestGuess(parameterElement.asType().toString()));
                }

                // 组建 判断是否需要检测网络  结尾括号
                if (checkNet != null) {
                    constructorBuilder.endControlFlow();
                }

                // 组建 判断是否需要处理重复点击 结尾括号
                if (echoEnable != null) {
                    constructorBuilder.endControlFlow();
                }

                // onClick函数的大括号
                constructorBuilder.endControlFlow();

                // 内部类括号
                constructorBuilder.endControlFlow();
                // 函数参数的结尾括号
                constructorBuilder.addStatement(")");

            }
        }

        typeSpecBuilder.addMethod(constructorBuilder.build());

        return typeSpecBuilder.build();
    }
}

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
 * description: �ṩ����.javaԴ�ļ��ĸ�����
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ:27674569
 * Email: 27674569@qq.com
 * Version:1.0
 */
public class IOCHelper {
    public static final String IOC_TAG = "_IOC";

    // ע���������۵�Element
    private TypeElement mClassElement;

    // ����
    private String mPackageName;

    //  ��ľ�������
    private String mClassSimpleName;

    // ViewById �� VariableElement����
    Map<Integer, VariableElement> viewByIdElements = new HashMap<>();

    // Event �� ExecutableElement����
    Map<int[], ExecutableElement> eventElements = new HashMap<>();

    // Extra �� VariableElement����
    Map<String, VariableElement> extraElements = new HashMap<>();


    /**
     * @param classElement classElement
     * @param elementUtils elementUtils
     */
    public IOCHelper(TypeElement classElement, Elements elementUtils) {
        mClassElement = classElement;

        // ��������͵İ�����Element
        PackageElement packageElement = elementUtils.getPackageOf(classElement);

        // ��ȡpackage��ȫ����
        mPackageName = packageElement.getQualifiedName().toString();

        // ����������
        mClassSimpleName = classElement.getSimpleName().toString() + IOC_TAG;
    }


    /**
     * ע���������۵�Element
     */
    public TypeElement getTypeElement() {
        return mClassElement;
    }

    /**
     * ��ȡ����
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * ����
     */
    public String getClassSimpleName() {
        return mClassSimpleName;
    }

    /**
     * ȫ����
     */
    public String getClassName() {
        return mPackageName + mClassSimpleName;
    }

    // ��ȡ����.java�ļ���TypeSpec����
    public TypeSpec getTypeSpec() {
        // ��װ��
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(getClassSimpleName())
                .addModifiers(PUBLIC, FINAL);

        // ִ�������߳�ע��
        ClassName uiThreadClassName = ClassName.get("android.support.annotation", "UiThread");

        // ע����������
        ClassName parameterClassName = ClassName.bestGuess(mClassElement.asType().toString());
        // ��װ���캯��
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(uiThreadClassName)
                .addModifiers(PUBLIC)
                .addParameter(parameterClassName, "target", FINAL)
                .addParameter(ClassName.get("org.api", "ViewFinder"), "finder", FINAL);


        // ����Extra ���� �����Ĵ���
        for (Map.Entry<String, VariableElement> extraVariableElementEntry : extraElements.entrySet()) {
            // intent ���ε�Key
            String key = extraVariableElementEntry.getKey();
            // ע���Ӧ�ĳ�Ա����
            VariableElement extraElement = extraVariableElementEntry.getValue();
            // �õ�ע���Ա����������
            String filedName = extraElement.getSimpleName().toString();
            // ���� ViewFinder�ĺ���
            constructorBuilder.addStatement("target.$L = finder.getExtra($L)", filedName, key);
        }


        // ����ViewById���¼� viewByIdElements
        for (Map.Entry<Integer, VariableElement> viewByIdVariableElementEntry : viewByIdElements.entrySet()) {
            // �ؼ�Id
            int viewId = viewByIdVariableElementEntry.getKey();
            // ע���Ӧ�ĳ�Ա����
            VariableElement extraElement = viewByIdVariableElementEntry.getValue();
            // �õ�ע���Ա����������
            String filedName = extraElement.getSimpleName().toString();
            // ���� ViewFinder�ĺ���
            constructorBuilder.addStatement("target.$L = finder.findViewById($L)", filedName, viewId);
        }

        // �������¼�
        for (Map.Entry<int[], ExecutableElement> eventElementEntry : eventElements.entrySet()) {
            // ��ȡע�����������
            int[] viewIds = eventElementEntry.getKey();
            // ��ȡ����¼�������Ӧ��Element
            ExecutableElement eventElement = eventElementEntry.getValue();
            // �õ�����������
            String methodName = eventElement.getSimpleName().toString();

            // �õ�������Ҫ�����ĳ�Ա
            List<? extends VariableElement> parameterElements = eventElement.getParameters();

            // �õ����еĲ���
            VariableElement parameterElement = null;
            // �жϲ�������
            if (parameterElements != null && parameterElements.size() > 0) {

                // �ж��Ƿ�ֻ��һ������
                if (parameterElements.size() > 1) {
                    throw new IllegalArgumentException("method " + methodName + "  parameter size only one, and  parameter type must android.view.View .");
                }

                // �õ����еĲ���
                parameterElement = eventElement.getParameters().get(0);
            }


            // �ж��Ƿ���Ҫ���� �ظ�����¼�
            EchoEnable echoEnable = eventElement.getAnnotation(EchoEnable.class);
            // �ж��Ƿ���Ҫ���������⹦��
            CheckNet checkNet = eventElement.getAnnotation(CheckNet.class);

            // ������ӦViewId �����õ���¼�
            for (int viewId : viewIds) {

                // ����¼���View����
                ClassName viewClassName = ClassName.get("android.view", "View");
                // �������¼�
                constructorBuilder.beginControlFlow(" finder.findViewById($L).setOnClickListener(new $L.OnClickListener() ", viewId, viewClassName);
                // Overrideע��
                constructorBuilder.addCode("@Override\n");
                // OnClickListener �ӿں���
                constructorBuilder.beginControlFlow(" public void onClick($L v) ", viewClassName);

                // �齨 �ж��Ƿ���Ҫ�������
                if (checkNet != null) {
                    constructorBuilder.beginControlFlow(" if (finder.isOpenNetWork($S))", checkNet.value());
                }

                // �齨 �ж��Ƿ���Ҫ�����ظ����
                if (echoEnable != null) {
                    constructorBuilder.beginControlFlow(" if (finder.isFirstClick($L))", echoEnable.value());
                }

                // �齨 �ж��Ƿ���Ҫ�������Ĳ���
                if (parameterElement == null) {
                    constructorBuilder.addStatement("target.$L()", methodName);
                } else {
                    constructorBuilder.addStatement("target.$L(($L)v)", methodName, ClassName.bestGuess(parameterElement.asType().toString()));
                }

                // �齨 �ж��Ƿ���Ҫ�������  ��β����
                if (checkNet != null) {
                    constructorBuilder.endControlFlow();
                }

                // �齨 �ж��Ƿ���Ҫ�����ظ���� ��β����
                if (echoEnable != null) {
                    constructorBuilder.endControlFlow();
                }

                // onClick�����Ĵ�����
                constructorBuilder.endControlFlow();

                // �ڲ�������
                constructorBuilder.endControlFlow();
                // ���������Ľ�β����
                constructorBuilder.addStatement(")");

            }
        }

        typeSpecBuilder.addMethod(constructorBuilder.build());

        return typeSpecBuilder.build();
    }
}

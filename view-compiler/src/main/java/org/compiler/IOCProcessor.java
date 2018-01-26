package org.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import org.annotation.CheckNet;
import org.annotation.Event;
import org.annotation.Extra;
import org.annotation.ViewById;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * description: 编译器在编译时期进行扫描的进程
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ:27674569
 * Email: 27674569@qq.com
 * Version:1.0
 */
@AutoService(Processor.class) // 生成 META-INF 信息 告诉处理器需要处理我自定义的这个处理进程
public class IOCProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;

    /**
     * String 一个代表类名
     * IOCHelper  该类存在的所有信息
     */
    private Map<String, IOCHelper> mHelpers = new HashMap<>();

    /**
     * 每一个注解处理器类都必须有一个空的构造函数。
     * 然而，这里有一个特殊的init()方法，它会被注解处理工具调用，
     * 并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements,Types和Filer
     *
     * @param processingEnv API https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/ProcessingEnvironment.html
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }


    /**
     * 指定需要扫描哪些注解（自定义的）
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annottoanTypes = new LinkedHashSet<>();
        annottoanTypes.add(ViewById.class.getCanonicalName());
        annottoanTypes.add(Event.class.getCanonicalName());
        annottoanTypes.add(Extra.class.getCanonicalName());
        annottoanTypes.add(CheckNet.class.getCanonicalName());
        return annottoanTypes;
    }


    /**
     * 用来指定你使用的Java版本
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        /**
         * 返回能支持的最高版本
         */
        return SourceVersion.latestSupported();
    }


    /**
     * 编译期间 扫描某个类是 存在自己声明的注解会回调此函数
     *
     * @return true 停止编译
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mHelpers.clear();
        scanViewById(roundEnv);
        scanExtra(roundEnv);
        scanEvent(roundEnv);
        return createJavaFile();
    }

    /**
     * 扫描ViewById注解
     */
    private void scanViewById(RoundEnvironment roundEnv) {
        //  扫描ViewById注解
        Set<? extends Element> viewByIdElements = roundEnv.getElementsAnnotatedWith(ViewById.class);

        // 注解
        for (Element viewByIdElement : viewByIdElements) {
            // 检测标签
            if (!checkAnnotation(viewByIdElement, Extra.class, ElementKind.FIELD)) {
                return;
            }
            // 拿到文件Elenemt
            VariableElement fieldElement = (VariableElement) viewByIdElement;
            // 根据全类名 获取对应的helper
            IOCHelper helper = addHelperToHelpers(fieldElement);
            // 拿到当前类型的注解对象
            ViewById annotation = fieldElement.getAnnotation(ViewById.class);
            // 添加到辅助类，最后统一处理
            helper.viewByIdElements.put(annotation.value(), fieldElement);
        }

    }

    // 扫描Extra
    private void scanExtra(RoundEnvironment roundEnv) {
        //  扫描ViewById注解
        Set<? extends Element> extraElements = roundEnv.getElementsAnnotatedWith(Extra.class);

        // 注解
        for (Element extraElement : extraElements) {
            // 检测标签
            if (!checkAnnotation(extraElement, Extra.class, ElementKind.FIELD)) {
                return;
            }
            // 拿到文件Elenemt
            VariableElement fieldElement = (VariableElement) extraElement;
            // 根据全类名 获取对应的helper
            IOCHelper helper = addHelperToHelpers(fieldElement);
            // 拿到当前类型的注解对象
            Extra annotation = fieldElement.getAnnotation(Extra.class);
            // 添加到辅助类，最后统一处理
            helper.extraElements.put(annotation.value(), fieldElement);
        }
    }

    /**
     * 扫描Event点击事件
     */
    private void scanEvent(RoundEnvironment roundEnv) {
        //  扫描ViewById注解
        Set<? extends Element> eventElements = roundEnv.getElementsAnnotatedWith(Event.class);

        // 注解
        for (Element eventElement : eventElements) {
            // 检测标签
            if (!checkAnnotation(eventElement, Event.class, ElementKind.METHOD)) {
                return;
            }
            // 拿到文件Elenemt
            ExecutableElement methodElement = (ExecutableElement) eventElement;
            // 根据全类名 获取对应的helper
            IOCHelper helper = addHelperToHelpers(methodElement);
            // 拿到当前类型的注解对象
            Event annotation = methodElement.getAnnotation(Event.class);
            // 添加到辅助类，最后统一处理
            helper.eventElements.put(annotation.value(), methodElement);
        }
    }


    /**
     * 添加到辅助类并返回辅助类
     */
    private IOCHelper addHelperToHelpers(Element element) {
        // 拿到类的Element
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        // 拿到当前全类名
        String qualifileame = classElement.getQualifiedName().toString();
        // 根据全类名 获取对应的helper
        IOCHelper helper = mHelpers.get(qualifileame);
        if (helper == null) {
            helper = new IOCHelper(classElement, mElementUtils);
            mHelpers.put(qualifileame, helper);
        }
        return helper;
    }

    /**
     * 创建java文件
     * @return
     */
    private boolean createJavaFile() {
        for (Map.Entry<String, IOCHelper> stringIOCHelperEntry : mHelpers.entrySet()) {
            // 注解所在类的全类名 eg:com.compilerdemo.MainActivity
            IOCHelper helper = stringIOCHelperEntry.getValue();

            try {
                System.out.println(helper.getPackageName()+"-------------------------------------");
                JavaFile.builder(helper.getPackageName(), helper.getTypeSpec())
                            .addFileComment("自动生成代码")
                            .build()
                            .writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void can(RoundEnvironment roundEnv) {

//        // 获取ViewByID注解所对应的Element
//        Set<? extends Element> viewByIdElements = roundEnv.getElementsAnnotatedWith(ViewById.class);
//        //  解析 ViewById
//        Map<Element, List<Element>> viewByIdElementElementMap = new LinkedHashMap<>();
//
//        for (Element viewByIdElement : viewByIdElements) {
//            Element enclosingElement = viewByIdElement.getEnclosingElement();
//
//            List<Element> elements = viewByIdElementElementMap.get(enclosingElement);
//            if (elements == null) {
//                elements = new ArrayList<>();
//                viewByIdElementElementMap.put(enclosingElement, elements);
//            }
//            elements.add(viewByIdElement);
//
//        }
//
//        // 获取Event注解所对应的Element
//        Set<? extends Element> eventElements = roundEnv.getElementsAnnotatedWith(Event.class);
//        //  解析 Event
//        Map<Element, List<Element>> eventElementElementMap = new LinkedHashMap<>();
//
//        for (Element eventElement : eventElements) {
//            // 获取所在类 类Element
//            Element enclosingElement = eventElement.getEnclosingElement();
//
//            System.out.println("------------------------------------------------------------------------------------");
//            System.out.println(enclosingElement.getSimpleName());
//            System.out.println("------------------------------------------------------------------------------------");
//            List<Element> elements = eventElementElementMap.get(enclosingElement);
//            if (elements == null) {
//                elements = new ArrayList<>();
//                eventElementElementMap.put(enclosingElement, elements);
//            }
//            elements.add(eventElement);
//        }
//
//
//        // 生成 java 类
//        for (Map.Entry<Element, List<Element>> entry : viewByIdElementElementMap.entrySet()) {
//            Element enclosingElement = entry.getKey();
//            List<Element> elements = entry.getValue();
//            String classNameStr = enclosingElement.getSimpleName().toString();
//
//            // 组装类:  xxx_ViewHelpers
//            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classNameStr + "_ViewHelper")
//                    .addModifiers(PUBLIC, FINAL);
//
//            ClassName uiThreadClassName = ClassName.get("android.support.annotation", "UiThread");
//            ClassName parameterClassName = ClassName.bestGuess(classNameStr);
//            // 组装构造函数: public xxx_ViewBinding(xxx target)
//            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
//                    .addAnnotation(uiThreadClassName)
//                    .addModifiers(PUBLIC)
//                    .addParameter(parameterClassName, "target")
//                    .addParameter(ClassName.get("org.api", "ViewFinder"), "finder", Modifier.FINAL);
//
//            // 添加 target.textView1 = Utils.findViewById(target,R.id.tv1);
//            for (Element bindViewElement : elements) {
//                String filedName = bindViewElement.getSimpleName().toString();
//                int resId = bindViewElement.getAnnotation(ViewById.class).value();
//                constructorBuilder.addStatement("target.$L = finder.findViewById($L)", filedName, resId);
//            }

//            typeSpecBuilder.addMethod(constructorBuilder.build());


//            try {
//                // 写入生成 java 类
//                String packageName = mElementUtils
//                        .getPackageOf(enclosingElement)
//                        .getQualifiedName()
//                        .toString();
//                JavaFile.builder(packageName, typeSpecBuilder.build())
//                        .addFileComment("自动生成代码")
//                        .build()
//                        .writeTo(mFiler);
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("翻车了");
//            }
//        }

    }


    /**
     * 检测Element是否正确 根据ElementKind 判断
     */
    private boolean checkAnnotation(Element element, Class<?> typeClass, ElementKind elementKind) {
        // 判断是否是private的
        if (judgePrivate(element)) {
            error(element, "%s() must can not be private.", typeClass.getSimpleName());
            return false;
        }
        // 判断是否是对应类型
        if (elementKind != element.getKind()) {
            error(element, "%s must be declared on field.", element.getSimpleName());
            return false;
        }
        return true;
    }


    /**
     * error日志
     */
    private void error(Element element, String errorMsg, Object... args) {
        if (args.length > 0) {
            errorMsg = String.format(errorMsg, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, errorMsg, element);
    }

    /**
     * 判断是否是私有的
     */
    private boolean judgePrivate(Element element) {
        return element.getModifiers().contains(java.lang.reflect.Modifier.PRIVATE);
    }


}

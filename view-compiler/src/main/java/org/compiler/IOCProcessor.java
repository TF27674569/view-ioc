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
 * description: �������ڱ���ʱ�ڽ���ɨ��Ľ���
 * <p/>
 * Created by TIAN FENG on 2018/1/25.
 * QQ:27674569
 * Email: 27674569@qq.com
 * Version:1.0
 */
@AutoService(Processor.class) // ���� META-INF ��Ϣ ���ߴ�������Ҫ�������Զ��������������
public class IOCProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;

    /**
     * String һ����������
     * IOCHelper  ������ڵ�������Ϣ
     */
    private Map<String, IOCHelper> mHelpers = new HashMap<>();

    /**
     * ÿһ��ע�⴦�����඼������һ���յĹ��캯����
     * Ȼ����������һ�������init()���������ᱻע�⴦���ߵ��ã�
     * ������ProcessingEnviroment������
     * ProcessingEnviroment�ṩ�ܶ����õĹ�����Elements,Types��Filer
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
     * ָ����Ҫɨ����Щע�⣨�Զ���ģ�
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
     * ����ָ����ʹ�õ�Java�汾
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        /**
         * ������֧�ֵ���߰汾
         */
        return SourceVersion.latestSupported();
    }


    /**
     * �����ڼ� ɨ��ĳ������ �����Լ�������ע���ص��˺���
     *
     * @return true ֹͣ����
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
     * ɨ��ViewByIdע��
     */
    private void scanViewById(RoundEnvironment roundEnv) {
        //  ɨ��ViewByIdע��
        Set<? extends Element> viewByIdElements = roundEnv.getElementsAnnotatedWith(ViewById.class);

        // ע��
        for (Element viewByIdElement : viewByIdElements) {
            // ����ǩ
            if (!checkAnnotation(viewByIdElement, Extra.class, ElementKind.FIELD)) {
                return;
            }
            // �õ��ļ�Elenemt
            VariableElement fieldElement = (VariableElement) viewByIdElement;
            // ����ȫ���� ��ȡ��Ӧ��helper
            IOCHelper helper = addHelperToHelpers(fieldElement);
            // �õ���ǰ���͵�ע�����
            ViewById annotation = fieldElement.getAnnotation(ViewById.class);
            // ��ӵ������࣬���ͳһ����
            helper.viewByIdElements.put(annotation.value(), fieldElement);
        }

    }

    // ɨ��Extra
    private void scanExtra(RoundEnvironment roundEnv) {
        //  ɨ��ViewByIdע��
        Set<? extends Element> extraElements = roundEnv.getElementsAnnotatedWith(Extra.class);

        // ע��
        for (Element extraElement : extraElements) {
            // ����ǩ
            if (!checkAnnotation(extraElement, Extra.class, ElementKind.FIELD)) {
                return;
            }
            // �õ��ļ�Elenemt
            VariableElement fieldElement = (VariableElement) extraElement;
            // ����ȫ���� ��ȡ��Ӧ��helper
            IOCHelper helper = addHelperToHelpers(fieldElement);
            // �õ���ǰ���͵�ע�����
            Extra annotation = fieldElement.getAnnotation(Extra.class);
            // ��ӵ������࣬���ͳһ����
            helper.extraElements.put(annotation.value(), fieldElement);
        }
    }

    /**
     * ɨ��Event����¼�
     */
    private void scanEvent(RoundEnvironment roundEnv) {
        //  ɨ��ViewByIdע��
        Set<? extends Element> eventElements = roundEnv.getElementsAnnotatedWith(Event.class);

        // ע��
        for (Element eventElement : eventElements) {
            // ����ǩ
            if (!checkAnnotation(eventElement, Event.class, ElementKind.METHOD)) {
                return;
            }
            // �õ��ļ�Elenemt
            ExecutableElement methodElement = (ExecutableElement) eventElement;
            // ����ȫ���� ��ȡ��Ӧ��helper
            IOCHelper helper = addHelperToHelpers(methodElement);
            // �õ���ǰ���͵�ע�����
            Event annotation = methodElement.getAnnotation(Event.class);
            // ��ӵ������࣬���ͳһ����
            helper.eventElements.put(annotation.value(), methodElement);
        }
    }


    /**
     * ��ӵ������ಢ���ظ�����
     */
    private IOCHelper addHelperToHelpers(Element element) {
        // �õ����Element
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        // �õ���ǰȫ����
        String qualifileame = classElement.getQualifiedName().toString();
        // ����ȫ���� ��ȡ��Ӧ��helper
        IOCHelper helper = mHelpers.get(qualifileame);
        if (helper == null) {
            helper = new IOCHelper(classElement, mElementUtils);
            mHelpers.put(qualifileame, helper);
        }
        return helper;
    }

    /**
     * ����java�ļ�
     * @return
     */
    private boolean createJavaFile() {
        for (Map.Entry<String, IOCHelper> stringIOCHelperEntry : mHelpers.entrySet()) {
            // ע���������ȫ���� eg:com.compilerdemo.MainActivity
            IOCHelper helper = stringIOCHelperEntry.getValue();

            try {
                System.out.println(helper.getPackageName()+"-------------------------------------");
                JavaFile.builder(helper.getPackageName(), helper.getTypeSpec())
                            .addFileComment("�Զ����ɴ���")
                            .build()
                            .writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void can(RoundEnvironment roundEnv) {

//        // ��ȡViewByIDע������Ӧ��Element
//        Set<? extends Element> viewByIdElements = roundEnv.getElementsAnnotatedWith(ViewById.class);
//        //  ���� ViewById
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
//        // ��ȡEventע������Ӧ��Element
//        Set<? extends Element> eventElements = roundEnv.getElementsAnnotatedWith(Event.class);
//        //  ���� Event
//        Map<Element, List<Element>> eventElementElementMap = new LinkedHashMap<>();
//
//        for (Element eventElement : eventElements) {
//            // ��ȡ������ ��Element
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
//        // ���� java ��
//        for (Map.Entry<Element, List<Element>> entry : viewByIdElementElementMap.entrySet()) {
//            Element enclosingElement = entry.getKey();
//            List<Element> elements = entry.getValue();
//            String classNameStr = enclosingElement.getSimpleName().toString();
//
//            // ��װ��:  xxx_ViewHelpers
//            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classNameStr + "_ViewHelper")
//                    .addModifiers(PUBLIC, FINAL);
//
//            ClassName uiThreadClassName = ClassName.get("android.support.annotation", "UiThread");
//            ClassName parameterClassName = ClassName.bestGuess(classNameStr);
//            // ��װ���캯��: public xxx_ViewBinding(xxx target)
//            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
//                    .addAnnotation(uiThreadClassName)
//                    .addModifiers(PUBLIC)
//                    .addParameter(parameterClassName, "target")
//                    .addParameter(ClassName.get("org.api", "ViewFinder"), "finder", Modifier.FINAL);
//
//            // ��� target.textView1 = Utils.findViewById(target,R.id.tv1);
//            for (Element bindViewElement : elements) {
//                String filedName = bindViewElement.getSimpleName().toString();
//                int resId = bindViewElement.getAnnotation(ViewById.class).value();
//                constructorBuilder.addStatement("target.$L = finder.findViewById($L)", filedName, resId);
//            }

//            typeSpecBuilder.addMethod(constructorBuilder.build());


//            try {
//                // д������ java ��
//                String packageName = mElementUtils
//                        .getPackageOf(enclosingElement)
//                        .getQualifiedName()
//                        .toString();
//                JavaFile.builder(packageName, typeSpecBuilder.build())
//                        .addFileComment("�Զ����ɴ���")
//                        .build()
//                        .writeTo(mFiler);
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("������");
//            }
//        }

    }


    /**
     * ���Element�Ƿ���ȷ ����ElementKind �ж�
     */
    private boolean checkAnnotation(Element element, Class<?> typeClass, ElementKind elementKind) {
        // �ж��Ƿ���private��
        if (judgePrivate(element)) {
            error(element, "%s() must can not be private.", typeClass.getSimpleName());
            return false;
        }
        // �ж��Ƿ��Ƕ�Ӧ����
        if (elementKind != element.getKind()) {
            error(element, "%s must be declared on field.", element.getSimpleName());
            return false;
        }
        return true;
    }


    /**
     * error��־
     */
    private void error(Element element, String errorMsg, Object... args) {
        if (args.length > 0) {
            errorMsg = String.format(errorMsg, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, errorMsg, element);
    }

    /**
     * �ж��Ƿ���˽�е�
     */
    private boolean judgePrivate(Element element) {
        return element.getModifiers().contains(java.lang.reflect.Modifier.PRIVATE);
    }


}

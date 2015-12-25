package personal.tianjie.rpc.util;

import com.google.common.collect.Sets;
import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tianjie on 4/4/15.
 */
public final class SystemUtil {
    private static Logger LOGGER = LoggerFactory.getLogger(SystemUtil.class);
    private static final String[] EMPTY_STRING_ARRAY = new String[]{};
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[]{};

    private SystemUtil() {
    }

    public static Set<String> scanAnnotationObject(
            final Class<? extends Annotation> clazz,
            String... packages) {

        final Set<String> set = Sets.newLinkedHashSet();
        for (String pkg : packages) {
            AnnotationDetector annotationDetector = new AnnotationDetector(
                    new TypeReporter() {
                        public Class<? extends Annotation>[] annotations() {
                            return new Class[]{clazz};
                        }

                        public void reportTypeAnnotation(
                                Class<? extends Annotation> annotation,
                                String className) {
                            set.add(className);
                        }
                    });
            try {
                annotationDetector.detect(pkg);
            } catch (IOException e) {
                LOGGER.error("扫描RpcService包[{}]出错", pkg, e);
            }
        }
        return set;
    }

    /**
     * 获取classLoader
     *
     * @param clazz
     * @return
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        if (clazz != null && clazz.getClassLoader() != null) {
            return clazz.getClassLoader();
        }

        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        }

        return ClassLoader.getSystemClassLoader();
    }

    /**
     * 获取class的完整名字
     *
     * @param classes
     * @return
     */
    public static String[] getClassFullName(Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] classNames = new String[classes.length];
        for (int x = 0; x < classes.length; x++) {
            classNames[x] = classes[x].getName();
        }
        return classNames;
    }

    /**
     * 获取实例的class数组
     *
     * @param values
     * @return
     */
    public static Class<?>[] getClassType(Object... values) {
        if (values == null || values.length == 0) {
            return EMPTY_CLASS_ARRAY;
        }

        Class[] classTypes = new Class[values.length];
        for (int i = 0; i < values.length; i++) {
            classTypes[i] = values[i].getClass();
        }
        return classTypes;
    }

    public static Class<?>[] getClassTypeByName(ClassLoader cl,
                                                String... names)
            throws ClassNotFoundException {
        if (names == null || names.length == 0) {
            return EMPTY_CLASS_ARRAY;
        }

        Class[] classTypes = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            Matcher matcher = arrayMatcher(name);
            if (matcher.find()) {
                classTypes[i] = getArrayClass(cl, matcher);
            } else {
                classTypes[i] = cl.loadClass(names[i]);
            }
        }
        return classTypes;
    }

    private static final Pattern PATTERN = Pattern
            .compile("^(\\[+)(B|C|D|F|I|J|S|Z|L)([^;]*);{0,1}");
    private final static int ARR_FLAG_IDX = 1,
            CLASS_FLAG_IDX = 2,
            CLASS_IDX = 3;

    private static Matcher arrayMatcher(String name) {
        return PATTERN.matcher(name);
    }

    private static Class getArrayClass(ClassLoader cl, Matcher matcher)
            throws ClassNotFoundException {
        String arrayFlag = matcher.group(ARR_FLAG_IDX);
        String classFlag = matcher.group(CLASS_FLAG_IDX);
        String className;
        Class clz;
        //基础数据类型的CLASS
        if (!"L".equals(classFlag)) {
            clz = primitiveClass(classFlag.charAt(0));
        } else {
            //引用类
            className = matcher.group(CLASS_IDX);
            clz = cl.loadClass(className);
        }
        //创建数组
        Object o = null;
        for (int i = 0; i < arrayFlag.length(); i++) {

            Class clazz;
            if (o == null) {
                clazz = clz;
            } else {
                clazz = o.getClass();
            }
            o = Array.newInstance(clazz, 0);
        }
        return o.getClass();
    }

    /**
     * @param c
     * @return
     * @see <code>http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3</code>
     *
     */
    private static Class primitiveClass(char c) {
        switch (c) {
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'D':
                return double.class;
            case 'F':
                return float.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'S':
                return short.class;
            case 'Z':
                return boolean.class;
            default:
                return null;
        }
    }

}

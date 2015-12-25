import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 15-9-12.
 */
public class ClassTypeTest {

    public static void main(String... args) throws Exception {
        Object target = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{TargetInterface.class},
                new TargetHandler());

        ((TargetInterface) target).targetMethod(
                null, null, null, null, null, null, null, null, null);

    }
}

interface TargetInterface {
    void targetMethod(int[] i, long[] l, Integer[] I, Long[] L, Byte[] B, byte[] b, Object[] o, Character[] C, char[] c);
}

class TargetHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] classes = method.getParameterTypes();
        for (Class<?> clazz : classes) {
            System.out.println(clazz.getName());
        }
        return null;
    }
}

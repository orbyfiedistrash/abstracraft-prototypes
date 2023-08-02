package test.abstracraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.redstone.abstracraft.core.ClassUtil;
import tools.redstone.abstracraft.core.RawOptionalMethod;
import tools.redstone.abstracraft.core.Required;
import tools.redstone.abstracraft.core.RequiredMethodFinder;

public class BytecodeAnalysisTest {

    static class MyMethod implements RawOptionalMethod {
        void call(Object a, int b) {

        }
    }

    static class MyMethod2 implements RawOptionalMethod {
        void call() { }
    }

    public static final MyMethod myMethod = new MyMethod();

    @Required(MyMethod2.class)
    class TestFeatureLikeClass {
        void abc() {
            myMethod.call("abc", 4);
        }
    }

    @Test
    void test_GetRequiredDependencies() {
        Class<?> loadedFeature = ClassUtil.transformAndLoad(ClassUtil.directClassLoader(),
                TestFeatureLikeClass.class, RequiredMethodFinder::transformClass);

        Assertions.assertTrue(loadedFeature.isAnnotationPresent(Required.class));
        Assertions.assertArrayEquals(new Class[] { MyMethod2.class, MyMethod.class },
                loadedFeature.getAnnotation(Required.class).value());
    }

}

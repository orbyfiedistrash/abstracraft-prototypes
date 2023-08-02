package test.abstracraft;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.redstone.abstracraft.core.*;

import java.lang.reflect.Field;
import java.util.List;

public class BytecodeAnalysisTest {

    interface ImplementedMethod extends RawOptionalMethod {
        @Override
        default boolean isImplemented() {
            return true;
        }
    }

    static class MyMethod implements ImplementedMethod {
        void call(Object a, int b) {

        }
    }

    static class MyMethod2 implements ImplementedMethod {
        void call() { }
    }

    public static final MyMethod myMethod = new MyMethod();

    @Required(MyMethod2.class)
    static class TestFeatureLikeClass {
        void abc() {
            myMethod.call("abc", 4);
        }
    }

    @Test
    void test_GetRequiredDependencies() {
        var finder = new RequiredMethodFinder();
        var list = finder.findRequiredMethodsForClass(TestFeatureLikeClass.class);
        for (var dependency : list) {
            Field f = dependency.getField();
            // forces the modifiers of the field to conform to PUBLIC | ~FINAL
            ReflectUtil.forceAccessible(f);

            // check if the method is set
            if (ReflectUtil.<RawOptionalMethod>getFieldValue(null, f).isImplemented()) {
                // ...
            }

            // set the value of the field (requires forceAccessible for final fields)
            ReflectUtil.setFieldValue(null, f, null);
        }
    }

}

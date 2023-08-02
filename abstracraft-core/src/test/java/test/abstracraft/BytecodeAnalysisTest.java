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

    public static final MyMethod myMethod = new MyMethod();

    static class TestFeatureLikeClass {
        void abc() {
            myMethod.call("abc", 4);
        }
    }

    @Test
    void test_GetRequiredDependencies() {
        var finder = new RequiredMethodFinder();
        var list = finder.findRequiredMethodsForClass(TestFeatureLikeClass.class)
                .stream()
                .map(dep -> dep.getField().getDeclaringClass().getSimpleName() + "." + dep.getField().getName())
                .toList();

        Assertions.assertArrayEquals(new String[] {"BytecodeAnalysisTest.myMethod"}, list.toArray(new String[0]));
    }

}

package test.abstracraft.core;

import org.junit.jupiter.api.Assertions;
import tools.redstone.abstracraft.HandleAbstraction;
import tools.redstone.abstracraft.adapter.Adapter;
import tools.redstone.abstracraft.adapter.AdapterRegistry;
import tools.redstone.abstracraft.usage.Abstraction;

public class AdapterHookTest {

    public static void main(String[] args) {
        TestSystem.runTests(AdapterHookTest.class, true);
    }

    public interface A extends Abstraction { B getB(); }
    public interface B extends Abstraction { String hello(); }
    public static class InternalA { public InternalB getB() { return new InternalB(); } }
    public static class InternalB { public final String hello = "HELLO"; }

    public static class AImpl extends HandleAbstraction<InternalA> implements A {
        public AImpl(InternalA handle) {
            super(handle);
        }

        @Override
        public B getB() {
            return adapt(handle().getB());
        }
    }

    public static class BImpl extends HandleAbstraction<InternalB> implements B {
        public BImpl(InternalB handle) {
            super(handle);
        }

        @Override
        public String hello() {
            return handle().hello;
        }
    }

    static class test_AdapterHooks {
        void run() {
            A a = new AImpl(new InternalA());
            B b = a.getB();

            Assertions.assertEquals(b.hello(), "HELLO");
        }
    }

    @TestSystem.Test
    void test_AdapterHooks(TestSystem.TestInterface itf) {
        // setup adapters
        AdapterRegistry.getInstance().register(Adapter.handle(A.class, InternalA.class));
        AdapterRegistry.getInstance().register(Adapter.handle(B.class, InternalB.class));

        // execute run
        itf.runTransformed("run");
    }

}

package tools.redstone.abstracraft.core;

import org.objectweb.asm.*;

import java.util.*;

/**
 * Analyzes bytecode of a class to determine the methods required.
 *
 * @author orbyfied
 */
public class RequiredMethodFinder {

    static Map<String, Boolean> isMethodInterfaceCache = new HashMap<>();

    /**
     * Checks whether the class by the given name implements
     * {@link RawOptionalMethod}.
     *
     * @param name The class name.
     * @return Whether it is a method interface.
     */
    static boolean isMethodInterface(String name) {
        Boolean b = isMethodInterfaceCache.get(name);
        if (b != null)
            return b;

        try {
            isMethodInterfaceCache.put(name, b = RawOptionalMethod.class.isAssignableFrom(
                    Class.forName(name.replace('/', '.'))));
            return b;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public RequiredMethodFinder() { }

    // Packed ASM method info
    record MethodInfo(int access, String name, String desc, String signature, String[] exceptions) { }

    // Represents a value retrieved from a field on the stack
    record FieldValue(Type fieldType, String owner, String name) { }

    /**
     * Find all method dependencies required by the given class.
     *
     * @return The list of dependencies.
     */
    public List<MethodDependency> findRequiredMethodsForClass(Class<?> klass) {
        return findRequiredMethodsForClass(new ClassReader(ReflectUtil.getBytes(klass)));
    }

    /**
     * Find all method dependencies required by the given class bytes.
     *
     * @return The list of dependencies.
     */
    public List<MethodDependency> findRequiredMethodsForClass(ClassReader classReader) {
        List<MethodDependency> list = new ArrayList<>();
        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return findRequiredMethodsForMethod(list,
                        new MethodInfo(access, name, desc, signature, exceptions));
            }
        }, 0);

        return list;
    }

    /**
     * Find all dependencies required by the given method.
     *
     * @param list The list of dependencies to output to.
     */
    public MethodVisitor findRequiredMethodsForMethod(List<MethodDependency> list, MethodInfo methodInfo) {
        return new MethodVisitor(Opcodes.ASM9) {
            /* Track the current method interfaces on the compute stack */
            Stack<Object> methodInterfaceStack = new Stack<>();

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                var type = Type.getType(descriptor);
                if ((opcode == Opcodes.GETSTATIC || opcode == Opcodes.GETFIELD) &&
                        isMethodInterface(type.getInternalName())) {
                    methodInterfaceStack.push(new FieldValue(Type.getType(descriptor), owner, name));
                }
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if (isMethodInterface(owner) && "call".equals(name)) {
                    var field = (FieldValue) methodInterfaceStack.pop();
                    list.add(new MethodDependency(field.owner, field.name));
                }
            }
        };
    }

}

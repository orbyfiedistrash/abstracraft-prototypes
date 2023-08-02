package tools.redstone.abstracraft.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A utility class for transforming and loading classes.
 */
public class ReflectUtil {
    private ReflectUtil() { }

    static final Map<String, Class<?>> forNameCache = new HashMap<>();

    // The sun.misc.Unsafe instance
    static final Unsafe UNSAFE;

    /* Method handles for cracking  */
    static final MethodHandle SETTER_Field_modifiers;
    static final MethodHandles.Lookup INTERNAL_LOOKUP;

    static {
        try {
            // get using reflection
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
        } catch (Exception e) {
            // rethrow error
            throw new ExceptionInInitializerError(e);
        }

        try {
            {
                // get lookup
                Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                MethodHandles.publicLookup();
                INTERNAL_LOOKUP = (MethodHandles.Lookup)
                        UNSAFE.getObject(
                                UNSAFE.staticFieldBase(field),
                                UNSAFE.staticFieldOffset(field)
                        );
            }

            SETTER_Field_modifiers  = INTERNAL_LOOKUP.findSetter(Field.class, "modifiers", Integer.TYPE);
        } catch (Throwable t) {
            // throw exception in init
            throw new ExceptionInInitializerError(t);
        }
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Get the loaded class by the given name.
     *
     * @param name The class name.
     * @throws IllegalArgumentException If no class by that name exists.
     * @return The class.
     */
    public static Class<?> getClass(String name) {
        Class<?> klass = forNameCache.get(name);
        if (klass != null)
            return klass;

        try {
            forNameCache.put(name, klass = Class.forName(name));
            return klass;
        } catch (Exception e) {
            throw new IllegalArgumentException("No class by name '" + name + "'", e);
        }
    }

    /**
     * Get the bytes of the class file of the given loaded class.
     *
     * @param klass The class.
     * @return The bytes.
     */
    public static byte[] getBytes(Class<?> klass) {
        try {
            // get resource path
            String className = klass.getName();
            String classAsPath = className.replace('.', '/') + ".class";

            // open resource
            try (InputStream stream = klass.getClassLoader().getResourceAsStream(classAsPath)) {
                if (stream == null)
                    throw new IllegalArgumentException("Could not find resource stream for " + klass);
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred", e);
        }
    }

    /**
     * Set the modifiers of the given field.
     *
     * @param f The field.
     * @param mods The modifiers.
     */
    public static void setModifiers(Field f, int mods) {
        try {
            SETTER_Field_modifiers.invoke(f, mods);
        } catch (Throwable t) {
            throw new IllegalStateException("Failed to set modifiers of " + f, t);
        }
    }

    /**
     * Force the given field to be accessible, removing
     * any access modifiers and the final modifier.
     *
     * @param f The field.
     * @return The field back.
     */
    public static Field forceAccessible(Field f) {
        int mods = f.getModifiers();
        mods &= ~Modifier.PRIVATE;
        mods &= ~Modifier.PROTECTED;
        mods &= ~Modifier.FINAL;
        mods |= Modifier.PUBLIC;
        setModifiers(f, mods);
        return f;
    }

    /**
     * Get the value of the given field.
     *
     * @param on The instance.
     * @param f The field.
     * @param <T> The value type.
     * @return The value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object on, Field f) {
        try {
            return (T) f.get(on);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get field value of " + f, e);
        }
    }

    /**
     * Get the value of the given field.
     *
     * @param on The instance.
     * @param f The field.
     * @param value The value to set.
     */
    public static void setFieldValue(Object on, Field f, Object value) {
        try {
            f.set(on, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set field value of " + f, e);
        }
    }

}

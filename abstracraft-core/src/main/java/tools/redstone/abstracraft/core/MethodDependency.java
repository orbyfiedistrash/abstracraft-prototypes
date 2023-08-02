package tools.redstone.abstracraft.core;

import java.lang.reflect.Field;

// Represents a dependency found to be required by the program
public class MethodDependency {

    final String fieldOwner; // The name of the owner of the field
    final String fieldName;  // The name of the field
    Field field;             // The cached reflection field

    public MethodDependency(String fieldOwner, String fieldName) {
        this.fieldOwner = fieldOwner;
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldOwner() {
        return fieldOwner;
    }

    public Field getField() {
        if (field != null)
            return field;

        try {
            return this.field = ReflectUtil.getClass(fieldOwner).getDeclaredField(fieldName);
        } catch (Exception e) {
            return this.field = null;
        }
    }

}

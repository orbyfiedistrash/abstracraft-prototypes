package tools.redstone.abstracraft.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
    Class<?>[] value();
}

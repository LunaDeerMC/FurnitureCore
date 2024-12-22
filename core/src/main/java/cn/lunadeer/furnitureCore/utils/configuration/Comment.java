package cn.lunadeer.furnitureCore.utils.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for adding single line comments to configuration fields.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
    String value();
}

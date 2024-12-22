package cn.lunadeer.furnitureCore.utils.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for adding multiple lines of comments to configuration fields.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Comments {
    String[] value();
}

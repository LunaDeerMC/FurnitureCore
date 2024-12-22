package cn.lunadeer.furnitureCore.utils.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for adding comments to configuration fields.
 * <p>
 * If you need multiple lines of comments use \n to separate them.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
    String value();
}

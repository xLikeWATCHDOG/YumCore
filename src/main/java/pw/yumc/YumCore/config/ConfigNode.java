package pw.yumc.YumCore.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specializes a non-default path for config node
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigNode {

    /**
     * @return 是否允许为空
     */
    boolean notNull() default false;

    /**
     * Defines the path to the node if it has another as the variable name.
     * Every indention is separated with an dot ('.')
     *
     * @return the path to the node
     */
    String path() default "";
}

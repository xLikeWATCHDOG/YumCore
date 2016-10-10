package pw.yumc.YumCore.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义参数
 *
 * @since 2016年7月23日 上午9:00:27
 * @author 喵♂呜
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValue {
    /**
     * @return 键
     */
    String key();

    /**
     * @return 值
     */
    String value() default "";
}
package pw.yumc.YumCore.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数限制
 *
 * @since 2016年7月23日 上午9:00:27
 * @author 喵♂呜
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {
    /**
     * @return 最大长度(最大值)
     */
    int max() default 255;

    /**
     * @return 最小长度(或最小值)
     */
    int min();
}
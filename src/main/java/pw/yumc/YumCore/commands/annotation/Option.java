package pw.yumc.YumCore.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展选项注解
 *
 * @since 2016年7月23日 上午9:00:07
 * @author 喵♂呜
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    String value();
}

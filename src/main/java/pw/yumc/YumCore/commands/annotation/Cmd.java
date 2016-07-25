package pw.yumc.YumCore.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令注解
 *
 * <pre>
 * 参数名称            描述       默认值
 * name              命令名称     方法名称
 * aliases           命令别名
 * minimumArguments  最小参数     默认0
 * permission        权限
 * onlyPlayer        只允许玩家   false
 * onlyConsole       只允许控制台 false
 * </pre>
 *
 * @since 2016年7月23日 上午8:59:05
 * @author 喵♂呜
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {
    /**
     * @return 命令别名
     */
    String[] aliases() default "";

    /**
     * @return 命令最小参数
     */
    int minimumArguments() default 0;

    /**
     * @return 命令名称
     */
    String value() default "";

    /**
     * @return 只允许控制台执行
     */
    boolean onlyConsole() default false;

    /**
     * @return 只允许玩家执行
     */
    boolean onlyPlayer() default false;

    /**
     * @return 当前命令权限
     */
    String permission() default "";
}

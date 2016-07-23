package pw.yumc.YumCore.commands.annotation;

/**
 *
 * @since 2016年7月23日 上午9:04:56
 * @author 喵♂呜
 */
public @interface Sort {
    /**
     * @return 命令排序
     */
    int sort() default 50;
}

package pw.yumc.YumCore.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

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
     * @return 命令执行者
     */
    Executor[] executor() default Executor.ALL;

    /**
     * @return 命令最小参数
     */
    int minimumArguments() default 0;

    /**
     * @deprecated 详见 {@link Executor}
     * @return 只允许控制台执行
     */
    @Deprecated
    boolean onlyConsole() default false;

    /**
     * @deprecated 详见 {@link Executor}
     * @return 只允许玩家执行
     */
    @Deprecated
    boolean onlyPlayer() default false;

    /**
     * @return 当前命令权限
     */
    String permission() default "";

    /**
     * @return 命令名称
     */
    String value() default "";

    /**
     * 命令执行者
     *
     * @author 喵♂呜
     * @since 2016年8月26日 下午8:55:15
     */
    public enum Executor {
        /**
         * 玩家
         */
        PLAYER {
            @Override
            public String getName() {
                return "玩家";
            }
        },
        /**
         * 控制台
         */
        CONSOLE {
            @Override
            public String getName() {
                return "控制台";
            }
        },
        /**
         * 命令方块
         */
        BLOCK {
            @Override
            public String getName() {
                return "命令方块";
            }
        },
        /**
         * 命令矿车
         */
        COMMANDMINECART {
            @Override
            public String getName() {
                return "命令矿车";
            }
        },
        /**
         * 远程控制台
         */
        REMOTECONSOLE {
            @Override
            public String getName() {
                return "远程控制台";
            }
        },
        /**
         * 所有
         */
        ALL {
            @Override
            public String getName() {
                return "所有执行者";
            }
        },
        /**
         * 未知
         */
        UNKNOW {
            @Override
            public String getName() {
                return "未知";
            }
        };
        /**
         * 解析Executor
         *
         * @param sender
         *            命令执行者
         * @return {@link Executor}
         */
        public static Executor valueOf(final CommandSender sender) {
            if (sender instanceof Player) {
                return Executor.PLAYER;
            } else if (sender instanceof ConsoleCommandSender) {
                return Executor.CONSOLE;
            } else if (sender instanceof BlockCommandSender) {
                return Executor.BLOCK;
            } else if (sender instanceof CommandMinecart) {
                return Executor.COMMANDMINECART;
            } else if (sender instanceof RemoteConsoleCommandSender) {
                return Executor.REMOTECONSOLE;
            } else {
                return Executor.UNKNOW;
            }
        }

        /**
         * @return 执行者名称
         */
        public abstract String getName();
    }
}

package pw.yumc.YumCore.commands;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pw.yumc.YumCore.commands.exception.CommandException;
import pw.yumc.YumCore.commands.exception.CommandParseException;

/**
 * 命令参数解析
 *
 * @author 喵♂呜
 * @since 2016年10月5日 下午4:02:04
 */
public class CommandParse {
    private static Map<Class, Parse> allparses = new HashMap<>();
    static {
        new IntegerParse();
        new LongParse();
        new BooleanParse();
        new PlayerParse();
        new StringParse();
    }
    public List<Parse> parse = new LinkedList<>();
    public List<Object> parsed = new LinkedList<>();

    public CommandParse(final Class[] classes, final Annotation[][] annons) {
        for (int i = 0; i < classes.length; i++) {
            final Class clazz = classes[i];
            final Annotation[] annotations = annons[i];
            if (clazz.isAssignableFrom(CommandSender.class)) {
                continue;
            }
            if (!allparses.containsKey(clazz)) {
                throw new CommandParseException(String.format("无法解析的参数类型 %s !", clazz.getName()));
            }
            final Parse parse = allparses.get(clazz).clone();
            for (final Annotation annotation : annotations) {
                if (annotation.annotationType() == Default.class) {
                    parse.setAttr("default", ((Default) annotation).value());
                } else if (annotation.annotationType() == Limit.class) {
                    parse.setAttr("min", ((Limit) annotation).min());
                    parse.setAttr("max", ((Limit) annotation).max());
                } else if (annotation.annotationType() == KeyValue.class) {
                    final KeyValue kv = (KeyValue) annotation;
                    parse.setAttr(kv.key(), kv.value());
                }
            }
            this.parse.add(parse);
        }
    }

    public static CommandParse get(final Method method) {
        return new CommandParse(method.getParameterTypes(), method.getParameterAnnotations());
    }

    public static void registerParse(final Class clazz, final Parse parse) {
        allparses.put(clazz, parse);
    }

    public Object[] parse(final CommandArgument cmdArgs) {
        final String args[] = cmdArgs.getArgs();
        if (args.length == 0) {
            return null;
        }
        final List<Object> pobjs = new LinkedList<>();
        pobjs.add(cmdArgs.getSender());
        for (int i = 0; i < args.length; i++) {
            try {
                if (i < parse.size()) {
                    pobjs.add(parse.get(i).parse(args[i]));
                }
            } catch (final Exception e) {
                throw new CommandException(String.format("第 %s 个参数 ", i + 1) + e.getMessage());
            }
        }
        return pobjs.toArray();
    }

    public static class BooleanParse extends Parse<Boolean> {
        public BooleanParse() {
            allparses.put(Boolean.class, this);
            allparses.put(boolean.class, this);
        }

        @Override
        public Boolean parse(final String arg) {
            try {
                return Boolean.parseBoolean(arg);
            } catch (final Exception e) {
                throw new CommandParseException("必须为True或者False!");
            }
        }
    }

    /**
     * 默认参数
     *
     * @since 2016年7月23日 上午9:00:27
     * @author 喵♂呜
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        String value();
    }

    public static class IntegerParse extends Parse<Integer> {
        public IntegerParse() {
            allparses.put(Integer.class, this);
            allparses.put(int.class, this);
        }

        @Override
        public Integer parse(final String arg) {
            try {
                final int result = Integer.parseInt(arg);
                if (min > result || result > max) {
                    throwRange();
                }
                return result;
            } catch (final Exception e) {
                throw new CommandParseException("必须为数字!");
            }
        }
    }

    /**
     * 自定义参数
     *
     * @since 2016年7月23日 上午9:00:27
     * @author 喵♂呜
     */
    @Target(ElementType.METHOD)
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

    public static class LongParse extends Parse<Long> {
        public LongParse() {
            allparses.put(Long.class, this);
            allparses.put(long.class, this);
        }

        @Override
        public Long parse(final String arg) {
            try {
                final long result = Long.parseLong(arg);
                if (min > result || result > max) {
                    throwRange();
                }
                return result;
            } catch (final Exception e) {
                throw new CommandParseException("必须为数字!");
            }
        }
    }

    public static abstract class Parse<RT> implements Cloneable {
        protected Object def;
        protected Map<String, Object> attrs = new HashMap<>();
        protected int min;
        protected int max;

        @Override
        public Parse<RT> clone() {
            try {
                return (Parse<RT>) super.clone();
            } catch (final CloneNotSupportedException e) {
                return null;
            }
        }

        public Object getDefault() {
            return def;
        }

        public void load() {
            def = attrs.get("default");
            min = (int) attrs.get("min");
            max = (int) attrs.get("max");
        }

        public abstract RT parse(String arg) throws CommandParseException;

        public void setAttr(final String name, final Object value) {
            attrs.put(name, value);
        }

        public void throwRange() {
            throwRange(null);
        }

        public void throwRange(final String str) {
            throw new CommandException(String.format(str == null ? "必须在 %s 到 %s之间!" : str, min, max));
        }
    }

    public static class PlayerParse extends Parse<Player> {
        public PlayerParse() {
            allparses.put(Player.class, this);
        }

        @Override
        public Player parse(final String arg) {
            final Player p = Bukkit.getPlayerExact(arg);
            if (attrs.containsKey("check") && p == null) {
                throw new CommandParseException("玩家 " + arg + "不存在或不在线!");
            }
            return p;
        }
    }

    public static class StringParse extends Parse<String> {
        public StringParse() {
            allparses.put(String.class, this);
        }

        @Override
        public String parse(final String arg) {
            if (min > arg.length() || arg.length() > max) {
                throwRange("长度必须在 %s 和 %s 之间!");
            }
            return arg;
        }
    }
}

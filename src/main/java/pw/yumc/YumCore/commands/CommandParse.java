package pw.yumc.YumCore.commands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.commands.annotation.Default;
import pw.yumc.YumCore.commands.annotation.KeyValue;
import pw.yumc.YumCore.commands.annotation.Limit;
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
    private final List<Parse> parse = new LinkedList<>();

    private final boolean isMain;

    public CommandParse(final Class[] classes, final Annotation[][] annons, final boolean isMain) {
        this.isMain = isMain;
        for (int i = 0; i < classes.length; i++) {
            final Class clazz = classes[i];
            final Annotation[] annotations = annons[i];
            if (clazz.isAssignableFrom(CommandSender.class)) {
                continue;
            }
            Parse parse = allparses.get(clazz);
            if (parse == null) {
                if (!clazz.isEnum()) {
                    throw new CommandParseException(String.format("存在无法解析的参数类型 %s", clazz.getName()));
                }
                parse = new EnumParse(clazz);
            }
            this.parse.add(parse.clone().parseAnnotation(annotations));
        }
    }

    public static CommandParse get(final Method method) {
        return new CommandParse(method.getParameterTypes(), method.getParameterAnnotations(), method.getReturnType().equals(boolean.class));
    }

    public static void registerParse(final Class clazz, final Parse parse) {
        allparses.put(clazz, parse);
    }

    public Object[] parse(final CommandArgument cmdArgs) {
        final String args[] = cmdArgs.getArgs();
        final List<Object> pobjs = new LinkedList<>();
        pobjs.add(cmdArgs.getSender());
        for (int i = 0; i < parse.size(); i++) {
            try {
                final Parse p = parse.get(i);
                final String param = i < args.length ? args[i] : p.def;
                pobjs.add(param == null ? param : p.parse(param));
            } catch (final Exception e) {
                Log.debug(e);
                throw new CommandParseException(String.format("第 %s 个参数 ", isMain ? 1 : 2 + i) + e.getMessage());
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
                throw new CommandParseException("必须为True或者False!", e);
            }
        }
    }

    public static class EnumParse extends Parse<Enum> {
        Class<Enum> etype;
        Enum[] elist;

        public EnumParse(final Class<Enum> etype) {
            this.etype = etype;
            this.elist = etype.getEnumConstants();
        }

        @Override
        public Enum parse(final String arg) {
            try {
                return Enum.valueOf(etype, arg);
            } catch (final IllegalArgumentException ex) {
                throw new CommandParseException(String.format("不是 %s 有效值为 %s", etype.getSimpleName(), Arrays.toString(elist)));
            }
        }
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
            } catch (final NumberFormatException e) {
                throw new CommandParseException("必须为数字!", e);
            }
        }
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
            } catch (final NumberFormatException e) {
                throw new CommandParseException("必须为数字!", e);
            }
        }
    }

    public static class MaterialParse extends Parse<Material> {
        public MaterialParse() {
            allparses.put(String.class, this);
        }

        @Override
        public Material parse(final String arg) {
            try {
                return Material.valueOf(arg);
            } catch (final Exception e) {
                throw new CommandParseException("玩家 " + arg + "不存在或不在线!");
            }
        }
    }

    public static abstract class Parse<RT> implements Cloneable {
        protected String def;
        protected Map<String, String> attrs = new HashMap<>();
        protected int min = 0;
        protected int max = Integer.MAX_VALUE;

        @Override
        public Parse<RT> clone() {
            try {
                return (Parse<RT>) super.clone();
            } catch (final CloneNotSupportedException e) {
                return null;
            }
        }

        public String getDefault() {
            return def;
        }

        public abstract RT parse(String arg) throws CommandParseException;

        public Parse<RT> parseAnnotation(final Annotation[] annotations) {
            for (final Annotation annotation : annotations) {
                if (annotation.annotationType() == Default.class) {
                    def = ((Default) annotation).value();
                } else if (annotation.annotationType() == Limit.class) {
                    min = ((Limit) annotation).min();
                    max = ((Limit) annotation).max();
                } else if (annotation.annotationType() == KeyValue.class) {
                    final KeyValue kv = (KeyValue) annotation;
                    attrs.put(kv.key(), kv.value());
                }
            }
            return this;
        }

        public void throwRange() {
            throwRange(null);
        }

        public void throwRange(final String str) {
            throw new CommandException(String.format(str == null ? "必须在 %s 到 %s 之间!" : str, min, max));
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
        List<String> options;

        public StringParse() {
            allparses.put(String.class, this);
            if (attrs.containsKey("option")) {

            }
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

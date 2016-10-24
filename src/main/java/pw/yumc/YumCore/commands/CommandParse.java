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
    private List<Parse> parse = new LinkedList<>();
    private boolean isMain;

    public CommandParse(Class[] classes, Annotation[][] annons, boolean isMain) {
        this.isMain = isMain;
        // 第一个参数实现了CommandSender忽略
        for (int i = 1; i < classes.length; i++) {
            Class clazz = classes[i];
            Annotation[] annotations = annons[i];
            Parse parse = allparses.get(clazz);
            if (clazz.isEnum()) {
                parse = new EnumParse(clazz);
            }
            if (parse == null) { throw new CommandParseException(String.format("存在无法解析的参数类型 %s", clazz.getName())); }
            this.parse.add(parse.clone().parseAnnotation(annotations));
        }
    }

    public static CommandParse get(Method method) {
        return new CommandParse(method.getParameterTypes(), method.getParameterAnnotations(), method.getReturnType().equals(boolean.class));
    }

    /**
     * 转化数组为字符串
     *
     * @param arr
     *            数组
     * @param split
     *            分割符
     * @return 字符串
     */
    public static String join(Object[] arr, String split) {
        StringBuffer str = new StringBuffer();
        for (Object s : arr) {
            str.append(s.toString());
            str.append(split);
        }
        return str.length() > split.length() ? str.toString().substring(0, str.length() - split.length()) : str.toString();
    }

    public static void register(Class clazz, Parse parse) {
        allparses.put(clazz, parse);
    }

    public Object[] parse(CommandArgument cmdArgs) {
        String args[] = cmdArgs.getArgs();
        List<Object> pobjs = new LinkedList<>();
        pobjs.add(cmdArgs.getSender());
        for (int i = 0; i < parse.size(); i++) {
            try {
                Parse p = parse.get(i);
                String param = i < args.length ? args[i] : p.def;
                // 参数大于解析器 并且为最后一个参数
                if (i + 1 == parse.size() && args.length >= parse.size()) {
                    param = join(Arrays.copyOfRange(args, i, args.length), " ");
                }
                pobjs.add(param == null ? null : p.parse(cmdArgs.getSender(), param));
            } catch (Exception e) {
                throw new CommandParseException(String.format("第 %s 个参数 ", isMain ? 1 : 2 + i) + e.getMessage());
            }
        }
        Log.d("解析参数: %s => %s", Arrays.toString(args), pobjs);
        return pobjs.toArray();
    }

    public static class BooleanParse extends Parse<Boolean> {
        public BooleanParse() {
            allparses.put(Boolean.class, this);
            allparses.put(boolean.class, this);
        }

        @Override
        public Boolean parse(CommandSender sender, String arg) {
            try {
                return Boolean.parseBoolean(arg);
            } catch (Exception e) {
                throw new CommandParseException("必须为True或者False!", e);
            }
        }
    }

    public static class EnumParse extends Parse<Enum> {
        Class<Enum> etype;
        Enum[] elist;

        public EnumParse(Class<Enum> etype) {
            this.etype = etype;
            this.elist = etype.getEnumConstants();
        }

        @Override
        public Enum parse(CommandSender sender, String arg) {
            try {
                return Enum.valueOf(etype, arg);
            } catch (IllegalArgumentException ex) {
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
        public Integer parse(CommandSender sender, String arg) {
            try {
                int result = Integer.parseInt(arg);
                if (min > result || result > max) {
                    throwRange();
                }
                return result;
            } catch (NumberFormatException e) {
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
        public Long parse(CommandSender sender, String arg) {
            try {
                long result = Long.parseLong(arg);
                if (min > result || result > max) {
                    throwRange();
                }
                return result;
            } catch (NumberFormatException e) {
                throw new CommandParseException("必须为数字!", e);
            }
        }
    }

    public static class MaterialParse extends Parse<Material> {
        public MaterialParse() {
            allparses.put(String.class, this);
        }

        @Override
        public Material parse(CommandSender sender, String arg) {
            try {
                return Material.valueOf(arg);
            } catch (Exception e) {
                throw new CommandParseException(String.format("%s 不是一个有效的Material枚举", arg), e);
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
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        public String getDefault() {
            return def;
        }

        public abstract RT parse(CommandSender sender, String arg) throws CommandParseException;

        public Parse<RT> parseAnnotation(Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Default.class) {
                    def = ((Default) annotation).value();
                } else if (annotation.annotationType() == Limit.class) {
                    min = ((Limit) annotation).min();
                    max = ((Limit) annotation).max();
                } else if (annotation.annotationType() == KeyValue.class) {
                    KeyValue kv = (KeyValue) annotation;
                    attrs.put(kv.key(), kv.value());
                }
            }
            return this;
        }

        public void throwException(String str, Object... objects) {
            throw new CommandParseException(String.format(str, objects));
        }

        public void throwRange() {
            throwRange(null);
        }

        public void throwRange(String str) {
            throw new CommandParseException(String.format(str == null ? "范围必须在 %s 到 %s 之间!" : str, min, max));
        }
    }

    public static class PlayerParse extends Parse<Player> {
        public PlayerParse() {
            allparses.put(Player.class, this);
        }

        @Override
        public Player parse(CommandSender sender, String arg) {
            Player p = Bukkit.getPlayerExact(arg);
            if (attrs.containsKey("check") && p == null) { throw new CommandParseException("玩家 " + arg + "不存在或不在线!"); }
            return p;
        }
    }

    public static class StringParse extends Parse<String> {
        List<String> options;

        public StringParse() {
            allparses.put(String.class, this);
        }

        @Override
        public String parse(CommandSender sender, String arg) {
            if (min > arg.length() || arg.length() > max) {
                throwRange("长度必须在 %s 和 %s 之间!");
            }
            if (options != null && !options.contains(arg)) {
                throwException("参数 %s 不是一个有效的选项 有效值为 %s", arg, options);
            }
            return arg;
        }

        @Override
        public Parse<String> parseAnnotation(Annotation[] annotations) {
            if (attrs.containsKey("option")) {
                options = Arrays.asList(attrs.get("option").split(","));
            }
            return super.parseAnnotation(annotations);
        }
    }
}

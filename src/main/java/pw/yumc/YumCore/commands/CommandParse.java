package pw.yumc.YumCore.commands;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.commands.annotation.Option;
import pw.yumc.YumCore.commands.exception.ParseException;

/**
 * 命令参数解析
 *
 * @author 喵♂呜
 * @since 2016年10月5日 下午4:02:04
 */
public class CommandParse {
    private static Map<Class, Parse> allparses = new HashMap<>();
    private boolean isMain;
    private List<Parse> parse = new LinkedList<>();
    static {
        new FileParse();
        new PlayerParse();
        new StringParse();
    }

    public CommandParse(Class[] classes, Annotation[][] annons, boolean isMain) {
        this.isMain = isMain;
        // 第一个参数实现了CommandSender忽略
        for (int i = 1; i < classes.length; i++) {
            Class clazz = classes[i];
            Annotation[] annotations = annons[i];
            Parse parse = allparses.get(clazz);
            try {
                parse = new ValueOfParse(clazz, clazz.getDeclaredMethod("valueOf", String.class));
            } catch (NoSuchMethodException ignored) {
            }
            if (parse == null) { throw new ParseException(String.format("存在无法解析的参数类型 %s", clazz.getName())); }
            this.parse.add(parse.clone().parseAnnotation(annotations).handleAttrs());
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
        StringBuilder str = new StringBuilder();
        for (Object s : arr) {
            str.append(s.toString());
            str.append(split);
        }
        return str.length() > split.length() ? str.toString().substring(0, str.length() - split.length()) : str.toString();
    }

    public static void register(Class clazz, Parse parse) {
        allparses.put(clazz, parse);
    }

    public Object[] parse(CommandSender sender, String label, String[] args) {
        List<Object> pobjs = new LinkedList<>();
        pobjs.add(sender);
        for (int i = 0; i < parse.size(); i++) {
            try {
                Parse p = parse.get(i);
                String param = i < args.length ? args[i] : p.def;
                // 参数大于解析器 并且为最后一个参数
                if (i + 1 == parse.size() && args.length >= parse.size()) {
                    param = join(Arrays.copyOfRange(args, i, args.length), " ");
                }
                pobjs.add(param == null ? null : p.parse(sender, param));
            } catch (Exception e) {
                throw new ParseException(String.format("第 %s 个参数 %s", isMain ? 1 : 2 + i, e.getMessage()));
            }
        }
        Log.d("解析参数: %s => %s", Arrays.toString(args), pobjs);
        return pobjs.toArray();
    }

    public static abstract class Parse<RT> implements Cloneable {
        protected Map<String, String> attrs = new HashMap<>();
        protected String def;
        protected int max = Integer.MAX_VALUE;
        protected int min = 0;

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

        public abstract RT parse(CommandSender sender, String arg) throws ParseException;

        public Parse<RT> parseAnnotation(Annotation[] annotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Option.class) {
                    String value = ((Option) annotation).value();
                    for (String str : value.split(" ")) {
                        if (str.isEmpty()) {
                            continue;
                        }
                        if (str.contains(":")) {
                            String[] args = str.split(":");
                            attrs.put(args[0], args[1]);
                        } else {
                            attrs.put(str, null);
                        }
                    }
                }
            }
            return this;
        }

        public Parse<RT> handleAttrs() {
            if (attrs.containsKey("def")) {
                def = String.valueOf(attrs.get("def"));
            } else if (attrs.containsKey("min")) {
                min = Integer.parseInt(String.valueOf(attrs.get("min")));
            } else if (attrs.containsKey("max")) {
                max = Integer.parseInt(String.valueOf(attrs.get("max")));
            }
            return this;
        }

        public <T> T throwException(String str, Object... objects) {
            throw new ParseException(String.format(str, objects));
        }

        public <T> T throwRange() {
            return throwRange("");
        }

        public <T> T throwRange(String str) {
            return throwException(str.isEmpty() ? "范围必须在 %s 到 %s 之间!" : str, min, max);
        }
    }

    public static class ValueOfParse extends Parse<Object> {
        private Class etype;
        private Enum[] elist;
        private Method method;
        private Method checker;

        public ValueOfParse(Class etype, Method method) {
            this.etype = etype;
            try {
                checker = etype.getDeclaredMethod("doubleValue");
            } catch (NoSuchMethodException ignored) {
            }
            this.method = method;
            if (etype.isEnum()) {
                this.elist = ((Class<Enum>) etype).getEnumConstants();
            }
        }

        @Override
        public Object parse(CommandSender sender, String arg) {
            try {
                Object result = method.invoke(null, arg);
                if (checker != null) {
                    double num = (double) checker.invoke(result);
                    if (min > num || num > max) {
                        throwRange();
                    }
                }
                return result;
            } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
                if (etype.isEnum() && elist.length < 21) {
                    return throwException("%s 不是 %s 有效值为 %s", arg, etype.getSimpleName(), Arrays.toString(elist));
                } else {
                    return throwException("%s 不是一个有效的 %s", arg, etype.getSimpleName());
                }
            }
        }
    }

    public static class FileParse extends Parse<File> {
        public FileParse() {
            register(File.class, this);
        }

        @Override
        public File parse(CommandSender sender, String arg) throws ParseException {
            File file = new File(arg);
            if (attrs.containsKey("check") && !file.exists()) { throw new ParseException("文件 " + arg + " 不存在!"); }
            return file;
        }
    }

    public static class PlayerParse extends Parse<Player> {
        boolean check = false;

        public PlayerParse() {
            register(Player.class, this);
        }

        @Override
        public Player parse(CommandSender sender, String arg) {
            Player p = Bukkit.getPlayerExact(arg);
            if (check && p == null) { return throwException("玩家 %s 不存在或不在线!", arg); }
            return p;
        }

        @Override
        public Parse<Player> handleAttrs() {
            check = attrs.containsKey("check");
            return this;
        }
    }

    public static class StringParse extends Parse<String> {
        List<String> options;

        public StringParse() {
            register(String.class, this);
        }

        @Override
        public String parse(CommandSender sender, String arg) {
            if (min > arg.length() || arg.length() > max) { return throwRange("长度必须在 %s 和 %s 之间!"); }
            if (options != null && !options.contains(arg)) { return throwException("参数 %s 不是一个有效的选项 有效值为 %s", arg, options); }
            return arg;
        }

        @Override
        public Parse<String> handleAttrs() {
            if (attrs.containsKey("option")) {
                options = Arrays.asList(attrs.get("option").split(","));
            }
            return this;
        }
    }
}

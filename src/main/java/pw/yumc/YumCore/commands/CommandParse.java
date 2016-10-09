package pw.yumc.YumCore.commands;

import java.lang.annotation.Annotation;
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
        for (final Class classe : classes) {
            final Class clazz = classe;
            if (clazz.isAssignableFrom(CommandSender.class)) {
                continue;
            }
            if (!allparses.containsKey(clazz)) {
                throw new CommandParseException(String.format("无法解析的参数类型 %s !", clazz.getName()));
            }
            parse.add(allparses.get(clazz));
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
                throw new CommandException(String.format(e.getMessage(), i + 1));
            }
        }
        return pobjs.toArray();
    }

    public static class BooleanParse implements Parse<Boolean> {
        public BooleanParse() {
            allparses.put(Boolean.class, this);
            allparses.put(boolean.class, this);
        }

        @Override
        public Boolean parse(final String arg) {
            try {
                return Boolean.parseBoolean(arg);
            } catch (final Exception e) {
                throw new CommandParseException("第 %s 个参数必须为True或者False!");
            }
        }
    }

    public static class IntegerParse implements Parse<Integer> {
        public IntegerParse() {
            allparses.put(Integer.class, this);
            allparses.put(int.class, this);
        }

        @Override
        public Integer parse(final String arg) {
            try {
                return Integer.parseInt(arg);
            } catch (final Exception e) {
                throw new CommandParseException("第 %s 个参数必须为数字!");
            }
        }
    }

    public static class LongParse implements Parse<Long> {
        public LongParse() {
            allparses.put(Long.class, this);
            allparses.put(long.class, this);
        }

        @Override
        public Long parse(final String arg) {
            try {
                return Long.parseLong(arg);
            } catch (final Exception e) {
                throw new CommandParseException("第 %s 个参数必须为数字!");
            }
        }
    }

    public static interface Parse<RT> {
        public RT parse(String arg) throws CommandParseException;
    }

    public static class PlayerParse implements Parse<Player> {
        public PlayerParse() {
            allparses.put(Player.class, this);
        }

        @Override
        public Player parse(final String arg) {
            return Bukkit.getPlayerExact(arg);
        }
    }

    public static class StringParse implements Parse<String> {
        public StringParse() {
            allparses.put(String.class, this);
        }

        @Override
        public String parse(final String arg) {
            return arg;
        }
    }
}

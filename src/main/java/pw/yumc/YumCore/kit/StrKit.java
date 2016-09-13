package pw.yumc.YumCore.kit;

/**
 * 字符串工具类
 * 
 * @author 喵♂呜
 * @since 2016年9月14日 上午1:02:23
 */
public class StrKit {
    /**
     * 转移数组后获取字符串
     *
     * @param args
     *            原数组
     * @param start
     *            数组开始位置
     * @return 转移后的数组字符串
     */
    public static String consolidateStrings(final String[] args, final int start) {
        String ret = args[start];
        if (args.length > start + 1) {
            for (int i = start + 1; i < args.length; i++) {
                ret = ret + " " + args[i];
            }
        }
        return ret;
    }
}

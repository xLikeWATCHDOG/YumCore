package pw.yumc.YumCore.sql.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * 数据库键值管理类
 *
 * @since 2015年12月14日 下午1:26:24
 * @author 喵♂呜
 */
public class KeyValue {

    private final HashMap<Object, Object> keyvalues = new HashMap<Object, Object>();

    /**
     * 数据库键值管理类
     */
    public KeyValue() {
    }

    /**
     * 数据库键值管理类
     *
     * @param key
     *            键
     * @param value
     *            值
     */
    public KeyValue(final String key, final Object value) {
        add(key, value);
    }

    /**
     * 添加数据
     *
     * @param key
     *            键
     * @param value
     *            值
     * @return {@link KeyValue}
     */
    public KeyValue add(final String key, final Object value) {
        this.keyvalues.put(key, value);
        return this;
    }

    /**
     * 获得所有的键
     *
     * @return 所有的键
     */
    public String[] getKeys() {
        return this.keyvalues.keySet().toArray(new String[0]);
    }

    /**
     * 获得值
     *
     * @param key
     *            查询的键
     * @return 值
     */
    public String getString(final String key) {
        final Object obj = this.keyvalues.get(key);
        return obj == null ? "" : obj.toString();
    }

    /**
     * 获得所有的值
     *
     * @return 所有的值
     */
    public Object[] getValues() {
        final List<Object> keys = new ArrayList<Object>();
        for (final Entry<Object, Object> next : this.keyvalues.entrySet()) {
            keys.add(next.getValue());
        }
        return keys.toArray(new Object[0]);
    }

    /**
     * 判断数据是否为空
     *
     * @return 数据是否为空
     */
    public boolean isEmpty() {
        return this.keyvalues.isEmpty();
    }

    /**
     * 转换为数据表创建SQL语句
     *
     * @return 数据表创建SQL语句
     */
    public String toCreateString() {
        final StringBuilder sb = new StringBuilder();
        for (final Entry<Object, Object> next : this.keyvalues.entrySet()) {
            sb.append("`");
            sb.append(next.getKey());
            sb.append("` ");
            sb.append(next.getValue());
            sb.append(", ");
        }
        return sb.toString().substring(0, sb.length() - 2);
    }

    /**
     * 转换字段为数据添加SQL语句
     *
     * @return 添加SQL语句
     */
    public String toInsertString() {
        String ks = "";
        String vs = "";
        for (final Entry<Object, Object> next : this.keyvalues.entrySet()) {
            ks += "`" + next.getKey() + "`, ";
            vs += "'" + next.getValue() + "', ";
        }
        return "(" + ks.substring(0, ks.length() - 2) + ") VALUES (" + vs.substring(0, vs.length() - 2) + ")";
    }

    /**
     * @return 转换为键列
     */
    public String toKeys() {
        final StringBuilder sb = new StringBuilder();
        for (final Object next : this.keyvalues.keySet()) {
            sb.append("`");
            sb.append(next);
            sb.append("`, ");
        }
        return sb.toString().substring(0, sb.length() - 2);
    }

    @Override
    public String toString() {
        return this.keyvalues.toString();
    }

    /**
     * 转换字段为更新SQL语句
     *
     * @return 更新SQL语句
     */
    public String toUpdateString() {
        final StringBuilder sb = new StringBuilder();
        for (final Entry<Object, Object> next : this.keyvalues.entrySet()) {
            sb.append("`");
            sb.append(next.getKey());
            sb.append("`='");
            sb.append(next.getValue());
            sb.append("' ,");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * 转换字段为查询SQL语句
     *
     * @return 查询SQL语句
     */
    public String toWhereString() {
        final StringBuilder sb = new StringBuilder();
        for (final Entry<Object, Object> next : this.keyvalues.entrySet()) {
            sb.append("`");
            sb.append(next.getKey());
            sb.append("`='");
            sb.append(next.getValue());
            sb.append("' and ");
        }
        return sb.substring(0, sb.length() - 5);
    }
}

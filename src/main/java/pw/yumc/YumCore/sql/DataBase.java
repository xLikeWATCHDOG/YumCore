package pw.yumc.YumCore.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import pw.yumc.YumCore.bukkit.Log;
import pw.yumc.YumCore.sql.core.DataBaseCore;
import pw.yumc.YumCore.sql.core.KeyValue;
import pw.yumc.YumCore.sql.core.MySQLCore;
import pw.yumc.YumCore.sql.core.SQLiteCore;

/**
 * 数据库管理类
 *
 * @since 2015年12月14日 下午1:26:06
 * @author 喵♂呜
 *
 */
public class DataBase {
    private final DataBaseCore dataBaseCore;

    /**
     * 初始化数据库管理
     *
     * @param core
     *            数据库核心
     */
    public DataBase(final DataBaseCore core) {
        this.dataBaseCore = core;
    }

    public static DataBase create(final Plugin plugin, final ConfigurationSection dbConfig) {
        final ConfigurationSection cfg = dbConfig.getConfigurationSection("MySQL");
        if (dbConfig.getString("FileSystem").equalsIgnoreCase("MySQL")) {
            plugin.getLogger().info("已启用MySQL保存数据,开始连接数据库...");
            return new DataBase(new MySQLCore(cfg));
        }
        return new DataBase(new SQLiteCore(plugin, cfg));
    }

    /**
     * 关闭数据库连接
     *
     * @return 是否关闭成功
     */
    public boolean close() {
        try {
            this.dataBaseCore.getConnection().close();
            return true;
        } catch (final SQLException e) {
            Log.debug("数据库链接关闭失败!", e);
            return false;
        }
    }

    /**
     * 复制当前数据核心的数据库到指定的数据库
     * 此方法将不会删除当前数据库原有数据
     * 此方法可能花费较长的时间
     *
     * 注意: 当前方法将不会创建表在新的数据库内 需要自行创建数据表
     *
     * @param db
     *            接受数据的数据库核心
     * @return 是否转换成功
     */
    public boolean copyTo(final DataBaseCore db) {
        try {
            final String src = this.dataBaseCore.getConnection().getMetaData().getURL();
            final String des = db.getConnection().getMetaData().getURL();
            Log.info("开始从源 " + src + " 复制数据到 " + des + " ...");
            ResultSet rs = this.dataBaseCore.getConnection().getMetaData().getTables(null, null, "%", null);
            final List<String> tables = new LinkedList<>();
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            info("源数据库中有 " + tables.size() + " 张数据表 ...");
            rs.close();
            int s = 0;
            final long start = System.currentTimeMillis();
            for (final String table : tables) {
                Log.info("开始复制源数据库中的表 " + table + " ...");
                if (table.toLowerCase().startsWith("sqlite_autoindex_")) {
                    continue;
                }
                Log.info("清空目标数据库中的表 " + table + " ...");
                db.execute("DELETE FROM " + table);
                rs = this.dataBaseCore.query("SELECT * FROM " + table);
                int n = 0;
                String query = "INSERT INTO " + table + " VALUES (";
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    query += "?, ";
                }
                query = query.substring(0, query.length() - 2) + ")";

                final Connection con = db.getConnection();
                try {
                    con.setAutoCommit(false);
                    final PreparedStatement ps = con.prepareStatement(query);
                    long time = System.currentTimeMillis();
                    while (rs.next()) {
                        n++;
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            ps.setObject(i, rs.getObject(i));
                        }
                        ps.addBatch();
                        if (n % 100 == 0) {
                            try {
                                ps.executeBatch();
                                con.commit();
                            } catch (final SQLException e) {
                                info("#====================================================");
                                info("#数据复制区段(不是ID!) " + (n - 100) + "-" + n + " 出现错误...");
                                info("#错误信息如下: ");
                                e.printStackTrace();
                                info("#====================================================");
                            }
                        }
                        if (System.currentTimeMillis() - time > 500) {
                            info("已复制 " + n + " 条记录...");
                            time = System.currentTimeMillis();
                        }
                    }
                    s += n;
                    ps.executeBatch();
                    con.commit();
                    info("数据表 " + table + " 复制完成 共 " + n + " 条记录...");
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    con.setAutoCommit(true);
                }
                rs.close();
            }
            info("成功从 " + src + " 复制 " + s + " 条数据到 " + des + " 耗时 " + (System.currentTimeMillis() - start) / 1000 + " 秒...");
            db.getConnection().close();
            this.dataBaseCore.getConnection().close();
            return true;
        } catch (final SQLException e) {
            return false;
        }
    }

    /**
     * 创建数据表
     *
     * @param tableName
     *            表名
     * @param fields
     *            字段参数
     * @param Conditions
     *            -附加值
     * @return 运行结果
     */
    public boolean createTables(final String tableName, final KeyValue fields, final String Conditions) {
        try {
            this.dataBaseCore.createTables(tableName, fields, Conditions);
            return isTableExists(tableName);
        } catch (final Exception e) {
            sqlerr("创建数据表 " + tableName + " 异常(内部方法)...", e);
            return false;
        }
    }

    /**
     * 对数据库表中的记录进行删除操作
     *
     * @param tableName
     *            表名
     * @param fields
     *            删除条件
     * @return 受到影响的数据条目
     */
    public int dbDelete(final String tableName, final KeyValue fields) {
        final String sql = "DELETE FROM `" + tableName + "` WHERE " + fields.toWhereString();
        try {
            return this.dataBaseCore.update(sql);
        } catch (final Exception e) {
            sqlerr(sql, e);
            return 0;
        }
    }

    /**
     * 判断数据库某个值是否存在!
     *
     * @param tableName
     *            数据库表名
     * @param fields
     *            选择条件
     * @return 首个符合条件的结果
     */
    public boolean dbExist(final String tableName, final KeyValue fields) {
        final String sql = "SELECT * FROM " + tableName + " WHERE " + fields.toWhereString();
        try {
            return this.dataBaseCore.query(sql).next();
        } catch (final Exception e) {
            sqlerr(sql, e);
            return false;
        }
    }

    /**
     * 对数据库表进行插入操作
     *
     * @param tabName
     *            表名
     * @param fields
     *            带键值的
     * @return 受到影响的数据条目
     */
    public int dbInsert(final String tabName, final KeyValue fields) {
        final String sql = "INSERT INTO `" + tabName + "` " + fields.toInsertString();
        try {
            return this.dataBaseCore.update(sql);
        } catch (final Exception e) {
            sqlerr(sql, e);
            return 0;
        }

    }

    // @SuppressWarnings("unchecked")
    // public <M> List<M> dbSelect(final Class<? extends Model<?>> model, final KeyValue selCondition) {
    // final List<M> modellist = new ArrayList<>();
    // final String sql = "SELECT " + toKeys(model) + " FROM `" + model.getAnnotation(Entity.class).name() + "`" + (selCondition == null ? "" : " WHERE " + selCondition.toWhereString());
    // try {
    // final ResultSet dbresult = this.dataBaseCore.execute(sql);
    // while (dbresult.next()) {
    // final M m = (M) model.newInstance();
    // final Field[] fields = model.getDeclaredFields();
    // for (final Field col : fields) {
    // col.set(m, dbresult.getObject(col.getName()));
    // }
    // modellist.add(m);
    // }
    // } catch (final InstantiationException e) {
    // info("模型类实例化失败!");
    // e.printStackTrace();
    // } catch (final Exception e) {
    // sqlerr(sql, e);
    // }
    // return modellist;
    // }

    /**
     * 对数据库表进行选择操作！
     *
     * @param tableName
     *            数据库表名
     * @param fields
     *            读取的字段
     * @param selCondition
     *            选择条件
     * @return 一个含有KeyValue的List（列表）
     */
    public List<KeyValue> dbSelect(final String tableName, final KeyValue fields, final KeyValue selCondition) {
        final String sql = "SELECT " + fields.toKeys() + " FROM `" + tableName + "`" + (selCondition == null ? "" : " WHERE " + selCondition.toWhereString());
        final List<KeyValue> kvlist = new ArrayList<>();
        try {
            final ResultSet dbresult = this.dataBaseCore.query(sql);
            while (dbresult.next()) {
                final KeyValue kv = new KeyValue();
                for (final String col : fields.getKeys()) {
                    kv.add(col, dbresult.getString(col.toString()));
                }
                kvlist.add(kv);
            }
        } catch (final Exception e) {
            sqlerr(sql, e);
        }
        return kvlist;
    }

    /**
     * 对数据库表进行选择操作！
     *
     * @param tableName
     *            数据库表名
     * @param selCondition
     *            选择条件
     * @param fields
     *            读取的字段
     * @return 一个含有KeyValue的List（列表）
     */
    public List<KeyValue> dbSelect(final String tableName, final KeyValue selCondition, final String... fields) {
        final String sql = "SELECT " + getKeys(fields) + " FROM `" + tableName + "`" + (selCondition == null ? "" : " WHERE " + selCondition.toWhereString());
        final List<KeyValue> kvlist = new ArrayList<>();
        try {
            final ResultSet dbresult = this.dataBaseCore.query(sql);
            while (dbresult.next()) {
                final KeyValue kv = new KeyValue();
                for (final String col : fields) {
                    kv.add(col, dbresult.getString(col.toString()));
                }
                kvlist.add(kv);
            }
        } catch (final Exception e) {
            sqlerr(sql, e);
        }
        return kvlist;
    }

    /**
     * 对数据库表进行选择操作！
     *
     * @param tableName
     *            数据库表名
     * @param fields
     *            字段名
     * @param selConditions
     *            选择条件
     * @return 首个符合条件的结果
     */
    public String dbSelectFirst(final String tableName, final String fields, final KeyValue selConditions) {
        final String sql = "SELECT " + fields + " FROM " + tableName + " WHERE " + selConditions.toWhereString() + " limit 1";
        try {
            final ResultSet dbresult = this.dataBaseCore.query(sql);
            if (dbresult.next()) {
                return dbresult.getString(fields);
            }
        } catch (final Exception e) {
            sqlerr(sql, e);
        }
        return null;
    }

    /**
     * 对数据库表中记录进行更新操作
     *
     * @param tabName
     *            表名
     * @param fields
     *            字段参数
     * @param upCondition
     *            更新条件
     * @return 受到影响的数据条目
     */
    public int dbUpdate(final String tabName, final KeyValue fields, final KeyValue upCondition) {
        final String sql = "UPDATE `" + tabName + "` SET " + fields.toUpdateString() + " WHERE " + upCondition.toWhereString();
        try {
            return this.dataBaseCore.update(sql);
        } catch (final Exception e) {
            sqlerr(sql, e);
            return 0;
        }
    }

    /**
     * 获得当前使用的数据库核心
     *
     * @return 数据库核心
     */
    public DataBaseCore getDataBaseCore() {
        return this.dataBaseCore;
    }

    /**
     * 字段数组转字符串
     *
     * @param fields
     *            字段数组
     * @return 字段字符串
     */
    public String getKeys(final String... fields) {
        final StringBuilder sb = new StringBuilder();
        for (final String string : fields) {
            sb.append("`");
            sb.append(string);
            sb.append("`, ");
        }
        return sb.toString().substring(0, sb.length() - 2);
    }

    public boolean isFieldExists(final String tableName, final KeyValue fields) {
        final DatabaseMetaData dbm;
        final ResultSet tables;
        try {
            dbm = this.dataBaseCore.getConnection().getMetaData();
            tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                final ResultSet f = dbm.getColumns(null, null, tableName, fields.getKeys()[0]);
                return f.next();
            }
        } catch (final SQLException e) {
            sqlerr("判断 表名:" + tableName + " 字段名:" + fields.getKeys()[0] + " 是否存在时出错!", e);
        }
        return false;
    }

    /**
     * 判断数据表是否存在
     *
     * @param tableName
     *            表名
     * @return 是否存在
     */
    public boolean isTableExists(final String tableName) {
        try {
            final DatabaseMetaData dbm = this.dataBaseCore.getConnection().getMetaData();
            final ResultSet tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (final SQLException e) {
            sqlerr("判断 表名:" + tableName + " 是否存在时出错!", e);
            return false;
        }
    }

    /**
     * 批量执行SQL语句
     *
     * @param sqls
     *            SQL语句列表
     */
    public void runSqlList(final Collection<String> sqls) {
        final Connection con = getDataBaseCore().getConnection();
        final long start = System.currentTimeMillis();
        try {
            long time = System.currentTimeMillis();
            con.setAutoCommit(false);
            final Statement st = con.createStatement();
            int i = 0;
            for (final String sql : sqls) {
                st.addBatch(sql);
                i++;
                if (i % 100 == 0) {
                    st.executeBatch();
                    con.commit();
                    if (System.currentTimeMillis() - time > 500) {
                        info("已执行 " + i + " 条语句...");
                        time = System.currentTimeMillis();
                    }
                }
            }
            st.executeBatch();
            con.commit();
            info("执行SQL完毕 总计: " + sqls.size() + " 条 耗时: " + start + "ms!");
        } catch (final SQLException e) {
            try {
                con.rollback();
                sqlerr("执行SQL数组发生错误 数据已回滚...", e);
            } catch (final SQLException e1) {
                sqlerr("执行SQL数组发生错误 警告! 数据回滚失败...", e1);
            }
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (final SQLException e) {
            }
        }
    }

    /**
     * 输出SQL错误
     *
     * @param sql
     *            SQL语句
     * @param e
     *            错误异常
     */
    public void sqlerr(final String sql, final Exception e) {
        info("数据库操作出错: " + e.getMessage());
        info("SQL查询语句: " + sql);
        Log.debug(this.getClass().getName());
        Log.debug(e);
    }

    /**
     * 测试数据库连接
     *
     * @return 是否连接成功
     */
    public boolean testConnect() {
        return this.dataBaseCore.getConnection() != null;
    }

    private void info(final String info) {
        Log.info(info);
    }

    // private String toKeys(final Class<? extends Model<?>> model) {
    // final Field[] fields = model.getDeclaredFields();
    // final StringBuilder sb = new StringBuilder();
    // for (final Field next : fields) {
    // sb.append("`");
    // sb.append(next.getName());
    // sb.append("`, ");
    // }
    // return sb.toString().substring(0, sb.length() - 2);
    // }

}

package pw.yumc.YumCore.sql.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pw.yumc.YumCore.bukkit.Log;

/**
 * 数据库核心类
 *
 * @since 2015年12月14日 下午1:26:15
 * @author 喵♂呜
 */
public abstract class DataBaseCore {
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
     * @throws SQLException
     *             SQL异常
     */
    public abstract boolean createTables(final String tableName, final KeyValue fields, final String Conditions) throws SQLException;

    /**
     * 执行SQL语句
     *
     * @param sql
     *            SQL语句
     * @return 是否执行成功
     * @throws SQLException
     *             SQL执行异常
     */
    public boolean execute(final String sql) throws SQLException {
        debug(sql);
        final Statement st = getStatement();
        final boolean result = st.execute(sql);
        st.close();
        return result;
    }

    /**
     * 执行SQL语句(预处理)
     *
     * @param sql
     *            SQL语句
     * @param obj
     *            参数
     * @return 是否执行成功
     * @throws SQLException
     *             SQL执行异常
     */
    public boolean execute(final String sql, final Object... obj) throws SQLException {
        debug(sql);
        final PreparedStatement ps = prepareStatement(sql);
        for (int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        final boolean result = ps.execute(sql);
        ps.close();
        return result;
    }

    /**
     * @return 获得自增关键词
     */
    public String getAUTO_INCREMENT() {
        return "AUTO_INCREMENT";
    }

    /**
     * 获得连接池中打开的数据连接.
     *
     * @return 数据连接
     */
    public abstract Connection getConnection();

    /**
     * 查询数据库
     *
     * @param sql
     *            SQL查询语句
     * @return 查询结果
     * @throws SQLException
     *             SQL查询异常
     */
    public ResultSet query(final String sql) throws SQLException {
        debug(sql);
        final Statement st = getStatement();
        final ResultSet result = st.executeQuery(sql);
        return result;
    }

    /**
     * 更新数据库内的数据
     *
     * @param sql
     *            SQL更新语句
     * @return 受到影响的行数
     * @throws SQLException
     *             SQL执行异常
     */
    public int update(final String sql) throws SQLException {
        debug(sql);
        final Statement st = getStatement();
        final int result = st.executeUpdate(sql);
        st.close();
        return result;
    }

    /**
     * 更新数据库内的数据(预处理)
     *
     * @param sql
     *            SQL更新语句
     * @param obj
     *            参数
     * @return 受到影响的行数
     * @throws SQLException
     *             SQL执行异常
     */
    public int update(final String sql, final Object[] obj) throws SQLException {
        debug(sql);
        final PreparedStatement ps = prepareStatement(sql);
        for (int i = 0; i < obj.length; i++) {
            ps.setObject(i + 1, obj[i]);
        }
        final int result = ps.executeUpdate(sql);
        ps.close();
        return result;
    }

    /**
     * 发送警告
     *
     * @param warn
     *            警告消息
     */
    public void warn(final String warn) {
        Log.warning(warn);
    }

    /**
     * SQL调试消息
     *
     * @param sql
     *            SQL语句
     */
    private void debug(final String sql) {
        Log.debug("[SQL] " + sql);
    }

    /**
     * 获得数据操作对象
     *
     * @return 操作对象
     * @throws SQLException
     *             SQL执行异常
     */
    protected Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    /**
     * 获得数据操作对象(预处理)
     *
     * @param sql
     *            SQL语句
     * @return 操作对象
     * @throws SQLException
     *             SQL执行异常
     */
    protected PreparedStatement prepareStatement(final String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }
}

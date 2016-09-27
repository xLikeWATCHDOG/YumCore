package pw.yumc.YumCore.mail;

import com.google.common.net.HostAndPort;

/**
 *
 * @since 2016年4月9日 下午5:23:09
 * @author 喵♂呜
 */
public class MailAPI {

    /**
     * 快速发信
     *
     * @param smtp
     *            SMTP 服务器
     * @param from
     *            发件人
     * @param to
     *            收件人
     * @param subject
     *            主题
     * @param content
     *            内容
     * @return 是否发送成功
     */
    public static boolean send(final HostAndPort smtp, final String from, final String to, final String subject, final String content) {
        return send(smtp, from, to, subject, content, null, null);
    }

    /**
     * 快速发信
     *
     * @param smtp
     *            SMTP 服务器
     * @param from
     *            发件人
     * @param to
     *            收件人
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return 是否发送成功
     */
    public static boolean send(final HostAndPort smtp, final String from, final String to, final String subject, final String content, final String username, final String password) {
        return send(smtp, from, null, to, subject, content, username, password);
    }

    /**
     * 快速发信
     *
     * @param smtp
     *            SMTP 服务器
     * @param from
     *            发件人
     * @param fromName
     *            发件人名称
     * @param to
     *            收件人
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return 是否发送成功
     */
    public static boolean send(final HostAndPort smtp, final String from, final String fromName, final String to, final String subject, final String content, final String username, final String password) {
        return send(smtp, from, fromName, to, null, subject, content, username, password);
    }

    /**
     * 快速发信
     *
     * @param smtp
     *            SMTP 服务器
     * @param from
     *            发件人
     * @param fromName
     *            发件人名称
     * @param to
     *            收件人
     * @param copyto
     *            抄送
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return 是否发送成功
     */
    public static boolean send(final HostAndPort smtp, final String from, final String fromName, final String to, final String copyto, final String subject, final String content, final String username, final String password) {
        return send(smtp, from, fromName, to, copyto, subject, content, null, username, password);
    }

    /**
     * 快速发信
     *
     * @param smtp
     *            SMTP 服务器
     * @param from
     *            发件人
     * @param fromName
     *            发件人名称
     * @param to
     *            收件人
     * @param copyto
     *            抄送
     * @param subject
     *            主题
     * @param content
     *            内容
     * @param filename
     *            文件名称
     * @param username
     *            用户名
     * @param password
     *            密码
     * @return 是否发送成功
     */
    public static boolean send(final HostAndPort smtp, final String from, final String fromName, final String to, final String copyto, final String subject, final String content, final String[] filename, final String username, final String password) {
        return XMail.send(smtp, from, fromName, to, copyto, subject, content, filename, username, password, true);
    }
}

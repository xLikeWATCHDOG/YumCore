package pw.yumc.YumCore.mail;

import java.util.Date;

import javax.mail.Address;

class SimpleMail {

    private String subject;
    private Object content;

    private Date sentDate = null;
    private String from = null;
    private Address sender;

    public SimpleMail(final String subject, final Object content) {
        this.subject = subject;
        this.content = content;
    }

    /**
     * 内容
     *
     * @return
     */
    public Object getContent() {
        return content;
    }

    public String getFrom() {
        return from;
    }

    public Address getSender() {
        return sender;
    }

    public Date getSentDate() {
        return sentDate;
    }

    /**
     * 标题
     *
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 内容
     *
     * @param content
     */
    public void setContent(final Object content) {
        this.content = content;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public void setSender(final Address sender) {
        this.sender = sender;
    }

    public void setSentDate(final Date date) {
        this.sentDate = date;
    }

    /**
     * 标题
     *
     * @param subject
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

}
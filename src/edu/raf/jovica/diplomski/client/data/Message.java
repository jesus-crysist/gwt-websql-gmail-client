package edu.raf.jovica.diplomski.client.data;

import com.google.code.gwt.database.client.util.StringUtils;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/1/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class Message implements IsSerializable {

    @SuppressWarnings(value="unused")
    protected Message() {}

    public Message(long id) {
        this.id = id;
        recipients = new ArrayList<String>();
    }

    public Message(String error) {
        this.error = error;
    }

    private long id;
    private int messageNumber;
    private String subject;
    private String sender;
    private ArrayList<String> recipients;
    private Date sentDate;
    private Date receivedDate;
    private int read;
    private String path;
    private String body;
    private String error;

    public long getId() {
        return id;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipientsAsSingleString() {
        return StringUtils.join(recipients, ",");
    }

    public void setRecipientsAsString(String recipients) {
        this.recipients = new ArrayList<String>( Arrays.asList(recipients.split(",") ));
    }

    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }

    public int getSentDate() {
        return (int) (sentDate.getTime() / 1000L);
    }

    public void setSentDate(int sentDate) {
        this.sentDate = new Date(sentDate * 1000L);
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public int getReceivedDate() {
        return (int) (receivedDate.getTime() / 1000L);
    }

    public void setReceivedDate(int receivedDate) {
        this.receivedDate = new Date(receivedDate * 1000L);
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public boolean isRead() {
        return read == 1;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public void setReadBool(boolean read) {
        this.read = read ? 1 : 0;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

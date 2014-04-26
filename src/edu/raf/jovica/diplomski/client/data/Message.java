package edu.raf.jovica.diplomski.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/1/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class Message implements IsSerializable {

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
    private boolean isRead;
    private String path;
    private String body;
    private String error;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public ArrayList<String> getRecipients() {
        return recipients;
    }

    public String getRecipientsAsSingleString() {

        String single = "";

        Iterator<String> iterator = recipients.iterator();

        while(iterator.hasNext()) {
            single += iterator.next();
        }

        return single;
    }

    public void setRecipients(ArrayList<String> recipients) {
        this.recipients = recipients;
    }

    public void addRecipient(String recipient) {
        this.recipients.add(recipient);
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sendDate) {
        this.sentDate = sendDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

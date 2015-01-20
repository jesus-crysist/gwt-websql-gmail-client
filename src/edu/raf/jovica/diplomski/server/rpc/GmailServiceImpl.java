package edu.raf.jovica.diplomski.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.gimap.GmailSSLStore;
import com.sun.mail.gimap.GmailStore;
import edu.raf.jovica.diplomski.client.data.Folder;
import edu.raf.jovica.diplomski.client.data.Message;
import edu.raf.jovica.diplomski.client.rpc.GmailService;
import edu.raf.jovica.diplomski.util.MessageComparator;

import javax.mail.*;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/30/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GmailServiceImpl extends RemoteServiceServlet implements GmailService {

    private Map<String, GmailSessionStore> loggedInStores = new HashMap<String, GmailSessionStore>();

    @Override
    public String login(String username, String password) {

        String gmailSmtpPort = "587";

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "gimaps");
        // for sending mail
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", gmailSmtpPort);

        try {

            Session mailSession = Session.getDefaultInstance(props, null);

            GmailSSLStore store = (GmailSSLStore) mailSession.getStore("gimaps");

            PasswordAuthentication credentials = new PasswordAuthentication(username, password);

            store.connect(credentials.getUserName(), credentials.getPassword());

            loggedInStores.put(username, new GmailSessionStore(mailSession, store, credentials));

            return username;

        } catch (MessagingException ex) {

            System.out.println("EXCEPTION IN LOGIN PAGE: " + ex.getMessage());
            ex.printStackTrace();

            if (ex.getMessage().contains("Invalid credentials")) {
                return "Server error:Invalid username or password.";
            } else {
                return "Server error:" + ex.getMessage();
            }

        }
    }

    @Override
    public String logout(String username) {

        loggedInStores.remove(username);

        return username;
    }

    @Override
    public ArrayList<Folder> getFolderList(String username, String path) {

        ArrayList<Folder> folders = new ArrayList<Folder>();

        try {

            GmailSSLStore store = (GmailSSLStore) loggedInStores.get(username).getStore();

            if (!store.isConnected()) {
                store.connect();
            }

            javax.mail.Folder root = store.getFolder(path);

            return getFolder(root).getChildren();

        } catch (MessagingException e) {
            folders.add( new Folder("", e.getMessage()) );
        }

        return folders;
    }

    private Folder getFolder(javax.mail.Folder parent) throws MessagingException {

        javax.mail.Folder[] subfolders = parent.list();
        String path = parent.getFullName();

        String name = parent.getName();

        Folder folder = new Folder(name);

        int newCount, unreadCount, totalCount;

        if (subfolders.length == 0) {

            newCount = parent.getNewMessageCount();
            unreadCount = parent.getUnreadMessageCount();
            totalCount = parent.getMessageCount();

        } else {

            newCount = 0;
            unreadCount = 0;
            totalCount = 0;

            ArrayList<Folder> list = new ArrayList<Folder>();

            for(javax.mail.Folder f : subfolders) {
                list.add ( getFolder(f) );
            }

            folder.setChildren(list);
        }

        folder.setNewMessagesCount(newCount);
        folder.setUndreadMessageCount(unreadCount);
        folder.setTotalMessagesCount(totalCount);
        folder.setPath(path);

        return folder;
    }

    @Override
    public ArrayList<Message> getMessagesForPath(String username, String path, int from, int to) {

        ArrayList<Message> messages = new ArrayList<Message>();

        try {

            GmailSSLStore store = (GmailSSLStore) loggedInStores.get(username).getStore();

            if (!store.isConnected()) {
                store.connect();
            }

            GmailFolder folder = (GmailFolder) store.getFolder(path);

            if (store != null && store.isConnected()) {
                store.close();
            }

            return getMessages(folder, from, to);

        } catch (MessagingException e) {
            messages.add( new Message( e.getMessage() ) );
            e.printStackTrace();
        } catch (IOException e) {
            messages.add( new Message( e.getMessage() ) );
        }

        return messages;
    }

    private ArrayList<Message> getMessages(GmailFolder folder, int from, int to) throws MessagingException, IOException {

        ArrayList<Message> messages = new ArrayList<Message>();
        javax.mail.Message[] gmailMessages = null;

        FetchProfile profile = new FetchProfile();
        profile.add(GmailFolder.FetchProfileItem.ENVELOPE);

        folder.open(GmailFolder.READ_ONLY);

        if ( folder.getMessageCount() < to ) {
            to = folder.getMessageCount();
        }

        if (from > 0) {
            gmailMessages = folder.getMessages(from, to);
        } else {
            gmailMessages = folder.getMessages();
        }

        folder.fetch(gmailMessages, profile);

        GmailMessage gmailMsg;
        Message msg;

        for (javax.mail.Message m: gmailMessages) {
            gmailMsg = (GmailMessage) m;

            msg = new Message(gmailMsg.getMsgId());
            msg.setSubject(gmailMsg.getSubject());

            msg.setSender(gmailMsg.getSender().toString());

            if (msg.getSender() == null) {
                System.out.println("Missing sender" + gmailMsg.getSender().toString());
            }

            Address[] recipients = gmailMsg.getRecipients(javax.mail.Message.RecipientType.TO);

            if (recipients != null && recipients.length > 0) { // messages in draft folder don't have recipients
                for (Address recipient : recipients) {
                    msg.addRecipient(recipient.toString());
                }
            }

            msg.setSentDate(gmailMsg.getSentDate());
            msg.setReceivedDate(gmailMsg.getReceivedDate());
            msg.setReadBool(gmailMsg.isSet(Flags.Flag.SEEN));
            msg.setPath(folder.getFullName());
            msg.setMessageNumber(gmailMsg.getMessageNumber());

            // Getting message body
            StringBuilder sb = new StringBuilder();
            Multipart multipart;
            MimePart mimePart;
            String messageString;

            if ( gmailMsg.getContentType().contains("multipart/") ) {
                multipart = (MimeMultipart) gmailMsg.getContent();

                messageString = multipartMessageToString(multipart);
            }
            else if ( gmailMsg.getContentType().toLowerCase().contains("text/") ) {

                messageString = gmailMsg.getContent().toString();
            }
            else {
                mimePart = (MimeBodyPart) gmailMsg.getContent();
                messagePartToString(mimePart, sb);

                messageString = sb.toString();
            }

            msg.setBody(messageString);

            messages.add(msg);
        }

        messages.sort(new MessageComparator());

        if (folder != null && folder.isOpen()) {
            folder.close(true);
        }

        return messages;
    }

    @Override
    public Message setMessageReadFlag(String username, Message msg) {

        try {

            GmailSSLStore store = (GmailSSLStore) loggedInStores.get(username).getStore();

            if (!store.isConnected()) {
                store.connect();
            }

            GmailFolder folder = (GmailFolder) store.getFolder(msg.getPath());

            folder.open(javax.mail.Folder.READ_WRITE);

            GmailMessage gmailMsg = (GmailMessage) folder.getMessage(msg.getMessageNumber());

            // set SEEN flag on the server by getting it's content
            gmailMsg.getContent();

            // set message flag as seen
            gmailMsg.setFlag(Flags.Flag.SEEN, true);

            // set read flag in message object
            msg.setReadBool( gmailMsg.getFlags().contains(Flags.Flag.SEEN) );

            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }

            if (store != null && store.isConnected()) {
                store.close();
            }

            return msg;

        } catch (MessagingException e) {
            msg = new Message( e.getMessage() );
            e.printStackTrace();
        } catch (IOException e) {
            msg = new Message( e.getMessage() );
            e.printStackTrace();
        }

        return msg;
    }

    private String multipartMessageToString (Multipart message) throws MessagingException, IOException {

        int partCount = message.getCount();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < partCount; i++) {
            messagePartToString(message.getBodyPart(i), sb);
        }

        return sb.toString();
    }

    protected void messagePartToString(Part p, StringBuilder sb) throws MessagingException, IOException {

        if (p.isMimeType("text/plain")) {

            if (p.getContent().toString() != null) {

                sb.append((String)p.getContent());
            }
        } else if (p.isMimeType("multipart/*")) {

            Multipart mp = (Multipart) p.getContent();

            for (int x = 0; x < mp.getCount(); x++) {

                messagePartToString(mp.getBodyPart(x), sb);
            }
        }
    }

    /**
     * Send message via SMTP protocol.
     * @param username User that's trying to send a mail.
     * @param msg Message with data to be sent
     * @return Message filled with data
     */
    public Message sendMessage(String username, Message msg) {

        Session session = loggedInStores.get(username).getSession();
        PasswordAuthentication credentials = loggedInStores.get(username).getCredentials();

        try {

            MimeMessage msgToSend = new MimeMessage(session);

            msgToSend.addRecipients(javax.mail.Message.RecipientType.TO, msg.getRecipientsAsSingleString());
            msgToSend.setSubject(msg.getSubject());
            msgToSend.setText(msg.getBody());

            Transport transport = session.getTransport("smtp");
            transport.connect(credentials.getUserName(), credentials.getPassword());
            transport.sendMessage(msgToSend, msgToSend.getAllRecipients());
            transport.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return msg;
    }

    private class GmailSessionStore {

        private Session session;
        private GmailStore store;
        private PasswordAuthentication credentials;

        public GmailSessionStore(Session session, GmailStore store, PasswordAuthentication credentials) {
            this.session = session;
            this.store = store;
            this.credentials = credentials;
        }

        public Session getSession() {
            return session;
        }

        public GmailStore getStore() {
            return store;
        }

        public PasswordAuthentication getCredentials() {
            return credentials;
        }
    }
}
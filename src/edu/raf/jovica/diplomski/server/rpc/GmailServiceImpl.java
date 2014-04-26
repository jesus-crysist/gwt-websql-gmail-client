package edu.raf.jovica.diplomski.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.mail.gimap.GmailFolder;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.gimap.GmailSSLStore;
import edu.raf.jovica.diplomski.client.data.Folder;
import edu.raf.jovica.diplomski.client.data.Message;
import edu.raf.jovica.diplomski.client.rpc.GmailService;
import edu.raf.jovica.diplomski.util.MessageComparator;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/30/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GmailServiceImpl extends RemoteServiceServlet implements GmailService {

    private Map<String, GmailSSLStore> loggedInStores = new HashMap<String, GmailSSLStore>();

    @Override
    public String login(String username, String password) {

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "gimaps");

        try {

            GmailSSLStore store = null;

            Session mailSession = Session.getDefaultInstance(props, null);

            store = (GmailSSLStore) mailSession.getStore("gimaps");

            store.connect(username, password);

            loggedInStores.put(username, store);

            return username;

        } catch (MessagingException ex) {

            System.out.println("EXCEPTION IN LOGIN PAGE: " + ex.getMessage());
            ex.printStackTrace();

            if (ex.getMessage().indexOf("Invalid credentials") != -1) {
                return "Server error:Invalid username or password.";
            } else {
                return "Server error:" + ex.getMessage();
            }

        }
    }

    @Override
    public void logout(String username) {
        loggedInStores.remove(username);
    }

    @Override
    public ArrayList<Folder> getFolderList(String username, String path) {

        ArrayList<Folder> folders = new ArrayList<Folder>();

        try {

            GmailSSLStore store = loggedInStores.get(username);

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
            String parentName = parent.getName();

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

            GmailSSLStore store = loggedInStores.get(username);

            GmailFolder folder = (GmailFolder) store.getFolder(path);

            return getMessages(folder, from, to);

        } catch (MessagingException e) {
            messages.add( new Message( e.getMessage() ) );
        }

        return messages;
    }

    private ArrayList<Message> getMessages(GmailFolder folder, int from, int to) throws MessagingException {

        ArrayList<Message> messages = new ArrayList<Message>();

        FetchProfile profile = new FetchProfile();
        profile.add(GmailFolder.FetchProfileItem.ENVELOPE);

        folder.open(GmailFolder.READ_ONLY);

        if ( folder.getMessageCount() < to ) {
            to = folder.getMessageCount();
        }

        javax.mail.Message[] gmailMessages = folder.getMessages(from,  to);

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

            for (int i = 0, len = recipients.length; i < len; i++) {
                msg.addRecipient( recipients[i].toString() );
            }

            msg.setSentDate(gmailMsg.getSentDate());
            msg.setReceivedDate(gmailMsg.getReceivedDate());
            msg.setRead(gmailMsg.isSet(Flags.Flag.SEEN));
            msg.setPath(folder.getFullName());
            msg.setMessageNumber(gmailMsg.getMessageNumber());

            messages.add(msg);
        }

        messages.sort(new MessageComparator());

        if (folder != null && folder.isOpen()) {
            folder.close(true);
        }

        return messages;
    }

    @Override
    public Message getMessageByNumber(String username, Message msg) {

        try {

            GmailSSLStore store = loggedInStores.get(username);

            GmailFolder folder = (GmailFolder) store.getFolder(msg.getPath());

            folder.open(javax.mail.Folder.READ_WRITE);

            GmailMessage gmailMsg = (GmailMessage) folder.getMessage(msg.getMessageNumber());

            StringBuilder sb = new StringBuilder();

            Multipart multipart;
            MimePart mimePart;
            String messageString;

            if (gmailMsg.getContentType().contains("multipart/")) {
                multipart = (MimeMultipart) gmailMsg.getContent();

                messageString = multipartMessageToString(multipart);
            } else {
                mimePart = (MimeBodyPart) gmailMsg.getContent();
                messagePartToString(mimePart, sb);

                messageString = sb.toString();
            }

            msg.setBody(messageString);

            // set message as seen
            gmailMsg.setFlag(Flags.Flag.SEEN, true);

            msg.setRead( gmailMsg.getFlags().contains(Flags.Flag.SEEN) );

            if (folder != null && folder.isOpen()) {
                folder.close(true);
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

        for (int i = 0, len = partCount; i < len; i++) {
            messagePartToString(message.getBodyPart(i), sb);
        }

        return sb.toString();
    }

    protected void messagePartToString(Part p, StringBuilder sb) throws MessagingException, IOException {

        if (p.isMimeType("text/plain")) {

            if (!p.getContent().toString().equals(null)) {

                sb.append((String)p.getContent());
            }
        } else if (p.isMimeType("multipart/*")) {

            Multipart mp = (Multipart) p.getContent();

            for (int x = 0; x < mp.getCount(); x++) {

                messagePartToString(mp.getBodyPart(x), sb);
            }
        }
    }
}
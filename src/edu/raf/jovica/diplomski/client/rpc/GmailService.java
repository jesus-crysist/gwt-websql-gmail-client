package edu.raf.jovica.diplomski.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import edu.raf.jovica.diplomski.client.data.Folder;
import edu.raf.jovica.diplomski.client.data.Message;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/30/13
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
@RemoteServiceRelativePath("Gmail")
public interface GmailService extends RemoteService {

    public String login(String username, String password);
    public void logout(String username);
    public ArrayList<Folder> getFolderList(String username, String path);
    public ArrayList<Message> getMessagesForPath(String username, String path, int from, int to);
    public Message getMessageByNumber(String username, Message msg);

    /**
     * Utility/Convenience class.
     * Use GmailService.App.getInstance() to access static instance of GmailAsync
     */
    public static class App {
        private static final GmailServiceAsync ourInstance = (GmailServiceAsync) GWT.create(GmailService.class);

        public static GmailServiceAsync getInstance() {
            return ourInstance;
        }
    }
}

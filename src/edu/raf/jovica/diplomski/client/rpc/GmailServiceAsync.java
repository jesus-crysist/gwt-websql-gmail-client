package edu.raf.jovica.diplomski.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
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

public interface GmailServiceAsync {
    public void login(String username, String password, AsyncCallback<String> callback);
    public void logout(String username, AsyncCallback<String> callback);
    public void getFolderList(String username, String path, AsyncCallback<ArrayList<Folder>> async);
    public void getMessagesForPath(String username, String path, int from, int to, AsyncCallback<ArrayList<Message>> async);
    public void getMessageByNumber(String username, Message msg, AsyncCallback<Message> async);
}

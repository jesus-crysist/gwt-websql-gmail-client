package edu.raf.jovica.diplomski.client.data;

import com.google.code.gwt.database.client.GenericRow;
import com.google.code.gwt.database.client.service.*;

import java.util.Collection;
import java.util.List;

/**
 * User: jovica
 * Date: 1/5/14
 * Time: 12:15 PM
 */
@Connection(name = "mailDB", version = "1.0", description = "Database for webmail application", maxsize = 10000)
public interface LocalSQL extends DataService {

    @Update("DROP TABLE folder")
    void removeFolderTable(VoidCallback callback);

    @Update("CREATE TABLE IF NOT EXISTS folder ("
            + "name VARCHAR(255), "
            + "path VARCHAR(1023) NOT NULL PRIMARY KEY, "
            + "newCount INTEGER, "
            + "unreadCount INTEGER, "
            + "totalCount INTEGER, "
            + "parent TINYINT(1) )")
    void createFolderTable(VoidCallback callback);

    @Update(sql="INSERT INTO folder (name, path, newCount, unreadCount, totalCount, parent) " +
            "VALUES ({_.getName()}, {_.getPath()}, {_.getNewMessagesCount()}, {_.getUnreadMessageCount()}" +
            ", {_.getTotalMessagesCount()}, {_.hasParent()})", foreach="folders")
    void insertFolders(Collection<Folder> folders, RowIdListCallback callback);

    @Select(sql="SELECT * FROM folder ORDER BY path")
    void loadAllFolders(ListCallback<GenericRow> callback);

    @Select(sql="SELECT * FROM folder WHERE path IN({folderPaths})")
    void getFoldersByPaths(List<String> folderPaths, ListCallback<GenericRow> callback);

    @Update(sql="UPDATE folder SET newCount={_.getNewMessagesCount()},unreadCount={_.getUnreadMessageCount()}, " +
            "totalCount={_.getTotalMessagesCount()} WHERE path={_.getPath()}", foreach="folders")
    void updateFolderCounts(Collection<Folder> folders, RowIdListCallback callback);

    @Update("DROP TABLE message")
    void removeMessageTable(VoidCallback callback);

    @Update("CREATE TABLE IF NOT EXISTS message ("
            + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + "msgId INTEGER, "
            + "subject VARCHAR(1023), "
            + "sender VARCHAR(1023), "
            + "recipients VARCHAR(10230), "
            + "sentDate INTEGER, "
            + "receivedDate INTEGER, "
            + "path VARCHAR(1023), "
            + "body TEXT, "
            + "isRead TINYINT(1) )")
    void createMessageTable(VoidCallback callback);

    @Update(sql="INSERT INTO message (msgId, subject, sender, recipients, sentDate, receivedDate, path, isRead, body) " +
            "VALUES ({_.getMessageNumber()}, {_.getSubject()}, {_.getSender()}, {_.getRecipientsAsSingleString()}" +
            ", {_.getSentDate()}, {_.getReceivedDate()}, {_.getPath()}, {_.isRead()}, {_.getBody()})", foreach="messages")
    void insertMessages(Collection<Message> messages, RowIdListCallback callback);

    @Update(sql="UPDATE message SET isRead={isRead} WHERE msgId={msgId} AND path={path}")
    void setReadMessage(boolean isRead, int msgId, String path, ListCallback<GenericRow> callback);

    @Select(sql="SELECT * FROM message WHERE msgId IN({messageIds})")
    void getMessagesByIds(List<Integer> messageIds, ListCallback<GenericRow> callback);

    @Update(sql="UPDATE message SET isRead={_.isRead()} WHERE msgId={_.getMessageNumber()} AND path={_.getPath()}", foreach="messages")
    void updateMessagesReadFlag(Collection<Message> messages, ListCallback<GenericRow> callback);

    @Select(sql="SELECT * FROM message WHERE msgId BETWEEN {startIndex} AND {lastIndex} AND path={path} ORDER BY path")
    void loadMessages(int startIndex, int lastIndex, String path, ListCallback<GenericRow> callback);

    @Select(sql="SELECT * FROM message WHERE msgId={msgId} AND path={path}")
    void loadSingleMessage(int msgId, String path, ListCallback<GenericRow> callback);
}

package edu.raf.jovica.diplomski.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

/**
 * User: jovica
 * Date: 11/23/13
 * Time: 11:34 PM
 */
public final class Folder implements IsSerializable {

    @SuppressWarnings(value="unused")
    protected Folder() {}

    public Folder(String name) {
        this.name = name;
        children = new ArrayList<Folder>();
    }

    public Folder(String name, String error) {
        this.name = name;
        this.error = error;
    }

    private String name;
    private String path;
    private int newMessagesCount;
    private int unreadMessagesCount;
    private int totalMessagesCount;
    private ArrayList<Folder> children;
    private String error;


    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getNewMessagesCount() {
        return newMessagesCount;
    }

    public void setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public int getUnreadMessageCount() {
        return unreadMessagesCount;
    }

    public void setUndreadMessageCount(int count) {
        this.unreadMessagesCount = count;
    }

    public int getTotalMessagesCount() {
        return totalMessagesCount;
    }

    public void setTotalMessagesCount(int totalMessagesCount) {
        this.totalMessagesCount = totalMessagesCount;
    }

    public ArrayList<Folder> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Folder> children) {
        this.children = children;
    }

    public void addChild(Folder f) {
        this.children.add(f);
    }

    public boolean hasChildren() {
        return (children.size() > 0);
    }

    public int hasParent() {
        return (path.indexOf('/') != -1 ? 0 : 1);
    }

    public String getError() {
        return error;
    }
}
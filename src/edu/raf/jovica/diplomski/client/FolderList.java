package edu.raf.jovica.diplomski.client;

import com.google.code.gwt.database.client.GenericRow;
import com.google.code.gwt.database.client.service.DataServiceException;
import com.google.code.gwt.database.client.service.ListCallback;
import com.google.code.gwt.database.client.service.RowIdListCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.raf.jovica.diplomski.client.data.Folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/23/13
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class FolderList extends Composite {

    interface FolderListUiBinder extends UiBinder<Widget, FolderList> {}
    private static FolderListUiBinder uiBinder = GWT.create(FolderListUiBinder.class);

    private Webmail parent;
    private TreeItem rootFolder;
    private TreeItem selectedItem;
    private ArrayList<Folder> folderList;
    private NewMailDialog newMailDialog;

    @UiField Button newMailButton;
    @UiField Tree folderTree;

    public FolderList() {
        initWidget(uiBinder.createAndBindUi(this));

        rootFolder = new TreeItem("");

        folderTree.addItem(rootFolder);
    }

    public void refresh(final String mode) {

        final Webmail parent = this.parent;

        rootFolder.setHTML(parent.getUsername() + "@gmail.com");

        reset();

        folderTree.addSelectionHandler(treeItemSelectionHandler);

        if (mode.equals(Diplomski.ONLINE_MODE)) {
            Diplomski.gmailService.getFolderList(parent.getUsername(), "", folderListCallback);
        } else {
            Diplomski.getDatabase().loadAllFolders(foldersFromDBCallback);
        }
    }

    public void reset() {
        rootFolder.removeItems();
    }

    public int getTotalMessageCount() {

        int index = Integer.parseInt(selectedItem.getElement().getAttribute("data-index"));

        return folderList.get(index).getTotalMessagesCount();
    }

    private void loadingFolders() {

        renderList(folderList, rootFolder);

        if (folderList.size() == 0) {
            rootFolder.addItem("No folders");
            rootFolder.setState(true);
            return;
        }

        rootFolder.setState(true);
    }

    private void renderList (ArrayList<Folder> folders, TreeItem root) {

        Folder folder;

        Iterator<Folder> iterator = folders.iterator();

        int index = 0;

        updateFolderDatabase(folders);

        while (iterator.hasNext()) {

            folder = iterator.next();

            String name = folder.getName();
            int count = folder.getUnreadMessageCount();

            TreeItem node = new TreeItem( (count > 0 ? name + "(" + count + ")" : name) );
            Element nodeElement = node.getElement();

            nodeElement.setAttribute("data-path", folder.getPath());
            nodeElement.setAttribute("data-name", name);
            nodeElement.setAttribute("data-index", "" + index);

            index++;

            nodeElement.getFirstChildElement().getStyle().setCursor(Style.Cursor.POINTER);

            root.addItem(node);

            if (count > 0) {
                nodeElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
            }

            if (folder.hasChildren()) {
                renderList(folder.getChildren(), node);
            }
        }
    }

    /**
     * Check if some folders already exist. If they do, update their data.
     * If some don't add those folders to the database.
     * @param folders List of folders to update
     */
    private void updateFolderDatabase(final ArrayList<Folder> folders) {

        final ArrayList<String> paths = new ArrayList<String>();

        for (Folder f : folders) {
            paths.add(f.getPath());
        }

        Diplomski.getDatabase().getFoldersByPaths(paths, new ListCallback<GenericRow>() {
            @Override
            public void onSuccess(List<GenericRow> result) {

                ArrayList<Folder> foldersToAdd = new ArrayList<Folder>();
                ArrayList<Folder> foldersToUpdate = new ArrayList<Folder>();

                if (result.size() == 0) {
                    foldersToAdd = folders;
                } else {

//                    Iterator<Folder> folderIterator = folders.iterator();
//                    Iterator<GenericRow> rowIterator = result.iterator();
//                    Folder f;
//                    GenericRow row;
                    boolean toAdd = true;

                    for (final Folder f : folders) {

                        for (final GenericRow row : result) {

                            if (f.getPath().equals( row.getString("path") )) {

                                if ( f.getTotalMessagesCount() != row.getInt("totalCount") ||
                                        f.getUnreadMessageCount() != row.getInt("unreadCount") ||
                                        f.getNewMessagesCount() != row.getInt("newCount") ) {
                                    foldersToUpdate.add(f);
                                }
                                toAdd = false;
                            }
                        }

                        if (toAdd) {
                            foldersToAdd.add(f);
                            toAdd = true;
                        }
                    }

                    if (foldersToUpdate.size() > 0) {
                        Diplomski.getDatabase().updateFolderCounts(foldersToUpdate, new RowIdListCallback() {
                            @Override
                            public void onSuccess(List<Integer> rowIds) {
                            }

                            @Override
                            public void onFailure(DataServiceException error) {
                                Diplomski.displayError(error.toString());
                            }
                        });
                    }

                }

                if (foldersToAdd.size() > 0) {
                    Diplomski.getDatabase().insertFolders(foldersToAdd, new RowIdListCallback() {
                        @Override
                        public void onSuccess(List<Integer> rowIds) {
                            Diplomski.displayError(rowIds.size() + " folders successfuly written to the database!");
                        }

                        @Override
                        public void onFailure(DataServiceException error) {
                            Diplomski.displayError(error.toString());
                        }
                    });
                }
            }

            @Override
            public void onFailure(DataServiceException error) {
                Diplomski.displayError(error.toString());
            }
        });
    }

    public void setParent(Webmail parent) {
        this.parent = parent;
    }

    public void setMode(String mode) {

        if (mode.equals(Diplomski.ONLINE_MODE)) {
            newMailDialog = new NewMailDialog();

            RootPanel.get().add(newMailDialog);

            newMailDialog.setParent(parent);
        } else {
            newMailButton.setVisible(false);

            if (newMailDialog != null) {
                RootPanel.get().remove(newMailDialog);

                newMailDialog = null;
            }
        }
    }

    public Folder getSelectedFolder() {
        return folderList.get( Integer.parseInt(selectedItem.getElement().getAttribute("data-index")) );
    }

    private Folder getFolderFromPath(ArrayList<Folder> folders, String path, int level) {

        String[] pathParts = path.split("/");

        for (Folder f : folders) {

            if (f.getPath().equals(path)) {
                return f;
            } else if (f.getName().equals(pathParts[level])) {

                return getFolderFromPath(f.getChildren(), path, ++level);
            }
        }

        return null;
    }

    AsyncCallback< ArrayList<Folder> > folderListCallback = new AsyncCallback< ArrayList<Folder> >() {

        @Override
        public void onSuccess(ArrayList<Folder> folders) {

            if (folders.get(0).getError() != null) {
                Diplomski.displayError(folders.get(0).getError());
                parent.logOut();
            } else {

                folderList = folders;

                loadingFolders();
            }
        }

        @Override
        public void onFailure(Throwable caught) {
            Diplomski.displayError(caught.getMessage());
            parent.logOut();
        }
    };

    SelectionHandler<TreeItem> treeItemSelectionHandler = new SelectionHandler<TreeItem>() {

        @Override
        public void onSelection(SelectionEvent<TreeItem> event) {

            TreeItem item = event.getSelectedItem();

            if (selectedItem == item) {
                return;
            }

            if (selectedItem != null) {
                selectedItem.getElement().getFirstChildElement().getStyle().setBackgroundColor("transparent");
            }

            Element el = item.getElement();

            item.setState(true);

            if (el.getInnerText().equals("No folders")) {
                return;
            }

            el.getFirstChildElement().getStyle().setBackgroundColor("lightblue");

            String path = el.getAttribute("data-path");

            if (path.length() > 0) {

                Folder selectedFolder = getFolderFromPath(folderList, path, 0);

                parent.messageList.refresh(parent.getMode(), selectedFolder);
            }

            selectedItem = item;
        }
    };

    ListCallback<GenericRow> foldersFromDBCallback = new ListCallback<GenericRow>() {
        @Override
        public void onSuccess(List<GenericRow> result) {

            Iterator<GenericRow> iterator = result.iterator();
            HashMap<String, Folder> foldersMap = new HashMap<String, Folder>();

            while(iterator.hasNext()) {

                GenericRow row = iterator.next();

                Folder f = new Folder( row.getString("name") );
                f.setPath(row.getString("path"));
                f.setNewMessagesCount( row.getInt("newCount") );
                f.setUndreadMessageCount( row.getInt("unreadCount") );
                f.setTotalMessagesCount( row.getInt("totalCount") );

                String path = row.getString("path");

                if (!path.contains("/")) { // first level folder

                    foldersMap.put(path, f);
                } else { // nested path

                    String[] paths = path.split("/");

                    Folder parent = foldersMap.get(paths[0]);

                    putChildWithRightParent(parent, path, f);
                }

            }

            folderList = (new ArrayList<Folder>());
            folderList.addAll(foldersMap.values());

            loadingFolders();
        }

        @Override
        public void onFailure(DataServiceException error) {
            Diplomski.displayError(error.getMessage());
            parent.logOut();
        }
    };


    private void putChildWithRightParent(Folder parent, String path, Folder child) {

        ArrayList<Folder> children = parent.getChildren();

        int pathParts = path.split("/").length;
        int parentPathParts = parent.getPath().split("/").length;

        if (children.size() != 0 || (pathParts - 1) == parentPathParts) {

            Iterator<Folder> childrenIterator = parent.getChildren().iterator();
            Folder f;

            while(childrenIterator.hasNext()) {

                f = childrenIterator.next();

                if ((pathParts - 1) == f.getPath().split("/").length) {
                    f.addChild(child);
                    return;
                }
            }
        }

        parent.addChild(child);
    }

    @UiHandler("newMailButton")
    public void onNewMailButtonClick (ClickEvent event) {
        newMailDialog.center();
    }
}
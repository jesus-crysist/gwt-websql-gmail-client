package edu.raf.jovica.diplomski.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/23/13
 * Time: 8:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Webmail extends Composite {

    interface WebmailUiBinder extends UiBinder<Widget, Webmail> {}
    private static WebmailUiBinder uiBinder = GWT.create(WebmailUiBinder.class);

    private Diplomski main;
    private String mode;
    private String username;

    @UiField FolderList folders;
    @UiField MessageList messageList;
    @UiField MessageDetails messageDetails;

    public Webmail(Diplomski main) {

        this.main = main;
        this.mode = Diplomski.ONLINE_MODE;

        initWidget(uiBinder.createAndBindUi(this));

        folders.setParent(this);
        messageList.setParent(this);
        messageDetails.setParent(this);
    }

    @SuppressWarnings(value = "unused")
    public Webmail() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void refresh() {
        folders.refresh(this.mode);
    }

    public void reset() {
        folders.reset();
        messageList.reset();
        messageDetails.reset();
    }

    public void logOut() {
        reset();
        this.mode = Diplomski.OFFLINE_MODE;
        main.logOut();
    }

    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;

        folders.setMode(mode);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
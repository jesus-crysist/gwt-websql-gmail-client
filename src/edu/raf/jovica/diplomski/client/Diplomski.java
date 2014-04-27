package edu.raf.jovica.diplomski.client;

import com.google.code.gwt.database.client.Database;
import com.google.code.gwt.database.client.service.DataServiceException;
import com.google.code.gwt.database.client.service.VoidCallback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.raf.jovica.diplomski.client.data.LocalSQL;
import edu.raf.jovica.diplomski.client.rpc.GmailService;
import edu.raf.jovica.diplomski.client.rpc.GmailServiceAsync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/23/13
 * Time: 12:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class Diplomski implements EntryPoint {
    interface DiplomskiUiBinder extends UiBinder<Widget, Diplomski> {}
    private static DiplomskiUiBinder uiBinder = GWT.create(DiplomskiUiBinder.class);

    public static final String ONLINE_MODE = "online";
    public static final String OFFLINE_MODE = "offline";
    public static final String serverUrl = "http://localhost:8080";
    // (1) Create the client proxy. Note that although you are creating the
    // service interface proper, you cast the result to the asynchronous
    // version of the interface. The cast is always safe because the
    // generated proxy implements the asynchronous interface automatically.
    //
    public static final GmailServiceAsync gmailService = (GmailServiceAsync) GWT.create(GmailService.class);

    private LoginForm loginForm;
    private Webmail appPanel;
    private static LocalSQL database;

    @UiField DockLayoutPanel mainPanel;
    @UiField static Label errorLabel;
    @UiField Label usernameLabel;
    @UiField Button logoutButton;

    @Override
    public void onModuleLoad () {

        mainPanel = (DockLayoutPanel) uiBinder.createAndBindUi(this);

        logoutButton.setVisible(false);

        if (!Database.isSupported()) {
            HTMLPanel msgPanel = new HTMLPanel("SQL databse is not supported in your browser.");
            mainPanel.add(msgPanel);
        }

        loginForm = new LoginForm(this);
        appPanel = new Webmail(this);
        database = GWT.create(LocalSQL.class);

        VoidCallback databaseCreationCallback = new VoidCallback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(DataServiceException error) {
                Diplomski.displayError(error.toString());
            }
        };

        // removing databases (temporary)
        database.removeFolderTable(databaseCreationCallback);
        database.removeMessageTable(databaseCreationCallback);

        // creating databases
        database.createFolderTable(databaseCreationCallback);
        database.createMessageTable(databaseCreationCallback);

        Window.enableScrolling(false);
        Window.setMargin("0px");

        mainPanel.add(loginForm);

        Style mainPanelStyle = mainPanel.getElement().getStyle();
        mainPanelStyle.setMargin(20.0, Style.Unit.PX);

        RootLayoutPanel.get().add(mainPanel);
    }

    @UiHandler("logoutButton")
    public void onLogoutClick(ClickEvent event) {
        logOut();
    }

    public void logIn (String username) {

        if (mainPanel.remove(loginForm)) {

            if (username == OFFLINE_MODE) {

                usernameLabel.setText("OFF-LINE MODE");
                logoutButton.setText("Go online");
                logoutButton.setVisible(true);

                this.setMode(OFFLINE_MODE);

            } else {

                usernameLabel.setText("Welcome " + username + "@gmail.com");
                logoutButton.setText("Log out");
                logoutButton.setVisible(true);

                this.setMode(ONLINE_MODE);

            }

            mainPanel.add(appPanel);
            appPanel.setUsername(username);
            appPanel.refresh();

        } else {
            mainPanel.clear();
            mainPanel.add(new Label("Initialization failed"));
        }
    }

    public void logOut() {

        if (mainPanel.remove(appPanel)) {

            Diplomski.gmailService.logout(appPanel.getUsername(), new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {}

                @Override
                public void onSuccess(String result) {
                    appPanel.reset();
                    mainPanel.add(loginForm);
                    usernameLabel.setText("You are not logged in yet.");
                    logoutButton.setVisible(false);
                }
            });

        } else {

            mainPanel.clear();
            mainPanel.add(new Label("Initialization failed"));
        }
    }

    public void setMode(String mode) {
        this.appPanel.setMode(mode);
    }

    public static LocalSQL getDatabase() {
        return Diplomski.database;
    }

    public static final native void log (Object msg) /*-{
        console.log(msg);
    }-*/;

    public static void displayError (String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
        log(text);

        // Hide label after 5 seconds
        Timer t = new Timer() {
            @Override
            public void run() {
                errorLabel.setVisible(false);
            }
        };
        t.schedule(5000);
    }

}
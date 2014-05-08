package edu.raf.jovica.diplomski.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 11/23/13
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginForm extends Composite {

    interface LoginFormUiBinder extends UiBinder<Widget, LoginForm> {}
    private static LoginFormUiBinder uiBinder = GWT.create(LoginFormUiBinder.class);

    Diplomski app;

    @UiField Label messageLabel;
    @UiField TextBox username;
    @UiField PasswordTextBox password;
    @UiField Button loginButton;
    @UiField Button offlineButton;
    @UiField VerticalPanel loginForm;

    public LoginForm (Diplomski app) {

        initWidget(uiBinder.createAndBindUi(this));

        hideErrorMessage();

        this.app = app;

        password.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    onLoginClick(null);
                }
            }
        });
    }

    @UiHandler("loginButton")
    void onLoginClick (ClickEvent ev) {

        if (ev != null) {
            ev.preventDefault();
            ev.stopPropagation();
        }

        String user = username.getValue();
        String pass = password.getValue();

        user = "focuss06";
        pass = "dukicaMaca.";

        if (user == null || user.equals("")) {
            showErrorMessage("Username field can not be empty");
            return;
        }

        if (pass == null || pass.equals("")) {
            showErrorMessage("Password field can not be empty");
            return;
        }

        hideErrorMessage();

        // Make the call. Control flow will continue immediately and later
        // 'callback' will be invoked when the RPC completes.
        Diplomski.gmailService.login(user, pass, loginCallback);
    }

    @UiHandler("offlineButton")
    @SuppressWarnings(value="unused")
    void onOfflineClick (ClickEvent ev) {
        hideErrorMessage();

        app.logIn(Diplomski.OFFLINE_MODE);
    }

    // Create an asynchronous callback to handle the result.
    AsyncCallback<String> loginCallback = new AsyncCallback<String>() {

        public void onSuccess(String result) {

            if (!result.contains("Server error:")) {
                app.logIn(result.replace("Server error:", ""));
            } else {
                showErrorMessage(result);
            }
        }

        public void onFailure(Throwable caught) {
            showErrorMessage(caught.getMessage());
        }
    };

    private void showErrorMessage (String text) {
        Diplomski.log(text);
        messageLabel.setText(text);
        messageLabel.setVisible(true);
    }

    private void hideErrorMessage () {
        messageLabel.setVisible(true);
    }
}
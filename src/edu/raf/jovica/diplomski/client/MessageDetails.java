package edu.raf.jovica.diplomski.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import edu.raf.jovica.diplomski.client.data.Message;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/7/13
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageDetails extends ResizeComposite {

    interface MessageDetailsUiBinder extends UiBinder<Widget, MessageDetails> {}
    private static MessageDetailsUiBinder uiBinder = GWT.create(MessageDetailsUiBinder.class);

    private Webmail parent;

    @UiField Element sender;
    @UiField Element sentDate;
    @UiField Element recipients;
    @UiField Element receivedDate;
    @UiField Element subject;
    @UiField HTML body;

    public MessageDetails() {

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setParent (Webmail parent) {
        this.parent = parent;
    }

    public void loadMessage(String mode, Message msg) {

        if (mode.equals(Diplomski.ONLINE_MODE)) {

            Diplomski.gmailService.getMessageByNumber(parent.getUsername(), msg, messageDetailsCallback);
        }
    }

    AsyncCallback<Message> messageDetailsCallback = new AsyncCallback<Message>() {
        @Override
        public void onFailure(Throwable caught) {
            Diplomski.displayError(caught.getMessage());
            parent.logOut();
        }

        @Override
        public void onSuccess(Message result) {
            sender.setInnerHTML(result.getSender());
            sentDate.setInnerHTML(result.getSentDate().toString());
            recipients.setInnerHTML(result.getRecipientsAsSingleString());
            receivedDate.setInnerHTML(result.getReceivedDate().toString());
            subject.setInnerHTML(result.getSubject());
            body.setHTML(result.getBody());

            parent.messageList.readMessage(result);
        }
    };
}
package edu.raf.jovica.diplomski.client;

import com.google.code.gwt.database.client.GenericRow;
import com.google.code.gwt.database.client.service.DataServiceException;
import com.google.code.gwt.database.client.service.ListCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import edu.raf.jovica.diplomski.client.data.Message;

import java.util.List;

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
            Diplomski.gmailService.setMessageReadFlag(parent.getUsername(), msg, messageDetailsCallback);
        } else {
            Diplomski.getDatabase().loadSingleMessage(msg.getMessageNumber(),
                    msg.getPath(), messageFromDBCallback);
        }
    }

    public void reset() {
        sender.setInnerHTML("");
        recipients.setInnerHTML("");
        sentDate.setInnerHTML("");
        receivedDate.setInnerHTML("");
        subject.setInnerHTML("");
        body.setHTML("");
    }

    private void renderMessage(Message msg) {
        sender.setInnerHTML(msg.getSender());
        sentDate.setInnerHTML( Long.toString(msg.getSentDate()) );
        recipients.setInnerHTML(msg.getRecipientsAsSingleString());
        receivedDate.setInnerHTML( Long.toString(msg.getReceivedDate()) );
        subject.setInnerHTML(msg.getSubject());
        body.setHTML(msg.getBody());

        parent.messageList.readMessage(msg);
    }

    AsyncCallback<Message> messageDetailsCallback = new AsyncCallback<Message>() {
        @Override
        public void onFailure(Throwable caught) {
            Diplomski.displayError(caught.getMessage());
            parent.logOut();
        }

        @Override
        public void onSuccess(final Message result) {
            Diplomski.getDatabase().loadSingleMessage(result.getMessageNumber(),
                    result.getPath(), messageFromDBCallback);
        }
    };

    ListCallback<GenericRow> messageFromDBCallback = new ListCallback<GenericRow>() {
        @Override
        public void onSuccess(List<GenericRow> result) {

            GenericRow row = result.get(0);

            Message m = new Message((long) row.getInt("id"));
            m.setMessageNumber(row.getInt("msgId"));
            m.setSubject(row.getString("subject"));
            m.setSender(row.getString("sender"));
            m.setRecipientsAsString(row.getString("recipients"));
            m.setSentDate(row.getInt("sentDate"));
            m.setReceivedDate(row.getInt("receivedDate"));
            m.setPath(row.getString("path"));
            m.setRead(row.getBoolean("isRead"));

            m.setBody(row.getString("body"));

            renderMessage(m);
        }

        @Override
        public void onFailure(DataServiceException error) {
            Diplomski.displayError(error.getMessage());
            parent.logOut();
        }
    };
}
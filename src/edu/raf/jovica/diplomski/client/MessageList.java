package edu.raf.jovica.diplomski.client;

import com.google.code.gwt.database.client.GenericRow;
import com.google.code.gwt.database.client.service.DataServiceException;
import com.google.code.gwt.database.client.service.ListCallback;
import com.google.code.gwt.database.client.service.RowIdListCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import edu.raf.jovica.diplomski.client.data.Folder;
import edu.raf.jovica.diplomski.client.data.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/1/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class MessageList extends ResizeComposite {

    interface MessageListUiBinder extends UiBinder<Widget, MessageList> {}
    private static MessageListUiBinder uiBinder = GWT.create(MessageListUiBinder.class);

    interface SelectionStyle extends CssResource {
        String selectedRow();
    }

    static final int VISIBLE_EMAIL_COUNT = 10;
    private final String[] columnDimensions = new String[] { "256px", "192px", "", "456px"  };

    public MessageList() {

        initWidget(uiBinder.createAndBindUi(this));

        startIndex = 0;

        initTable();
    }

    protected Webmail parent;
    private Message selectedMessage;
    private ArrayList<Message> currentMessages;
    private int startIndex, selectedRow = -1;

    @UiField FlexTable header;
    @UiField FlexTable table;
    @UiField SelectionStyle rowSelectionStyle;

    public void setParent(Webmail parent) {
        this.parent = parent;
    }

    public void reset() {
        // reset
        table.clear(true);

        if (selectedRow != -1) {
            styleRow(selectedRow, false);
        }

        if (currentMessages != null) {
            currentMessages.clear();
        }
    }

    /**
     * Initializes the table so that it contains enough rows for a full page of
     * emails. Also creates the images that will be used as 'read' flags.
     */
    private void initTable() {

        // Initialize the header.
        header.getColumnFormatter().setWidth(0, columnDimensions[0]);
        header.getColumnFormatter().setWidth(1, columnDimensions[1]);
        header.getColumnFormatter().setWidth(3, columnDimensions[3]);

        header.setText(0, 0, "Sender");
        header.setText(0, 1, "Date");
        header.setText(0, 2, "Subject");
        header.setText(0, 3, "Navigation");
        // header.setWidget(0, 3, navBar);
        header.getCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_RIGHT);

        // Initialize the table.
        table.getColumnFormatter().setWidth(0, columnDimensions[0]);
        table.getColumnFormatter().setWidth(1, columnDimensions[1]);

        table.addClickHandler(rowClickHandler);
    }

    private void updateTable(final ArrayList<Message> messages) {

        final ArrayList<Integer> messageIds = new ArrayList<Integer>();
        Iterator<Message> iterator = messages.iterator();

        while (iterator.hasNext()) {
            Message m = iterator.next();

            messageIds.add(m.getMessageNumber());
        }

        Diplomski.getDatabase().getMessagesByIds(messageIds, new ListCallback<GenericRow>() {
            @Override
            public void onSuccess(List<GenericRow> result) {

                ArrayList<Message> messagesToAdd = new ArrayList<Message>();
                ArrayList<Message> messagesToUpate = new ArrayList<Message>();

                Message m;
                GenericRow row;
                boolean toAdd = true;

                if (result.size() == 0) {
                    messagesToAdd = messages;
                } else {

                    Iterator<Message> messageIterator = messages.iterator();
                    Iterator<GenericRow> rowIterator = result.iterator();

                    while (messageIterator.hasNext()) {
                        m = messageIterator.next();

                        while (rowIterator.hasNext()) {
                            row = rowIterator.next();

                            if (m.getMessageNumber() == row.getInt("msgId")) {

                                if (m.isRead() != row.getBoolean("isRead")) {
                                    messagesToUpate.add(m);
                                }
                                toAdd = false;
                            }
                        }

                        if (toAdd) {
                            messagesToAdd.add(m);
                        }
                    }

                    if (messagesToUpate.size() > 0) {
                        Diplomski.getDatabase().updateMessagesReadFlag(messagesToUpate, new RowIdListCallback() {
                            @Override
                            public void onSuccess(List<Integer> rowIds) {
                            }

                            @Override
                            public void onFailure(DataServiceException error) {
                            }
                        });
                    }
                }

                if (messagesToAdd.size() > 0) {
                    Diplomski.getDatabase().insertMessages(messagesToAdd, new RowIdListCallback() {
                        @Override
                        public void onSuccess(List<Integer> rowIds) {
                            Diplomski.displayError(rowIds.size() + " messages successfuly written to the database!");

                            renderTable();
                        }

                        @Override
                        public void onFailure(DataServiceException error) {
                        }
                    });
                } else {
                    renderTable();
                }
            }

            @Override
            public void onFailure(DataServiceException error) {
                Diplomski.displayError(error.toString());
            }
        });
    }

    private void renderTable() {

        ArrayList<Message> messages = currentMessages;
        int i = 0;

        for (final Message msg  : messages) {

            // Add a new row to the table, then set each of its columns to the
            // email's sender and subject values.
            table.setText(i, 0, msg.getSender());
            table.setText(i, 1, Long.toString(msg.getReceivedDate()) );
            table.setText(i, 2, msg.getSubject());

            Element rowElement = table.getRowFormatter().getElement(i);

            if (!msg.isRead()) {
                rowElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
            }

            rowElement.setAttribute("data-id", String.valueOf(msg.getId()));

            i++;
        }
    }

    private void selectRow (int row) {
        // When a row (other than the first one, which is used as a header) is
        // selected, display its associated MailItem.

        if (currentMessages == null) {
            return;
        }

        Message selectedMessage = currentMessages.get(row);
        if (selectedMessage == null) {
            return;
        }

        // mark it in the local database
        Diplomski.getDatabase().setReadMessage(true, selectedMessage.getMessageNumber(), new RowIdListCallback() {
            @Override
            public void onSuccess(List<Integer> rowIds) {
                Diplomski.displayError("Read flag for selected message successfuly written in local database!");
            }

            @Override
            public void onFailure(DataServiceException error) {
                Diplomski.displayError(error.toString());
            }
        });

        styleRow(selectedRow, false);
        styleRow(row, true);

        selectedRow = row;

        parent.messageDetails.loadMessage(parent.getMode(), selectedMessage);
    }

    private void styleRow(int row, boolean selected) {
        if (row != -1) {
            String style = rowSelectionStyle.selectedRow();

            if (selected) {
                table.getRowFormatter().addStyleName(row, style);
            } else {
                table.getRowFormatter().removeStyleName(row, style);
            }
        }
    }

    public void readMessage (Message message) {

        Message selectedMessage = currentMessages.get(selectedRow);

        if (selectedMessage.getId() == message.getId()) {

            Element rowElement = table.getRowFormatter().getElement(selectedRow);

            if (!message.isRead()) {
                rowElement.getStyle().setFontWeight(Style.FontWeight.BOLD);
            } else {
                rowElement.getStyle().setFontWeight(Style.FontWeight.NORMAL);
            }

        }

    }

    public void refresh(String mode, Folder f) {

        startIndex = f.getTotalMessagesCount() - VISIBLE_EMAIL_COUNT;
        int lastIndex = startIndex + VISIBLE_EMAIL_COUNT;

        reset();

        if (mode.equals(Diplomski.ONLINE_MODE)) {

            Diplomski.gmailService.getMessagesForPath(parent.getUsername(), f.getPath(),
                    startIndex, lastIndex, messageListCallback);
        } else {
            Diplomski.getDatabase().loadMessages(startIndex, lastIndex, messagesFromDBCallback);
        }

    }

    AsyncCallback< ArrayList<Message> > messageListCallback = new AsyncCallback<ArrayList<Message>>() {

        @Override
        public void onSuccess(ArrayList<Message> result) {

            currentMessages = result;

            updateTable(result);
        }

        @Override
        public void onFailure(Throwable caught) {
            Diplomski.displayError(caught.getMessage());
            parent.logOut();
        }
    };

    ListCallback<GenericRow> messagesFromDBCallback = new ListCallback<GenericRow>() {
        @Override
        public void onSuccess(List<GenericRow> result) {

            for (final GenericRow row : result) {

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

                currentMessages.add(m);
            }

            renderTable();
        }

        @Override
        public void onFailure(DataServiceException error) {
            Diplomski.displayError(error.getMessage());
            parent.logOut();
        }
    };

    ClickHandler rowClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {

            HTMLTable.Cell cell = table.getCellForEvent(event);

            if (cell != null) {
                selectRow(cell.getRowIndex());
            }
        }
    };
}
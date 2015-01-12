package edu.raf.jovica.diplomski.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

/**
 * @author jovica
 */
public class NavigationBar extends Composite {
    interface PaginationUiBinder extends UiBinder<Widget, NavigationBar> {}
    private static PaginationUiBinder uiBinder = GWT.create(PaginationUiBinder.class);

    @UiField HTMLPanel rootElement;
    @UiField Hyperlink newerButton;
    @UiField Hyperlink olderButton;
    @UiField InlineHTML fromToText;
    @UiField InlineHTML totalText;

    MessageList parent;

    public NavigationBar(MessageList parent) {
        initWidget(uiBinder.createAndBindUi(this));

        rootElement.setVisible(false);

        this.parent = parent;
    }

    public void setNumbers(int start, int end, int total) {

        totalText.setText( String.valueOf(total) );

        if (end == total) {
            fromToText.setText( String.valueOf(end) );
        } else {
            fromToText.setText(start + "-" + end);
        }

        if (start > 1 && end != total) {
            newerButton.setVisible(true);
            olderButton.setVisible(true);
        } else {
            if (end != total) {
                newerButton.setVisible(false);
                olderButton.setVisible(true);
            } else {
                olderButton.setVisible(false);
            }

            if (start > 1) {
                newerButton.setVisible(true);
                olderButton.setVisible(false);
            } else {
                newerButton.setVisible(false);
            }
        }

        rootElement.setVisible(total > 0);
    }

    public void reset() {
        rootElement.setVisible(false);
    }

    @UiHandler("newerButton")
    void onNewerButtonClick(ClickEvent ev) {
        parent.loadNewerMessages();
    }

    @UiHandler("olderButton")
    void onOlderButtonClick(ClickEvent ev) {
        parent.loadOlderMessages();
    }
}
package edu.raf.jovica.diplomski.client.data;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created with IntelliJ IDEA.
 * User: jovica
 * Date: 12/1/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class User implements IsSerializable {

    public User() {}

    public User(String fullName, String emailAddress) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
    }

    private String fullName;
    private String emailAddress;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}

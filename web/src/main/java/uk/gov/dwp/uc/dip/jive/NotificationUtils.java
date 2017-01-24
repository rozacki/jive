package uk.gov.dwp.uc.dip.jive;


import com.vaadin.ui.Notification;

import static com.vaadin.ui.Notification.Type.HUMANIZED_MESSAGE;
import static com.vaadin.ui.Notification.Type.WARNING_MESSAGE;

public class NotificationUtils {

    public static void displayError(Exception e){
        Notification.show(e.getLocalizedMessage(), WARNING_MESSAGE);
    }

    public static void displayError(String message){
        Notification.show(message, WARNING_MESSAGE);
    }

    public static void displayInfo(String message){
        Notification.show(message, HUMANIZED_MESSAGE);
    }
}

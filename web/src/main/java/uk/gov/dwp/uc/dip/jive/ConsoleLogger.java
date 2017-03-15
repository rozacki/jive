package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.TextArea;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Created by chrisrozacki on 07/03/2017.
 */
public class ConsoleLogger extends Logger {

    // It's usually a good idea to add a dot suffix to the fully
    // qualified class name. This makes caller localization to work
    // properly even from classes that have almost the same fully
    // qualified class name as MyLogger, e.g. MyLoggerTest.
    static String FQCN = ConsoleLogger.class.getName() + ".";

    // It's enough to instantiate a factory once and for all.
    TextArea Console;

    final StringBuilder consoleStringBuilder = new StringBuilder();

    public ConsoleLogger(String name, TextArea console){
        super(name);
        Console = console;
    }

    /**
     Overrides the standard debug method by appending " world" at the
     end of each message.  */
    public void debug(Object message) {
        if(message==null)
            return;
        // log both to log and  console
        super.log(FQCN, Level.DEBUG, message, null);
        logToConsole(message.toString());
    }

    public void error(Object message){
        // log both to log and  console
        super.log(FQCN, Level.ERROR, message, null);
        logToConsole(message.toString());
    }

    void logToConsole(String... values){
        for(String string: values){
            consoleStringBuilder.append(string).append(": ");
        }
        Console.setReadOnly(false);
        Console.setValue(consoleStringBuilder.append('\n').toString());
        Console.setReadOnly(true);
        Console.setCursorPosition(Console.getValue().length());
    }
}

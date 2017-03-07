package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.TextArea;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Created by chrisrozacki on 07/03/2017.
 */
public class Log4JFactory implements LoggerFactory {
    TextArea Console;
    public Log4JFactory(TextArea console){
        Console = console;
    }
    @Override
    public Logger makeNewLoggerInstance(String arg0){
        return new ConsoleLogger(arg0, Console);
    }
}

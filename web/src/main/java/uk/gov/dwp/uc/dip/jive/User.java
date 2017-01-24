package uk.gov.dwp.uc.dip.jive;

import com.vaadin.server.VaadinServlet;
import org.apache.ambari.view.ViewContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

public class User {
    private final static Logger log = Logger.getLogger(User.class);

    public static String getUserName() {
        ServletContext servletContext = VaadinServlet.getCurrent().getServletContext();
        ViewContext context = (ViewContext) servletContext.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);
        if (context != null) {
            log.info("returning username from Ambari context.");
            return context.getUsername();
        }else{
            log.info("returning default user.");
            return Properties.getInstance().getJiveDevUser();
        }
    }

}

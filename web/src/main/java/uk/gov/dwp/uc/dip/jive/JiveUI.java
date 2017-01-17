package uk.gov.dwp.uc.dip.jive;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@SuppressWarnings("WeakerAccess")
@Theme("mytheme")
public class JiveUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        TabSheet tabSheet = new TabSheet();
        MainPanel mp = new MainPanel();
        tabSheet.addTab(mp,"Generate SQL");

        setContent(tabSheet);
    }

    @WebServlet(urlPatterns = "/*", name = "JiveServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = JiveUI.class, productionMode = false)
    public static class JiveServlet extends VaadinServlet {
    }
}

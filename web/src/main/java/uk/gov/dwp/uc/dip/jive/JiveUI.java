package uk.gov.dwp.uc.dip.jive;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveResultsPanel;

import javax.servlet.annotation.WebServlet;

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
    private final static Logger log = Logger.getLogger(JiveUI.class);

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        log.debug("User request:" + User.getUserName());
        TabSheet tabSheet = new TabSheet();
        MainPanel mp = new MainPanel();
        tabSheet.addTab(mp, "Generate SQL");

        HiveResultsPanel hrp = new HiveResultsPanel();
        tabSheet.addTab(hrp, "Run Results");
        tabSheet.getTab(1).setEnabled(false);

        mp.setHiveResultsPanel(hrp);
        mp.setTabSheet(tabSheet);
        setContent(tabSheet);
    }

    @WebServlet(urlPatterns = "/*", name = "JiveServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = JiveUI.class, productionMode = false)
    public static class JiveServlet extends VaadinServlet {
    }
}

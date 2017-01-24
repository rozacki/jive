package uk.gov.dwp.uc.dip.jive.hiverun;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.apache.log4j.Logger;

import static uk.gov.dwp.uc.dip.jive.hiverun.StatementResult.BeanProperties.ORDER;
import static uk.gov.dwp.uc.dip.jive.hiverun.StatementResult.BeanProperties.STATEMENT;
import static uk.gov.dwp.uc.dip.jive.hiverun.StatementResult.BeanProperties.SUCCESS;

public class HiveResultsPanel extends Panel{

    private static final Logger log = Logger.getLogger(HiveResultsPanel.class);
    private BeanItemContainer<StatementResult> container;

    public HiveResultsPanel() {
        super();
        log.debug("Building results panel");
        VerticalLayout layout = new VerticalLayout();
        container = new BeanItemContainer<>(StatementResult.class);
        Grid grid = new Grid();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setContainerDataSource(container);
        grid.setColumnOrder(ORDER, SUCCESS, STATEMENT);
        grid.setRowDescriptionGenerator((Grid.RowDescriptionGenerator)
                row -> ("<pre>"
                        + row.getItem().getItemProperty(STATEMENT).getValue().toString())
        + "</pre>");
        grid.setRowStyleGenerator((Grid.RowStyleGenerator) row -> {
            if (!(boolean)row.getItem().getItemProperty(SUCCESS).getValue()){
                return "v-grid-row-error";
            }
            return "v-grid-row-ok";
        });
        layout.addComponent(grid);
        this.setContent(layout);

    }

    public void reset(){
        container.removeAllItems();
    }

    public BeanItemContainer<StatementResult> getContainer() {
        return container;
    }
}

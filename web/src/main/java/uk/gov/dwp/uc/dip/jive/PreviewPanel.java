package uk.gov.dwp.uc.dip.jive;


import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveProxyExecutor;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveResultsPanel;
import uk.gov.dwp.uc.dip.schemagenerator.SchemaGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chrisrozacki on 27/02/2017.
 */
public class PreviewPanel extends Panel {

    private static final Logger log = Logger.getLogger(MainPanel.class);
    private String mapFile;
    private String hdfsLocation;
    private String databaseName;
    private MainPanel mainPanel;

    PreviewPanel(MainPanel mainPanel, HiveResultsPanel sqlResultPanel) {
        super();
        final VerticalLayout layout = new VerticalLayout();
        HorizontalLayout buttonBar = new HorizontalLayout();
        layout.addComponent(buttonBar);

        DataGrid previewGrid= new DataGrid();
        layout.addComponent(previewGrid);

        Button previewButton = new Button("Preview");
        buttonBar.addComponent(previewButton);

        ComboBox targetTablesCombo = new ComboBox();
        buttonBar.addComponent(targetTablesCombo);

        previewButton.addClickListener((Button.ClickListener) event->{
            log.debug("Run preview pressed.");
            try{

                HiveProxyExecutor hpe = new HiveProxyExecutor();

                List<String> allStatements = new ArrayList<String>();
                allStatements.add(String.format("select * from %s limit 100",(String)targetTablesCombo.getValue()));
                if(Properties.getInstance().isHiveAuthenticationDisabled()) {
                    List<List<Object>> table = hpe.executeMultipleStatementsNoAuth(allStatements, mainPanel.processFilePanel.getDataBaseName(), sqlResultPanel.getContainer());
                    previewGrid.setData(table);
                }else{
                    List<List<Object>> table = hpe.executeMultipleStatements(allStatements, mainPanel.processFilePanel.getDataBaseName(), sqlResultPanel.getContainer());
                    previewGrid.setData(table);
                }

            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        Button refreshTargetTables = new Button("Refresh target tables");
        buttonBar.addComponent(refreshTargetTables);
        refreshTargetTables.addClickListener((Button.ClickListener) event->{
            log.debug("Run refresh target table pressed.");
            try{
                SchemaGenerator schemaGenerator = new SchemaGenerator(mainPanel.mappingFileUploader.getFilePath(), mainPanel.getLocation());
                // As we generate preview per table this is just work around to take first available table
                // Ideally we should choose target table from the list
                Set<String> targetTables = schemaGenerator.techMap.getTargetTables();
                targetTablesCombo.addItems(targetTables);
            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        }
        );

        this.setContent(layout);
    }
}

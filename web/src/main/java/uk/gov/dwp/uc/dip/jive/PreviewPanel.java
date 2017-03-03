package uk.gov.dwp.uc.dip.jive;


import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveProxyExecutor;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveResultsPanel;
import uk.gov.dwp.uc.dip.jive.hiverun.StatementResult;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.schemagenerator.SchemaGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chrisrozacki on 27/02/2017.
 */
public class PreviewPanel extends Panel {
    private static final Logger log = Logger.getLogger(MainPanel.class);

    PreviewPanel(MainPanel mainPanel, HiveResultsPanel sqlResultPanel) {
        super();
        final HiveProxyExecutor hpe = new HiveProxyExecutor();
        final VerticalLayout layout = new VerticalLayout();

        HorizontalLayout previewButtonBar = new HorizontalLayout();
        layout.addComponent(previewButtonBar);

        DataGrid previewGrid= new DataGrid();
        layout.addComponent(previewGrid);

        Button previewButton = new Button("Preview");
        previewButtonBar.addComponent(previewButton);

        DataGrid statsGrid = new DataGrid();
        layout.addComponent(statsGrid);

        ComboBox targetTablesCombo = new ComboBox();
        previewButtonBar.addComponent(targetTablesCombo);
        Property.ValueChangeListener listener = new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                String targetTable = (String)targetTablesCombo.getValue();
                log.debug("Change target table selection to " + targetTable);

                loadStats(statsGrid, mainPanel.processFilePanel.getDataBaseName(), targetTable, mainPanel.mappingFileUploader.getFilePath()
                        ,hpe, sqlResultPanel.getContainer());
            }
        };
        targetTablesCombo.addValueChangeListener(listener);

        previewButton.addClickListener((Button.ClickListener) event->{
            String targetTable = (String)targetTablesCombo.getValue();
            log.debug("Run preview pressed for table " + targetTable);
            try{
                List<List<Object>> table =  hpe.executeSingleStatement(String.format("select * from %s limit 100"
                        ,targetTable), mainPanel.processFilePanel.getDataBaseName()
                        , sqlResultPanel.getContainer());
                previewGrid.setContent(table);
            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        Button refreshTargetTables = new Button("Refresh target tables list");
        previewButtonBar.addComponent(refreshTargetTables);
        refreshTargetTables.addClickListener((Button.ClickListener) event->{
            log.debug("Run refresh target table pressed.");
            try{
                SchemaGenerator schemaGenerator = new SchemaGenerator(mainPanel.mappingFileUploader.getFilePath(), mainPanel.getLocation());
                Set<String> targetTables = schemaGenerator.techMap.getTargetTables();
                targetTablesCombo.addItems(targetTables);
            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        }
        );

        HorizontalLayout statsButtonBar = new HorizontalLayout();
        Button refereshStats    =   new Button("Refresh statistics");
        statsButtonBar.addComponent(refereshStats);
        layout.addComponent(refereshStats);

        refereshStats.addClickListener((Button.ClickListener) event->{
            String targetTable = (String)targetTablesCombo.getValue();
            log.debug("Refresh statistics pressed for table " + targetTable);
            hpe.executeSingleStatement(String.format("ANALYZE TABLE %s COMPUTE STATISTICS FOR COLUMNS",targetTable)
                    , mainPanel.processFilePanel.getDataBaseName(),sqlResultPanel.getContainer());

            loadStats(statsGrid, mainPanel.processFilePanel.getDataBaseName(), targetTable, mainPanel.mappingFileUploader.getFilePath()
                    ,hpe, sqlResultPanel.getContainer());
        });

        this.setContent(layout);
    }

    public void loadStats(DataGrid statsGrid, String databaseName, String targetTable, String mappingPath
            , HiveProxyExecutor hpe, BeanItemContainer<StatementResult> container) {
        log.debug("Change target table selection to " + targetTable);

        try{
            SchemaGenerator schemaGenerator = new SchemaGenerator(mappingPath, null);
            String statsSQLTemplate = "describe formatted %s.%s";
            List<List<Object>> allStats = new ArrayList<>();
            List<Object>  columnNames = new ArrayList<>();
            columnNames.add("column name");
            columnNames.add("data_type");
            columnNames.add("min");
            columnNames.add("max");
            columnNames.add("num_nulls");
            columnNames.add("distinct_count");
            columnNames.add("avg_col_len");
            columnNames.add("max_col_len");
            columnNames.add("num_trues");
            columnNames.add("num_falses");
            columnNames.add("comment");
            allStats.add(columnNames);
            for(TechnicalMapping columnMapping: schemaGenerator.techMap.getTargetColumns(targetTable)){
                List<List<Object>> stats  = hpe.executeSingleStatement(String.format(statsSQLTemplate,targetTable
                        , columnMapping.targetFieldName)
                        , databaseName,container);
                if(stats !=null && stats.size()==4) {
                    allStats.add(stats.get(3));
                }
            }
            statsGrid.setContent(allStats);
        }
        catch(Exception e) {
            e.printStackTrace();
            NotificationUtils.displayError(e);
            log.error(e);
        }
    }
}

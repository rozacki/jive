package uk.gov.dwp.uc.dip.jive;


import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Sizeable;
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
    final TextArea console = new TextArea("ConsoleLogger");
    final StringBuilder consoleStringBuilder = new StringBuilder();

    PreviewPanel(MainPanel mainPanel, HiveResultsPanel sqlResultPanel) {
        super();
        final HiveProxyExecutor hpe = new HiveProxyExecutor();

        final VerticalLayout mainLayout = new VerticalLayout();
        final VerticalLayout previewLayout = new VerticalLayout();
        final VerticalLayout statsLayout = new VerticalLayout();
        final VerticalLayout consoleLayout = new VerticalLayout();
        //final VerticalSplitPanel splitPanelUpper = new VerticalSplitPanel();
        //final VerticalSplitPanel splitPanelLower = new VerticalSplitPanel();
        final HorizontalLayout previewButtonBar = new HorizontalLayout();
        final DataGrid previewGrid = new DataGrid();
        final Button previewButton = new Button("Preview");
        final HorizontalLayout statsButtonBar = new HorizontalLayout();
        final DataGrid statsGrid = new DataGrid();
        final ComboBox targetTablesCombo = new ComboBox();
        final Button refreshTargetTablesButton = new Button("Refresh target tables list");
        final Button refereshStatsButton    =   new Button("Refresh statistics");


        this.setContent(mainLayout);
        //this.setContent(splitPanelUpper);
        //splitPanelUpper.setFirstComponent(previewLayout);
        //splitPanelUpper.setSecondComponent(splitPanelLower);
        //splitPanelLower.setFirstComponent(statsLayout);
        //splitPanelLower.setSecondComponent(consoleLayout);
        //splitPanelUpper.setSplitPosition(40, Unit.PERCENTAGE);
        //splitPanelLower.setSplitPosition(60, Unit.PERCENTAGE);

        mainLayout.addComponent(previewLayout);
        previewLayout.addComponent(previewButtonBar);
        previewLayout.addComponent(previewGrid);
        previewButtonBar.addComponent(targetTablesCombo);
        previewButtonBar.addComponent(previewButton);
        previewButtonBar.addComponent(refreshTargetTablesButton);

        mainLayout.addComponent(statsLayout);
        statsLayout.addComponent(statsButtonBar);
        statsLayout.addComponent(statsGrid);
        statsButtonBar.addComponent(refereshStatsButton);

        mainLayout.addComponent(consoleLayout);
        consoleLayout.addComponent(console);

        this.setSizeFull();
        this.setHeight("100%");
        //splitPanelUpper.setSizeFull();
        //splitPanelUpper.setHeight("100%");
        previewGrid.setSizeFull();
        statsGrid.setSizeFull();
        console.setWordwrap(true);
        console.setSizeFull();
        console.setReadOnly(true);

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
                String sql=String.format("select * from %s limit 100",targetTable);
                logToConsole("getting preview 100 rows", sql);
                List<List<Object>> table =  hpe.executeSingleStatement(sql, mainPanel.processFilePanel.getDataBaseName()
                        , sqlResultPanel.getContainer());
                logToConsole("fetched rows", String.valueOf(table.size()));
                previewGrid.setContent(table);
            }catch(Exception e){
                logToConsole("error getting preview",e.toString());
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        refreshTargetTablesButton.addClickListener((Button.ClickListener) event->{
            log.debug("Run refresh target table pressed.");
            try{
                SchemaGenerator schemaGenerator = new SchemaGenerator(mainPanel.mappingFileUploader.getFilePath(), mainPanel.getLocation());
                Set<String> targetTables = schemaGenerator.techMap.getTargetTables();
                targetTablesCombo.removeAllItems();
                targetTablesCombo.addItems(targetTables);
            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        refereshStatsButton.addClickListener((Button.ClickListener) event->{
            String targetTable = (String)targetTablesCombo.getValue();
            String sql = String.format("ANALYZE TABLE %s COMPUTE STATISTICS FOR COLUMNS",targetTable);
            log.debug("Refresh statistics pressed for table " + targetTable);
            logToConsole("refresh statistics for table",targetTable,sql);
            hpe.executeSingleStatement(sql, mainPanel.processFilePanel.getDataBaseName(),sqlResultPanel.getContainer());

            loadStats(statsGrid, mainPanel.processFilePanel.getDataBaseName(), targetTable, mainPanel.mappingFileUploader.getFilePath()
                    ,hpe, sqlResultPanel.getContainer());
        });
    }

    /***
     *
     * @param statsGrid
     * @param databaseName
     * @param targetTable
     * @param mappingPath
     * @param hpe
     * @param container
     */
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
            columnNames.add("num nulls");
            columnNames.add("distinct count");
            columnNames.add("avg col len");
            columnNames.add("max col len");
            columnNames.add("num trues");
            columnNames.add("num falses");
            columnNames.add("comment");
            allStats.add(columnNames);
            for(TechnicalMapping columnMapping: schemaGenerator.techMap.getTargetColumns(targetTable)){
                String sql = String.format(statsSQLTemplate,targetTable, columnMapping.targetFieldName);
                logToConsole("loading statistics for column",columnMapping.targetFieldName,sql);
                List<List<Object>> stats  = hpe.executeSingleStatement(sql, databaseName,container);
                if(stats !=null && stats.size()==4) {
                    allStats.add(stats.get(3));
                }
            }
            statsGrid.setContent(allStats);
        }
        catch(Exception e) {
            logToConsole(e.toString());
            e.printStackTrace();
            NotificationUtils.displayError(e);
        }
    }
    void logToConsole(String... values){
        for(String string: values){
            consoleStringBuilder.append(string).append(": ");
        }
        console.setReadOnly(false);
        console.setValue(consoleStringBuilder.append('\n').toString());
        console.setReadOnly(true);
        console.setCursorPosition(console.getValue().length());
    }
}

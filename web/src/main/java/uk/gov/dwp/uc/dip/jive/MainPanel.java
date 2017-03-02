package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.*;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveResultsPanel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class MainPanel extends Panel {
    private static final Logger log = Logger.getLogger(MainPanel.class);

    public ProcessFilePanel processFilePanel;
    public MappingFileUploader mappingFileUploader;
    private TextField dataLocationTextField;

    MainPanel() {
        super();
        final VerticalLayout layout = new VerticalLayout();
        final HorizontalLayout mappingLayout = new HorizontalLayout();
        log.debug("Building main panel.");
        dataLocationTextField = new TextField("Data Location:");
        dataLocationTextField.setWidth(20, Unit.EM);
        dataLocationTextField.setDescription("Location for JSON files in HDFS");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dataLocation = Properties.getInstance().getDataLocation()
                + dateFormat.format(new Date());
        dataLocationTextField.setValue(dataLocation);

        // schema tree
        Tree schemaTree = new Tree();
        schemaTree.setCaption("Schema");
        schemaTree.setVisible(false);
        JSONSchemaLoader loadButton = new JSONSchemaLoader("Load schema", schemaTree);

        Table mappingGrid= new Table();
        //sourceDB,sourceCollection,sourceFieldLocation,sourceDataType,destinationTable,destinationField,destinationDataType,function,dataQuality
        mappingGrid.addContainerProperty("source DB", String.class, null);
        mappingGrid.addContainerProperty("source collection", String.class, null);
        mappingGrid.addContainerProperty("source field location", String.class, null);
        mappingGrid.addContainerProperty("destination table", String.class, null);
        mappingGrid.addContainerProperty("destination column", String.class, null);
        mappingGrid.addContainerProperty("destination data type", String.class, null);
        mappingGrid.addContainerProperty("destination function", String.class, null);
        mappingGrid.addContainerProperty("destination DQ", String.class, null);
        mappingGrid.setVisible(false);

        mappingFileUploader = new MappingFileUploader("Upload the Mapping File:");
        layout.addComponent(dataLocationTextField);
        layout.addComponent(mappingFileUploader);
        layout.addComponent(mappingLayout);
        layout.addComponent(mappingGrid);
        layout.addComponent(loadButton);

        mappingLayout.addComponent(schemaTree);
        mappingLayout.addComponent(mappingGrid);

        processFilePanel = new ProcessFilePanel();
        processFilePanel.setVisible(false);
        layout.addComponent(processFilePanel);

        layout.setMargin(true);
        layout.setSpacing(true);
        this.setContent(layout);
        this.setCaption("Settings and File Upload");

        mappingFileUploader.addSucceededListener((Upload.SucceededListener) event -> {
            processFilePanel.setOriginalFileName(mappingFileUploader.getOriginalFileName());
            processFilePanel.setTempFilePath(mappingFileUploader.getFilePath());
            processFilePanel.setStatus(ProcessFilePanel.StatusEnum.FILE_UPLOADED);
            processFilePanel.setJsonSourcePath(dataLocationTextField.getValue());
        });
    }

    void setHiveResultsPanel(HiveResultsPanel hiveResultsPanel) {
        processFilePanel.setHiveResultsPanel(hiveResultsPanel);
    }

    void setTabSheet(TabSheet tabSheet) {
        processFilePanel.setTabSheet(tabSheet);
    }

    public String getLocation(){
        return dataLocationTextField.getValue();
    }
}

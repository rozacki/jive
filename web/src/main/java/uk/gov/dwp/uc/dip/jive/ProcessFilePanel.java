package uk.gov.dwp.uc.dip.jive;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveProxyExecutor;
import uk.gov.dwp.uc.dip.jive.hiverun.HiveResultsPanel;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingValidator;
import uk.gov.dwp.uc.dip.schemagenerator.SchemaGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static java.util.Arrays.*;


class ProcessFilePanel extends Panel{

    private String jsonSourcePath;
    private HiveResultsPanel hiveResultsPanel;
    private TabSheet tabSheet;

    void setJsonSourcePath(String jsonSourcePath) {
        this.jsonSourcePath = jsonSourcePath;
    }

    void setHiveResultsPanel(HiveResultsPanel hiveResultsPanel) {
        this.hiveResultsPanel = hiveResultsPanel;
    }

    void setTabSheet(TabSheet tabSheet) {
        this.tabSheet = tabSheet;
    }

    enum StatusEnum{
        FILE_UPLOADED("File uploaded.",true),
        FILE_VERIFIED("Valid Mapping File.", true),
        FILE_INVALID("File Verification Failed.", false),
        FILE_GENERATED("SQL File Generated.", true);

        private String description;
        private boolean is_success;

        StatusEnum(String description, boolean is_success) {
            this.description = description;
            this.is_success = is_success;
        }

        public String getDescription() {
            return description;
        }
    }

    private final static Logger log = Logger.getLogger(ProcessFilePanel.class);
    private String tempFilePath;
    private String scriptFilePath;
    private Label statusLabel;
    private Button validateButton;
    private Button generateButton;
    private Button runButton;
    private Button downloadButton;
    private SchemaGenerator schemaGenerator;
    private TechnicalMappingValidator mappingValidator;
    private Label errorText;
    private PopupView errorPopup;
    private Button previewButton;
    private DataGrid dataGrid;

    ProcessFilePanel() {
        super();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addStyleName("v-jive-padding-left");
        statusLabel = new Label();

        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.addStyleName("v-jive-padding-bottom");
        Label emptyLabel = new Label();
        emptyLabel.setSizeFull();
        emptyLabel.setVisible(false);

        // Hive database to run sql against if run button pressed
        TextField runDatabaseTextField = new TextField("Enter Database Name:");
        runDatabaseTextField.setVisible(false);

        // Error popup - Used to display file validation errors.
        errorText = new Label();
        errorText.setSizeFull();

        errorPopup = new PopupView(new PopupView.Content() {
            @Override
            public String getMinimizedValueAsHTML() {
                return "";
            }

            @Override
            public Component getPopupComponent() {
                return errorText;
            }
        });

        errorPopup.setPopupVisible(false);
        verticalLayout.addComponent(errorPopup);
        verticalLayout.addStyleName("v-margin-bottom");

        validateButton = new Button("1. Validate File");
        validateButton.addClickListener((Button.ClickListener) event -> {
            log.debug("Validate button pressed.");
            mappingValidator = new TechnicalMappingValidator();
            if(mappingValidator.isFileValid(tempFilePath)){
                setStatus(StatusEnum.FILE_VERIFIED);
                errorPopup.setPopupVisible(false);
                tabSheet.getTab(1).setEnabled(false);
            }else{
                setStatus(StatusEnum.FILE_INVALID);

                StringBuilder error = new StringBuilder();
                for(String message : mappingValidator.getErrors()){
                    error.append(message).append("\n");
                }
                errorText.setContentMode(ContentMode.PREFORMATTED);
                errorText.setValue(error.toString());
                errorText.addStyleName("v-label-failure");
                errorPopup.setPopupVisible(true);
                runDatabaseTextField.setVisible(false);
                emptyLabel.setVisible(false);
                tabSheet.getTab(1).setEnabled(false);
            }
        });

        generateButton = new Button("2. Generate SQL");
        generateButton.addClickListener((Button.ClickListener) event -> {
            try {
                log.debug("Generate button pressed.");
                schemaGenerator = new SchemaGenerator(tempFilePath, jsonSourcePath);
                scriptFilePath = Properties.getInstance().getScriptsPath()
                        + UUID.randomUUID().toString() + ".sql";
                log.debug("Writing:" + scriptFilePath);
                File script = new File(scriptFilePath);
                FileWriter writer = new FileWriter(script);
                writer.write(schemaGenerator.transformAll());
                writer.flush();
                writer.close();
                setStatus(StatusEnum.FILE_GENERATED);
                runDatabaseTextField.setVisible(true);
                emptyLabel.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        runButton = new Button("4. Run SQL");
        runButton.addClickListener((Button.ClickListener) event->{
            log.debug("Run button pressed.");
            try {
                schemaGenerator = new SchemaGenerator(tempFilePath, jsonSourcePath);
                List<String> allStatements = schemaGenerator.transformAllToList();
                tabSheet.getTab(1).setEnabled(true);
                tabSheet.setSelectedTab(1);
                HiveProxyExecutor hpe = new HiveProxyExecutor();
                hiveResultsPanel.reset();
                if(Properties.getInstance().isHiveAuthenticationDisabled()) {
                    hpe.executeMultipleStatementsNoAuth(allStatements, runDatabaseTextField.getValue(), hiveResultsPanel.getContainer());
                }else{
                    hpe.executeMultipleStatements(allStatements, runDatabaseTextField.getValue(), hiveResultsPanel.getContainer());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        previewButton = new Button("5. Preview");
        previewButton.addClickListener((Button.ClickListener) event->{
            log.debug("Run preview pressed.");
            try{
                schemaGenerator = new SchemaGenerator(tempFilePath, jsonSourcePath);
                // As we generate preview per table this is just work around to take first available table
                // Ideally we should choose target table from the list
                Set<String>  targetTables = schemaGenerator.techMap.getTargetTables();
                HiveProxyExecutor hpe = new HiveProxyExecutor();
                targetTables.toArray();
                List<String> allStatements = new ArrayList<String>();
                allStatements.add(String.format("select * from %s limit 100",targetTables.toArray()[0]));
                if(Properties.getInstance().isHiveAuthenticationDisabled()) {
                    List<List<Object>> table = hpe.executeMultipleStatementsNoAuth(allStatements, runDatabaseTextField.getValue(), hiveResultsPanel.getContainer());
                    dataGrid.setData(table);
                }else{
                    List<List<Object>> table = hpe.executeMultipleStatements(allStatements, runDatabaseTextField.getValue(), hiveResultsPanel.getContainer());
                    dataGrid.setData(table);
                }

                /*
                List<List<Object>> table = new ArrayList<>();
                List<Object> innerList = new ArrayList<>(3);
                innerList.add("column1");
                innerList.add("column2");
                innerList.add("column3");
                table.add(innerList);
                innerList = new ArrayList<>(3);
                innerList.add(new Integer(1));
                innerList.add(new Integer(2));
                innerList.add(new Integer(3));
                table.add(innerList);

                innerList = new ArrayList<>(3);
                innerList.add(new Integer(11));
                innerList.add(new Integer(22));
                innerList.add(new Integer(33));
                table.add(innerList);
                */

            }catch(Exception e){
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        downloadButton = new Button("3. Download SQL");
        StreamResource myResource = createResource();
        FileDownloader fileDownloader = new FileDownloader(myResource);
        fileDownloader.extend(downloadButton);

        dataGrid= new DataGrid();

        buttonBar.addComponent(validateButton);
        buttonBar.addComponent(generateButton);
        buttonBar.addComponent(downloadButton);
        buttonBar.addComponent(runButton);
        buttonBar.addComponent(previewButton);
        buttonBar.addComponent(dataGrid);

        verticalLayout.addComponent(statusLabel);
        verticalLayout.addComponent(new Label(""));
        verticalLayout.addComponent(runDatabaseTextField);
        verticalLayout.addComponent(emptyLabel);
        verticalLayout.addComponent(buttonBar);
        verticalLayout.addComponent(new Label());

        this.setContent(verticalLayout);

    }

    void setStatus(StatusEnum status){
        statusLabel.setWidth(30, Unit.EM);
        statusLabel.setValue(status.getDescription());
        //Notification.show(status.getDescription(), HUMANIZED_MESSAGE);
        if(status.is_success){
            statusLabel.addStyleName("v-label-success");
            statusLabel.removeStyleName("v-label-failure");
        }else{
            statusLabel.addStyleName("v-label-failure");
            statusLabel.removeStyleName("v-label-success");
        }

        switch(status){
            case FILE_UPLOADED:
                validateButton.setEnabled(true);
                generateButton.setEnabled(false);
                runButton.setEnabled(false);
                downloadButton.setEnabled(false);
                previewButton.setEnabled(false);
                setVisible(true);
                break;
            case FILE_VERIFIED:
                validateButton.setEnabled(true);
                generateButton.setEnabled(true);
                runButton.setEnabled(false);
                downloadButton.setEnabled(false);
                previewButton.setEnabled(false);
                break;
            case FILE_INVALID:
                errorPopup.setPopupVisible(true);
                validateButton.setEnabled(true);
                generateButton.setEnabled(false);
                runButton.setEnabled(false);
                downloadButton.setEnabled(false);
                previewButton.setEnabled(false);
                break;
            case FILE_GENERATED:
                validateButton.setEnabled(true);
                generateButton.setEnabled(true);
                runButton.setEnabled(true);
                downloadButton.setEnabled(true);
                previewButton.setEnabled(true);
                break;
        }
    }

    void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }

    void setOriginalFileName(String originalFileName) {
        this.setCaption("File Name: " + originalFileName);
    }

    private StreamResource createResource() {
        return new StreamResource((StreamResource.StreamSource) () -> {

            try {
                return new FileInputStream(new File(scriptFilePath));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }, "jive_script.sql");
    }
}

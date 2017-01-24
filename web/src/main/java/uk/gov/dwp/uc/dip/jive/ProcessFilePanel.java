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
import java.util.List;
import java.util.UUID;


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

    ProcessFilePanel() {
        super();
        VerticalLayout verticalLayout = new VerticalLayout();
        statusLabel = new Label();

        HorizontalLayout buttonBar = new HorizontalLayout();
        Label emptyLabel = new Label();
        emptyLabel.setSizeFull();

        // Hive database to run sql against if run button pressed
        TextField runDatabaseTextField = new TextField("Run Database:");

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
        verticalLayout.addComponent(runDatabaseTextField);

        validateButton = new Button("1. Validate File");
        validateButton.addClickListener((Button.ClickListener) event -> {
            log.debug("Validate button pressed.");
            mappingValidator = new TechnicalMappingValidator();
            if(mappingValidator.isFileValid(tempFilePath)){
                setStatus(StatusEnum.FILE_VERIFIED);
                errorPopup.setPopupVisible(false);
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
            } catch (IOException e) {
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        runButton = new Button("4. Run SQL");
        runButton.addClickListener((Button.ClickListener) event -> {
            log.debug("Run button pressed.");
            File script = new File(scriptFilePath);
            try {
                List<String> allStatementLines = Files.readAllLines(script.toPath());
                StringBuilder allStatements = new StringBuilder();
                // Filter out !echo lines
                allStatementLines.stream().filter(line -> !StringUtils.trim(line).startsWith("!echo"))
                        .forEach(line -> allStatements.append(line).append("\n"));
                tabSheet.setSelectedTab(1);
                HiveProxyExecutor hpe = new HiveProxyExecutor();
                hiveResultsPanel.reset();

                hpe.executeSemiColonSeparatedStatements(allStatements.toString()
                        , runDatabaseTextField.getValue()
                        , hiveResultsPanel.getContainer());
            } catch (IOException e) {
                e.printStackTrace();
                NotificationUtils.displayError(e);
            }
        });

        downloadButton = new Button("3. Download SQL");
        StreamResource myResource = createResource();
        FileDownloader fileDownloader = new FileDownloader(myResource);
        fileDownloader.extend(downloadButton);

        buttonBar.addComponent(validateButton);
        buttonBar.addComponent(generateButton);
        buttonBar.addComponent(downloadButton);
        buttonBar.addComponent(runButton);

        verticalLayout.addComponent(statusLabel);
        verticalLayout.addComponent(new Label(""));
        verticalLayout.addComponent(buttonBar);

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
                setVisible(true);
                break;
            case FILE_VERIFIED:
                validateButton.setEnabled(true);
                generateButton.setEnabled(true);
                runButton.setEnabled(false);
                downloadButton.setEnabled(false);
                break;
            case FILE_INVALID:
                errorPopup.setPopupVisible(true);
                validateButton.setEnabled(true);
                generateButton.setEnabled(false);
                runButton.setEnabled(false);
                downloadButton.setEnabled(false);
                break;
            case FILE_GENERATED:
                validateButton.setEnabled(true);
                generateButton.setEnabled(true);
                runButton.setEnabled(true);
                downloadButton.setEnabled(true);
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

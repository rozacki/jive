package uk.gov.dwp.uc.dip.jive;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingValidator;
import uk.gov.dwp.uc.dip.schemagenerator.SchemaGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;
import static com.vaadin.ui.Notification.Type.TRAY_NOTIFICATION;


class ProcessFilePanel extends Panel{

    private String jsonSourcePath;

    void setJsonSourcePath(String jsonSourcePath) {
        this.jsonSourcePath = jsonSourcePath;
    }

    enum StatusEnum{
        FILE_NOT_SET("File not selected"),
        FILE_UPLOADED("File uploaded."),
        FILE_VERIFIED("File valid."),
        FILE_INVALID("File failed verification"),
        FILE_GENERATED("File Generated.");

        private String description;
        StatusEnum(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private String tempFilePath;
    private String originalFileName;
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

        // Error popup
        errorText = new Label();
        errorText.setSizeFull();
        errorPopup = new PopupView("Validation Errors;", errorText);
        errorPopup.setPopupVisible(false);
        verticalLayout.addComponent(errorPopup);

        validateButton = new Button("Validate File");
        validateButton.addClickListener((Button.ClickListener) event -> {
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

                errorText.setValue(error.toString());
                errorPopup.setPopupVisible(true);
            }
        });

        generateButton = new Button("Generate SQL");
        generateButton.addClickListener((Button.ClickListener) event -> {
            try {
                schemaGenerator = new SchemaGenerator(tempFilePath, jsonSourcePath);
                scriptFilePath = Properties.getInstance().getScriptsPath()
                        + UUID.randomUUID().toString() + ".sql";
                File script = new File(scriptFilePath);
                FileWriter writer = new FileWriter(script);
                writer.write(schemaGenerator.transformAll());
                writer.flush();
                writer.close();
                setStatus(StatusEnum.FILE_GENERATED);
            } catch (IOException e) {
                e.printStackTrace();
                Notification.show(e.getLocalizedMessage(),ERROR_MESSAGE);
            }
        });

        runButton = new Button("Run SQL");
        runButton.addClickListener((Button.ClickListener) event -> Notification.show("Not done yet.", ERROR_MESSAGE));

        downloadButton = new Button("Download SQL");
        StreamResource myResource = createResource();
        FileDownloader fileDownloader = new FileDownloader(myResource);
        fileDownloader.extend(downloadButton);


        buttonBar.addComponent(validateButton);
        buttonBar.addComponent(generateButton);
        buttonBar.addComponent(downloadButton);
        buttonBar.addComponent(runButton);

        verticalLayout.addComponent(statusLabel);
        verticalLayout.addComponent(buttonBar);
        this.setContent(verticalLayout);

    }

    void setStatus(StatusEnum status){
        statusLabel.setValue(status.getDescription());
        Notification.show(status.getDescription(), TRAY_NOTIFICATION);
        switch(status){
            case FILE_NOT_SET:
                this.setVisible(false);
                tempFilePath = "";
                originalFileName = "";
                break;
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
        this.originalFileName = originalFileName;
        this.setCaption("Process File: " + originalFileName);
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

package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class MainPanel extends Panel {

    private ProcessFilePanel processFilePanel;

    MainPanel() {
        final VerticalLayout layout = new VerticalLayout();

        TextField dataLocationTextField = new TextField("Data Location");
        dataLocationTextField.setWidth(20, Unit.EM);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dataLocation = Properties.getInstance().getDataLocation()
                + dateFormat.format(new Date());
        dataLocationTextField.setValue(dataLocation);

        MappingFileUploader mappingFileUploader = new MappingFileUploader("Select a file");
        layout.addComponent(dataLocationTextField);
        layout.addComponent(mappingFileUploader);

        processFilePanel = new ProcessFilePanel();
        processFilePanel.setVisible(false);
        layout.addComponent(processFilePanel);

        layout.setMargin(true);
        layout.setSpacing(true);
        this.setContent(layout);
        this.setCaption("Settings And File Upload");

        mappingFileUploader.addSucceededListener((Upload.SucceededListener) event -> {
            processFilePanel.setOriginalFileName(mappingFileUploader.getOriginalFileName());
            processFilePanel.setTempFilePath(mappingFileUploader.getFilePath());
            processFilePanel.setStatus(ProcessFilePanel.StatusEnum.FILE_UPLOADED);
            processFilePanel.setJsonSourcePath(dataLocationTextField.getValue());
        });

        mappingFileUploader.addFailedListener((Upload.FailedListener) event ->
                processFilePanel.setStatus(ProcessFilePanel.StatusEnum.FILE_NOT_SET));
    }
}

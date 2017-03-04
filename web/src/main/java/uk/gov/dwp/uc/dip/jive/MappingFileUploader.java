package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.Upload;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

class MappingFileUploader extends Upload {

    private final static Logger log = Logger.getLogger(MappingFileUploader.class);
    private String filePath;
    private String originalFileName;

    MappingFileUploader(String caption) {
        super();
        setCaption(caption);

        setReceiver((Upload.Receiver) (filename, mimeType) -> {
            try {
                originalFileName = filename;
                String extension = FilenameUtils.getExtension(originalFileName);
                filePath = Properties.getInstance().getUploadPath()
                        + UUID.randomUUID().toString() + "." + extension;
                File file = new File(filePath);
                log.info("Uploading:Original File Name:" + originalFileName);
                log.info("Saving to:" + filePath);
                return new FileOutputStream(file);
            } catch (IOException | IllegalArgumentException e) {
                log.error(e);
                NotificationUtils.displayError(e);
                return null;
            }
        });

        setImmediate(true);
        setButtonCaption("Upload");
    }

    String getFilePath() {
        return filePath;
    }

    String getOriginalFileName() {
        return originalFileName;
    }
}
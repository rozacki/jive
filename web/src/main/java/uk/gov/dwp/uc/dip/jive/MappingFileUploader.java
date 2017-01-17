package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.Upload;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

class MappingFileUploader extends Upload {

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

                return new FileOutputStream(file);
            } catch (IOException | IllegalArgumentException e) {
                return null;
            }
        });

        setImmediate(false);
        setButtonCaption("Upload");
    }

    String getFilePath() {
        return filePath;
    }

    String getOriginalFileName() {
        return originalFileName;
    }
}
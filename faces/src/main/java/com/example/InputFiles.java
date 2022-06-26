package com.example;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class InputFiles {
    private static final Logger LOGGER = Logger.getLogger(InputFiles.class.getSimpleName());

    @Inject
    FacesContext facesContext;

    private List<Part> files;

    private List<String> uploadedFiles = new ArrayList<>();

    public List<Part> getFiles() {
        return files;
    }

    public void setFiles(List<Part> files) {
        this.files = files;
    }

    public List<String> getUploadedFiles() {
        return uploadedFiles;
    }

    public void submit() {
        LOGGER.log(Level.INFO, "uploaded file size:{0}", files.size());
        for (Part part : files) {
            String submittedFilename = part.getSubmittedFileName();
            String name = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            long size = part.getSize();
            String contentType = part.getContentType();
            LOGGER.log(Level.INFO, "uploaded file: submitted filename: {0}, name:{1}, size:{2}, content type: {3}", new Object[]{
                    submittedFilename, name, size, contentType
            });

            part.getHeaderNames()
                    .forEach(headerName ->
                            LOGGER.log(Level.INFO, "header name: {0}, value: {1}", new Object[]{
                                    headerName, part.getHeader(headerName)
                            })
                    );

            uploadedFiles.add(submittedFilename);
            facesContext.addMessage(null, new FacesMessage(name + " was uploaded successfully!"));
        }
    }

}
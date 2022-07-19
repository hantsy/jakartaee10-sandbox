package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("multiparts")
@RequestScoped
public class MultipartResource {
    private static final Logger LOGGER = Logger.getLogger(MultipartResource.class.getName());

    java.nio.file.Path uploadedPath;

    @PostConstruct
    public void init() {
        try {
            uploadedPath = Files.createTempDirectory(Paths.get("/temp"), "uploads_");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Path("simple")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormParam("name") String name,
                               @FormParam("part") EntityPart part) {
        LOGGER.log(Level.INFO, "name: {0} ", name);
        LOGGER.log(
                Level.INFO,
                "uploading file: {0},{1},{2},{3}",
                new Object[]{
                        part.getMediaType(),
                        part.getName(),
                        part.getFileName(),
                        part.getHeaders()
                }
        );
        try {
            Files.copy(
                    part.getContent(),
                    Paths.get(uploadedPath.toString(), part.getFileName().orElse(generateFileName(UUID.randomUUID().toString(), mediaTypeToFileExtension(part.getMediaType())))),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.ok().build();

    }

    @Path("list")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMultiFiles(List<EntityPart> parts) {
        LOGGER.log(Level.INFO, "Uploading files: {0}", parts.size());
        parts.forEach(
                part -> {
                    LOGGER.log(
                            Level.INFO,
                            "{0},{1},{2},{3}",
                            new Object[]{
                                    part.getMediaType(),
                                    part.getName(),
                                    part.getFileName(),
                                    part.getHeaders()
                            }
                    );
                    try {
                        Files.copy(
                                part.getContent(),
                                Paths.get(uploadedPath.toString(), part.getFileName().orElse(generateFileName(UUID.randomUUID().toString(), mediaTypeToFileExtension(part.getMediaType())))),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return Response.ok().build();
    }

    @GET
    public List<EntityPart> getFiles() throws IOException {
        List<EntityPart> parts = new ArrayList<>();
        parts.add(EntityPart.withName("abd")
                .fileName("abc.text").content("this is a text content")
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build()
        );
        try (var files = Files.list(uploadedPath)) {
            var partsInUploaded = files
                    .map(path -> {
                                var file = path.toFile();
                                LOGGER.log(Level.INFO, "found uploaded file: {0}", file.getName());
                                try {
                                    return EntityPart.withName(file.getName())
                                            .fileName(file.getName())
                                            .content(new FileInputStream(file))
                                            .mediaType(fileExtensionToMediaType(getFileExt(file.getName())))
                                            .build();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                    )
                    .toList();
            parts.addAll(partsInUploaded);
        }

        return parts;
    }

    private String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private MediaType fileExtensionToMediaType(String extension) {
        return switch (extension.toLowerCase()) {
            case "txt" -> MediaType.TEXT_PLAIN_TYPE;
            case "svg" -> MediaType.APPLICATION_SVG_XML_TYPE;
            default -> MediaType.APPLICATION_OCTET_STREAM_TYPE;
        };
    }

    private String generateFileName(String fileName, String extension) {
        return fileName + "." + extension;
    }

    private String mediaTypeToFileExtension(MediaType mediaType) {
        return switch (mediaType.toString()) {
            case "text/plain" -> "txt";
            case "application/svg+xml" -> "svg";
            default -> "bin";
        };
    }
}

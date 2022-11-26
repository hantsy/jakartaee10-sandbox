/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package com.example.it;

import com.example.MultipartResource;
import com.example.RestConfig;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ArquillianExtension.class)
public class MultipartResourceTest {

    private final static Logger LOGGER = Logger.getLogger(MultipartResourceTest.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        File[] extraJars = Maven
                .resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity()
                .asFile();
        var war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(extraJars)
                .addClasses(MultipartResource.class, RestConfig.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.log(Level.INFO, "war deployment: {0}", new Object[]{war.toString(true)});
        return war;
    }

    @ArquillianResource
    private URL baseUrl;

    Client client;

    @BeforeEach
    public void before() throws Exception {
        LOGGER.log(Level.INFO, "baseURL: {0}", new Object[]{baseUrl.toExternalForm()});
        client = ClientBuilder.newClient();
        client.register(MultiPartFeature.class);
    }

    @AfterEach
    public void after() throws Exception {
        client.close();
    }

    @Test
    @RunAsClient
    public void testUploadSingleFile() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts/simple"));
        var part = EntityPart.withName("part").fileName("test.txt")
                .content(this.getClass().getResourceAsStream("/test.txt"))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
        var name = EntityPart.withName( "name").content("test").build();
        var genericEntity = new GenericEntity<List<EntityPart>>(List.of(name, part)) {};
        var entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA);
        Response r = target.request(MediaType.MULTIPART_FORM_DATA).post(entity);
        LOGGER.log(Level.INFO, "response status: {0}", r.getStatus());
        assertEquals(200, r.getStatus());
    }

    @Test
    @RunAsClient
    public void testUploadMultiFiles() throws Exception {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts/list"));
        List<EntityPart> parts = List.of(
                EntityPart.withName("textFile").fileName("test.txt")
                        .content(this.getClass().getResourceAsStream("/test.txt"))
                        .mediaType(MediaType.TEXT_PLAIN_TYPE)
                        .build(),
                EntityPart.withName("imageFile").fileName("test.svg")
                        .content(this.getClass().getResourceAsStream("/test.svg"))
                        .mediaType(MediaType.APPLICATION_SVG_XML_TYPE)
                        .build()
        );
        var genericEntity = new GenericEntity<List<EntityPart>>(parts) {};
        var entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA);
        Response r = target.request().post(entity);
        assertEquals(200, r.getStatus());
        LOGGER.log(Level.INFO, "Upload multiple files response status: {0}", r.getStatus());
    }

    @Test
    @RunAsClient
    public void testGetFiles() {
        var target = client.target(URI.create(baseUrl.toExternalForm() + "api/multiparts"));
        Response response = target.request().accept(MediaType.MULTIPART_FORM_DATA).get();

        assertEquals(200, response.getStatus());
        LOGGER.log(Level.INFO, "GetFiles response status: {0}", response.getStatus());
        List<EntityPart> parts = response.readEntity(new GenericType<List<EntityPart>>() {});
        parts.forEach(part -> LOGGER.log(
                Level.INFO,
                "Get file: {0},{1},{2},{3}",
                new Object[]{
                        part.getMediaType(),
                        part.getName(),
                        part.getFileName(),
                        part.getHeaders()
                }
        ));
    }
}

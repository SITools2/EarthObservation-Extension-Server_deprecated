/*******************************************************************************
 * Copyright 2011, 2012 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 * 
 * This file is part of SITools2.
 * 
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.ext.jeobrowser.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.api.DocAPI;
import fr.cnes.sitools.common.SitoolsXStreamRepresentation;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.common.model.Response;
import fr.cnes.sitools.ext.jeobrowser.common.AbstractJeoBrowserSitoolsServerTestCase;
import fr.cnes.sitools.ext.jeobrowser.upload.JeoUploadResourceModel;
import fr.cnes.sitools.util.RIAPUtils;

public class JeoUploadTestCase extends AbstractJeoBrowserSitoolsServerTestCase {

  private String appClassName = "fr.cnes.sitools.ext.jeobrowser.upload.JeoBrowserUploadApplication";

  private String appUrlAttach = "/jeodataset/jeo/upload";

  private String uploadFolder = settings.getRootDirectory() + settings.getStoreDIR() + "/jeoupload";

  private String fileToUpload = "tmp_zip.zip";

  private String jsonFileToUpload = "flux_json_complet.json";

  @Before
  public void setUp() throws Exception {
    setMediaTest(MediaType.APPLICATION_JSON);
  }

  @Test
  public void uploadTestZip() {

    BioUtils.delete(new File(uploadFolder, fileToUpload));

    File toUpload = new File(settings.getRootDirectory() + super.getTestRepository() + "_mapshup", fileToUpload);
    if (!toUpload.exists()) {
      fail("FILE MUST EXIST FOR TEST :" + toUpload.getName());
    }
    Reference ref = new Reference(getHostUrl() + appUrlAttach + "?" + JeoUploadResourceModel.PARAM_DATASTORAGE_URL
        + "=" + "/jeo/medias");

    ClientResource res = new ClientResource(ref);
    FileRepresentation rep = new FileRepresentation(toUpload, MediaType.APPLICATION_ZIP);

    Representation result = res.post(rep);

    assertTrue(res.getStatus().isSuccess());
    assertEquals(Status.SUCCESS_CREATED.getCode(), res.getStatus().getCode());
    assertNotNull(result);

    Response response = getResponse(getMediaTest(), result, String.class);
    assertNotNull(response);
    assertTrue(response.getSuccess());

  }

  @Test
  public void uploadTestJSON() throws IOException {

    File toUpload = new File(settings.getRootDirectory() + super.getTestRepository() + "_mapshup", jsonFileToUpload);
    if (!toUpload.exists()) {
      fail("FILE MUST EXIST FOR TEST :" + toUpload.getName());
    }

    FileRepresentation fileRepr = new FileRepresentation(toUpload, MediaType.APPLICATION_JSON);

    JsonRepresentation jsonRepr;

    jsonRepr = new JsonRepresentation(fileRepr);
    ClientResource res = new ClientResource(getHostUrl() + appUrlAttach);

    Representation result = res.post(jsonRepr, MediaType.APPLICATION_JSON);

    assertTrue(res.getStatus().isSuccess());
    assertEquals(Status.SUCCESS_CREATED.getCode(), res.getStatus().getCode());
    assertNotNull(result);

    Response response = getResponse(getMediaTest(), result, String.class);
    assertNotNull(response);
    assertTrue(response.getSuccess());
    RIAPUtils.exhaust(result);

  }

  /**
   * REST API Response wrapper for single item expected.
   * 
   * @param media
   *          MediaType expected
   * @param representation
   *          service response representation
   * @param dataClass
   *          class expected in the item property of the Response object
   * @return Response the response.
   */
  public static Response getResponse(MediaType media, Representation representation, Class<?> dataClass) {
    return getResponse(media, representation, dataClass, false);
  }

  /**
   * REST API Response Representation wrapper for single or multiple items
   * expexted
   * 
   * @param media
   *          MediaType expected
   * @param representation
   *          service response representation
   * @param dataClass
   *          class expected for items of the Response object
   * @param isArray
   *          if true wrap the data property else wrap the item property
   * @return Response
   */
  public static Response getResponse(MediaType media, Representation representation, Class<?> dataClass, boolean isArray) {
    try {
      if (!media.isCompatible(MediaType.APPLICATION_JSON) && !media.isCompatible(MediaType.APPLICATION_XML)) {
        Logger.getLogger(AbstractSitoolsTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null;
      }

      XStream xstream = XStreamFactory.getInstance().getXStreamReader(media);
      xstream.autodetectAnnotations(false);
      xstream.alias("response", Response.class);

      if (isArray && media.isCompatible(MediaType.APPLICATION_JSON)) {
        xstream.addImplicitCollection(Response.class, "data", dataClass);
      }
      xstream.alias("item", dataClass);
      xstream.alias("item", Object.class, dataClass);

      xstream.aliasField("data", Response.class, "data");

      SitoolsXStreamRepresentation<Response> rep = new SitoolsXStreamRepresentation<Response>(representation);
      rep.setXstream(xstream);

      if (media.isCompatible(getMediaTest())) {
        Response response = rep.getObject("response");

        return response;
      }
      else {
        Logger.getLogger(AbstractSitoolsTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null; // TODO complete test for XML, Object
      }
    }
    finally {
      RIAPUtils.exhaust(representation);
    }
  }

  /**
   * Configure XStream mapping of a Response object
   * 
   * @param xstream
   *          XStream
   */
  private static void configure(XStream xstream) {
    xstream.autodetectAnnotations(false);
  }

}

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsXStreamRepresentation;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.ext.jeobrowser.common.AbstractJeoBrowserSitoolsServerTestCase;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.EnumSon;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.ErrorDescription;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.Filter;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.JeoDescribe;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.util.FileUtils;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * Test the dataset describe service
 * 
 * 
 * @author m.gond
 */
public class JeoDatasetDescribeTestCase extends AbstractJeoBrowserSitoolsServerTestCase {

  /** The url of the opensearch service */
  private String refreshUrl = "/jeo/opensearch/refresh";

  /** The datasetId */
  private String datasetId = "07cd2a7a-a992-4671-b4b8-91b7f5edb2fc";

  /** The url of the dataset */
  private String datasetUrl = "/jeodataset";

  /** The url of the dataset */
  private String datasetUrlEmptyMapping = "/jeo_medias";

  /** Expected number of filters to find */
  private int expectedFilterNumber = 5;

  @Before
  @Override
  /**
   * Create component, store and application and start server
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {
    super.setUp();
    String directory = settings.getStoreDIR(Consts.APP_STORE_ENUM_FILTER);
    File file = new File(directory);
    if (file.exists()) {
      boolean cleaned = FileUtils.cleanDirectory(file);
      System.out.println("CLEANED : " + cleaned);
    }

  }

  /**
   * The complete url of the opensearch service
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl();
  }

  /**
   * Test with XML media type
   * 
   */
  @Test
  public void refreshValuesXML() {
    setMediaTest(MediaType.APPLICATION_XML);
    getDescribeDatasetError();
    refreshValues();
    getDescribeDataset();
  }

  /**
   * Test with JSON media type
   * 
   */
  @Test
  public void refreshValuesJSON() {
    setMediaTest(MediaType.APPLICATION_JSON);
    getDescribeDatasetError();
    refreshValues();
    getDescribeDataset();
    // test with dataset with empty mapping
    refreshValuesEmptyMapping();
  }

  /**
   * Generate the Dataset description on the server Invoke PUT
   * 
   */
  private void refreshValues() {
    String url = getHostUrl() + datasetUrl + refreshUrl;
    ClientResource cr = new ClientResource(url);
    String user = "admin";
    String password = "admin";
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user, password);
    Representation result = cr.put(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    MediaType type = getMediaTest();
    setMediaTest(MediaType.APPLICATION_JSON);
    JeoDescribe jeoDescribe = getJeoDescribe(getMediaTest(), result);
    setMediaTest(type);
    assertNotNull(jeoDescribe);
    assertNotNull(jeoDescribe.getFilters());
    assertEquals(expectedFilterNumber, jeoDescribe.getFilters().size());
    RIAPUtils.exhaust(result);

    // check that a file was created
    String directory = settings.getStoreDIR(Consts.APP_STORE_ENUM_FILTER);
    File file = new File(directory + "/" + datasetId);
    assertTrue(file.exists());

  }

  /**
   * Generate the Dataset description on the server for a dataset with an empty
   * mapping Invoke PUT
   * 
   * 
   */
  private void refreshValuesEmptyMapping() {
    String url = getHostUrl() + datasetUrlEmptyMapping + refreshUrl;
    ClientResource cr = new ClientResource(url);
    String user = "admin";
    String password = "admin";
    cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, user, password);
    Representation result = cr.put(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    MediaType type = getMediaTest();
    setMediaTest(MediaType.APPLICATION_JSON);
    JeoDescribe jeoDescribe = getJeoDescribe(getMediaTest(), result);
    setMediaTest(type);
    assertNotNull(jeoDescribe);
    assertNull(jeoDescribe.getFilters());
    RIAPUtils.exhaust(result);
  }

  /**
   * Get the description from the server INVOKE GET
   * 
   */
  private void getDescribeDataset() {
    String url = getHostUrl() + datasetUrl + refreshUrl;
    ClientResource cr = new ClientResource(url);
    Representation result = cr.get(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    MediaType type = getMediaTest();
    setMediaTest(MediaType.APPLICATION_JSON);
    JeoDescribe jeoDescribe = getJeoDescribe(getMediaTest(), result);
    setMediaTest(type);
    assertNotNull(jeoDescribe);
    assertNotNull(jeoDescribe.getFilters());
    assertEquals(expectedFilterNumber, jeoDescribe.getFilters().size());
    RIAPUtils.exhaust(result);

  }

  /**
   * Get the description from the server an expect an error INVOKE GET
   * 
   */
  private void getDescribeDatasetError() {
    String url = getHostUrl() + datasetUrl + refreshUrl;
    ClientResource cr = new ClientResource(url);
    Representation result = cr.get(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    JeoDescribe jeoDescribe = getJeoDescribe(getMediaTest(), result);
    assertNotNull(jeoDescribe);
    RIAPUtils.exhaust(result);
  }

  // ------------------------------------------------------------
  // RESPONSE REPRESENTATION WRAPPING

  /**
   * REST API Response Representation wrapper for single or multiple items
   * expexted
   * 
   * @param media
   *          MediaType expected
   * @param representation
   *          service response representation
   * @return Response
   */
  public static JeoDescribe getJeoDescribe(MediaType media, Representation representation) {
    try {
      if (!media.isCompatible(getMediaTest()) && !media.isCompatible(MediaType.APPLICATION_XML)) {
        Logger.getLogger(AbstractSitoolsTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null;
      }

      XStream xstream = XStreamFactory.getInstance().getXStreamReader(media);
      xstream.autodetectAnnotations(false);
      xstream.alias("jeoDescribe", JeoDescribe.class);
      xstream.alias("error", ErrorDescription.class);

      xstream.addImplicitCollection(JeoDescribe.class, "filters", Filter.class);
      xstream.addImplicitCollection(Filter.class, "son", EnumSon.class);

      SitoolsXStreamRepresentation<JeoDescribe> rep = new SitoolsXStreamRepresentation<JeoDescribe>(representation);
      rep.setXstream(xstream);

      if (media.isCompatible(getMediaTest())) {
        JeoDescribe describe = rep.getObject("jeoDescribe");
        return describe;
      }
      else {
        Logger.getLogger(AbstractSitoolsTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null; // TODO complete test with ObjectRepresentation
      }
    }
    finally {
      RIAPUtils.exhaust(representation);
    }
  }

}

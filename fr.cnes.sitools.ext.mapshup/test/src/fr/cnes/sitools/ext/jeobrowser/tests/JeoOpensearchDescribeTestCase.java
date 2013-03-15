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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.api.DocAPI;
import fr.cnes.sitools.ext.jeobrowser.common.AbstractJeoBrowserSitoolsServerTestCase;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * The test is testing the Opensearch service This service returns the XML
 * description of the Opensearch service on a specified dataset
 * 
 * 
 * @author m.gond
 */
public class JeoOpensearchDescribeTestCase extends AbstractJeoBrowserSitoolsServerTestCase {

  /** The url of the opensearch service */
  private String opensearchUrl = "/jeo/opensearch.xml";

  /** The url of the dataset */
  private String datasetUrl = "/jeodataset";

  /** The expectedTemplate */
  private String expectedTemplate = "http://localhost:8182/jeodataset/sva/1801deae-b618-46a2-aaf5-9101f9062ee5/tasks?q={searchTerms}&pw={startPage?}&count={count?}&start={start?}&bbox={geo:box?}&format=json&startDate={time:start?}&completionDate={time:stop?}&lat={geo:lat?}&lon={geo:lon?}&r={geo:radius?}";

  /** The expectedTemplate specific part */
  private String expectedTemplateSpecific = "&amp;date={sitools:date?}&amp;ele={sitools:ele?}&amp;notes={sitools:notes?}&amp;building_state={sitools:building_state?}&amp;building_identifier={sitools:building_identifier?}";

  /**
   * The complete url of the opensearch service
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl();
  }

  static {
    docAPI = new DocAPI(JeoOpensearchDescribeTestCase.class, "JeoOpensearchDescribeTestCase");
    docAPI.setActive(false);
    docAPI.setMediaTest(MediaType.APPLICATION_XML);
  }

  @Before
  public void setUp() {
    docAPI.setActive(false);
    setMediaTest(MediaType.APPLICATION_XML);
  }

  @Test
  public void testWithAPI() throws IOException {
    docAPI.setActive(true);
    docAPI.appendSubChapter("Get opensearch description simple", "description");
    retrieveOpensearchSearchDescription();
    docAPI.appendSubChapter("Get opensearch description with specific value", "description_specs");
    retrieveOpensearchSearchDescriptionWithSpecifics();
  }

  @Test
  public void retrieveOpensearchSearchDescription() throws IOException {

    String url = getHostUrl() + datasetUrl + opensearchUrl;
    if (docAPI.isActive()) {
      Map<String, String> parameters = new LinkedHashMap<String, String>();
      retrieveDocAPI(url, "", parameters, url);
    }
    else {

      ClientResource cr = new ClientResource(url);
      Representation result = cr.get(MediaType.APPLICATION_XML);
      assertNotNull(result);
      assertTrue(cr.getStatus().isSuccess());

      String opensearchResponse = result.getText();

      if (!opensearchResponse.contains(expectedTemplate)) {
        fail("Expected template : " + expectedTemplate + " not found in : " + opensearchResponse);
      }

      RIAPUtils.exhaust(result);
    }
  }

  @Test
  public void retrieveOpensearchSearchDescriptionWithSpecifics() throws IOException {

    String url = getHostUrl() + datasetUrl + opensearchUrl;
    if (docAPI.isActive()) {
      Map<String, String> parameters = new LinkedHashMap<String, String>();
      retrieveDocAPI(url, "", parameters, url);
    }
    else {
      ClientResource cr = new ClientResource(url);
      Representation result = cr.get(MediaType.APPLICATION_XML);
      assertNotNull(result);
      assertTrue(cr.getStatus().isSuccess());

      String opensearchResponse = result.getText();

      if (!opensearchResponse.contains(expectedTemplate + expectedTemplateSpecific)) {
        fail("Expected template : " + expectedTemplate + expectedTemplateSpecific + " not found in : "
            + opensearchResponse);
      }

      RIAPUtils.exhaust(result);
    }
  }

}

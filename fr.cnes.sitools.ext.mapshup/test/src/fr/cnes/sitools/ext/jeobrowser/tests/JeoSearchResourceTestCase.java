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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.api.DocAPI;
import fr.cnes.sitools.ext.jeobrowser.common.AbstractJeoBrowserSitoolsServerTestCase;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * Tests the search service
 * 
 * 
 * @author m.gond
 */
public class JeoSearchResourceTestCase extends AbstractJeoBrowserSitoolsServerTestCase {
  /** The url of the dataset */
  private String datasetUrl = "/jeodataset";

  /** The url of the search resource */
  private String searchResourceUrl = "/search";

  static {
    docAPI = new DocAPI(JeoSearchResourceTestCase.class, "JeoBrowserSearchTestCase");
    docAPI.setActive(false);
    docAPI.setMediaTest(MediaType.APPLICATION_JSON);
  }

  /**
   * SetUp function
   * 
   * @throws Exception
   *           if there is an error
   */
  @Before
  public void setUp() throws Exception {
    setMediaTest(MediaType.APPLICATION_JSON);
    docAPI.setActive(false);
  }

  /**
   * Test avec DocAPI
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  @Test
  public void testWithAPI() throws IOException, JSONException {
    docAPI.setActive(true);
    docAPI.appendChapter("Test with standard parameters");
    docAPI.appendSubChapter("Empty result", "empty");
    testEmptyResult();
    docAPI.appendSubChapter("Test with error", "error");
    testErrorResult();
    docAPI.appendSubChapter("Test with param standard", "standard");
    testWithParamStandard();
    docAPI.appendSubChapter("Test with box param", "box");
    testWithBoxParam();
    docAPI.appendSubChapter("Test with lat long radius param", "lat_long_radius");
    testWithLatLongRadiusParam();
    docAPI.appendSubChapter("Test with date", "date");
    testWithDateParam();
    docAPI.appendSubChapter("Test with searchTerms param", "searchTerms");
    testWithSearchTermsParam();
    docAPI.appendSubChapter("Test with searchTerms and fuzy search param", "searchTermsFuzy");
    testWithSearchTermsParamFuzySearch();
    docAPI.appendChapter("Test with specific parameters");
    docAPI.appendSubChapter("Test with specific param date", "spec_date");
    testWithSpecificParamDate();
    docAPI.appendSubChapter("Test with specific param date interval", "spec_date_interval");
    testWithSpecificParamDateInterval();
    docAPI.appendSubChapter("Test with specific param enumeration", "spec_enum");
    testWithSpecificParamEnumeration();
    docAPI.appendSubChapter("Test with specific param date", "spec_number");
    testWithSpecificParamNumber();
    docAPI.appendSubChapter("Test with specific param text", "spec_text");
    testWithSpecificParamText();
    docAPI.close();
  }

  /**
   * Test avec des paramètres standard et un resultat vide (count, startIndex, startPage) US : JEO Service opensearch,
   * id : 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  @Test
  public void testEmptyResult() throws IOException, JSONException {
    String url = getServiceUrl() + "?count=0&pw=0&start=0";
    sendRequestToSearchService(url, 0);
  }

  /**
   * Test avec des paramètres standard et une erreur (count, startIndex, startPage) US : JEO Service opensearch, id :
   * 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  // @Test
  public void testErrorResult() throws IOException, JSONException {

    String url = getServiceUrl()
        + "?colModel=identifier, date, notes, building_identifier, building_peoplenb, building_state, ele, testtest&count=1&pw=0&start=0";

    sendRequestToSearchService(url, 0);

  }

  /**
   * Test avec des paramètres standard (count, startIndex, startPage) US : JEO Service opensearch, id : 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  @Test
  public void testWithParamStandard() throws IOException, JSONException {
    String url = getServiceUrl() + "?count=1&pw=0&start=0";
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres lat long radius US : JEO Service opensearch avec paramètres geo:lat geo:lon et geo:radius
   * , id : 3170
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithBoxParam() throws IOException, JSONException {
    // with those params we get only one record
    String boxCoord = "0.0,40.0,4.0,44.0";
    String url = getServiceUrl() + "?bbox=" + boxCoord;
    sendRequestToSearchService(url, 2);

  }

  /**
   * Test avec des paramètres Lat long radius US : JEO Service opensearch avec paramètre geo:box , id : 3169
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithLatLongRadiusParam() throws IOException, JSONException {
    // distance is 0.8 in order to find only one record
    String distance = "0.8";
    String lat = "1";
    String longitude = "41";
    String url = getServiceUrl() + "?lat=" + lat + "&lon=" + longitude + "&r=" + distance;
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres date US : JJEO Service opensearch avec paramètres time:start et time:end : 3171
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithDateParam() throws IOException, JSONException {
    String dtstart = "2011-02-01T00:00:00";
    String dtend = "2011-03-01T00:00:00";
    String url = getServiceUrl() + "?startDate=" + dtstart + "&completionDate=" + dtend;
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres search terms US : JEO Service opensearch avec paramètre searchTerms : 3187
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSearchTermsParam() throws IOException, JSONException {
    String searchTerms = "465456";
    String url = getServiceUrl() + "?q=" + searchTerms;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres search terms US : JEO Service opensearch avec paramètre searchTerms : 3187
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSearchTermsParamFuzySearch() throws IOException, JSONException {
    String searchTerms = "465*";
    String url = getServiceUrl() + "?q=" + searchTerms;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres specifiques de type date US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamDate() throws IOException, JSONException {
    String dateParam = "2011-02-23T00:00:00";
    String url = getServiceUrl() + "?date=" + dateParam;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres specifiques de type date US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamDateInterval() throws IOException, JSONException {
    String dateParam = "2011-02-01T00:00:00/2011-03-01T00:00:00";
    String url = getServiceUrl() + "?date=" + dateParam;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres specifiques de type Enumeration US : JEO Service opensearch avec paramètres spécifiques,
   * 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamEnumeration() throws IOException, JSONException {
    String enumParam = "bon|Moyen";
    String url = getServiceUrl() + "?building_state=" + enumParam;
    sendRequestToSearchService(url, 2);
  }

  /**
   * Test avec des paramètres specifiques de type Text US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamText() throws IOException, JSONException {
    String textParam = "texte de la note 465456";
    String url = getServiceUrl() + "?notes=" + textParam;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres specifiques de type Number US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamNumber() throws IOException, JSONException {
    String numberParam = "172.5";
    String url = getServiceUrl() + "?ele=" + numberParam;
    sendRequestToSearchService(url, 1);
  }

  public String getServiceUrl() {
    return getHostUrl() + getDatasetUrl() + searchResourceUrl;
  }

  /**
   * Send a request to the specified URL and assert that the number is records in the expectedRecrod
   * 
   * @param url
   *          the url
   * @param expectedRecord
   *          the number of record expected
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  private void sendRequestToSearchService(String url, int expectedRecord) throws IOException, JSONException {
    if (docAPI.isActive()) {
      Map<String, String> parameters = new LinkedHashMap<String, String>();
      retrieveDocAPI(url, "", parameters, url);
    }
    else {
      ClientResource cr = new ClientResource(url);
      Representation result = cr.get(getMediaTest());
      assertNotNull(result);
      assertTrue(cr.getStatus().isSuccess());

      try {
        JsonRepresentation jsonRepr = new JsonRepresentation(result);
        JSONObject json = jsonRepr.getJsonObject();
        assertEquals("FeatureCollection", json.get("type"));
        assertNotNull(json.getJSONArray("features"));
        assertEquals(expectedRecord, json.getJSONArray("features").length());
      }
      finally {
        RIAPUtils.exhaust(result);
      }
    }
  }

  /**
   * Gets the datasetUrl value
   * 
   * @return the datasetUrl
   */
  public String getDatasetUrl() {
    return datasetUrl;
  }

  /**
   * Sets the value of datasetUrl
   * 
   * @param datasetUrl
   *          the datasetUrl to set
   */
  public void setDatasetUrl(String datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

}

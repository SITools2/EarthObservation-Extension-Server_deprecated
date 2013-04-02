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
package fr.cnes.sitools.ext.jeobrowser.describe.dataset.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.common.exception.SitoolsException;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.database.DatabaseRequest;
import fr.cnes.sitools.dataset.database.DatabaseRequestParameters;
import fr.cnes.sitools.dataset.dto.ColumnConceptMappingDTO;
import fr.cnes.sitools.dataset.dto.DictionaryMappingDTO;
import fr.cnes.sitools.dataset.jdbc.DefaultDatabaseRequest;
import fr.cnes.sitools.dataset.jdbc.RequestFactory;
import fr.cnes.sitools.dataset.jdbc.RequestSql;
import fr.cnes.sitools.dataset.model.Column;
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.datasource.jdbc.model.AttributeValue;
import fr.cnes.sitools.datasource.jdbc.model.Record;
import fr.cnes.sitools.dictionary.model.Concept;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.EnumSon;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.ErrorDescription;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.Filter;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.FilterType;
import fr.cnes.sitools.ext.jeobrowser.describe.dataset.model.JeoDescribe;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.util.Property;

/**
 * Implements the Dataset description service
 * 
 * 
 * @author m.gond
 */
public class JeoDatasetDescribeResource extends SitoolsParameterizedResource {
  /** Concept name for enumeration not unique */
  private static final String CONCEPT_ENUM_NOT_UNIQUE_NAME = "specificEnumerationFieldNotUnique";
  /** Concept name for enumeration unique */
  private static final String CONCEPT_ENUM_UNIQUE_NAME = "specificEnumerationFieldUnique";
  /** Concept name for specific date field */
  private static final String CONCEPT_DATE_NAME = "specificDateField";
  /** Concept name for specific number field */
  private static final String CONCEPT_NUMBER_NAME = "specificNumberField";
  /** Concept name for specific text field */
  private static final String CONCEPT_TEXT_NAME = "specificTextField";
  /** The name of the size property in the concepts */
  private static final String PROPERTY_SIZE_NAME = "size";
  /** The name of the size property in the concepts */
  private static final String PROPERTY_VALUE_NAME = "value";
  /** The name of the size property in the concepts */
  private static final String PROPERTY_OPERATOR_NAME = "operator";

  /** The resourceModel */
  private ResourceModel model;
  /** The dataset */
  private DataSet ds;
  /** The DataSetApplication */
  private DataSetApplication application;
  /** The settings */
  private SitoolsSettings settings;

  @Override
  public void doInit() {
    super.doInit();

    application = (DataSetApplication) getApplication();
    if (application == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "can not find DataSet Application");
    }
    settings = application.getSettings();
    File dir = new File(settings.getStoreDIR(Consts.APP_STORE_ENUM_FILTER));
    dir.mkdirs();

    model = this.getModel();
    ds = application.getDataSet();

  }

  @Override
  public void sitoolsDescribe() {
    setName("JeoRefreshEnumerationResource");
    setDescription("Resource plugin to refresh enumerations values");
    setNegotiated(false);
  }

  /**
   * Generate the dataset description file
   * 
   * @param entity
   *          the entry entity, ignored
   * @param variant
   *          the variant needed
   * @return the JSON file produced or an error response
   * 
   */
  @Put
  @Override
  public Representation put(Representation entity, Variant variant) {
    Representation result;
    try {
      createDatasetDescribe();
      result = getDatasetDescribe();
    }
    catch (SitoolsException e) {
      result = handleException(e, variant);
    }
    catch (SQLException e) {
      result = handleException(e, variant);
    }
    catch (IOException e) {
      result = handleException(e, variant);
    }
    return result;
  }

  @Override
  protected void describePut(MethodInfo info) {
    this.addInfo(info);
  }

  @Get
  @Override
  public Representation get(Variant variant) {
    Representation result = null;
    try {
      result = getDatasetDescribe();
    }
    catch (SitoolsException e) {
      result = handleException(e, variant);
    }

    return result;
  }

  @Override
  protected void describeGet(MethodInfo info) {
    this.addInfo(info);
  }

  /**
   * Handle the Exception and create a representation from the message of the
   * exception
   * 
   * @param e
   *          the Exception
   * @param variant
   *          the variant needed for the representation
   * @return a representation
   */
  private Representation handleException(Exception e, Variant variant) {
    JeoDescribe jeoDescribe = new JeoDescribe();
    ErrorDescription error = new ErrorDescription();
    error.setMessage(e.getLocalizedMessage());
    jeoDescribe.setError(error);
    return getRepresentation(jeoDescribe, variant);
  }

  /**
   * Get the Dataset description Representation stored, if there is no
   * description stored it throws a SitoolsException
   * 
   * @return a Representation
   * @throws SitoolsException
   *           if the description canno't be found
   */
  private Representation getDatasetDescribe() throws SitoolsException {
    File file = new File(settings.getStoreDIR(Consts.APP_STORE_ENUM_FILTER) + "/" + ds.getId());
    if (!file.exists()) {
      throw new SitoolsException("Description not found, please generate it before");
    }
    FileRepresentation repr = new FileRepresentation(file, MediaType.APPLICATION_JSON);
    repr.setCharacterSet(CharacterSet.UTF_8);
    return repr;
  }

  /**
   * Create the dataset description file
   * 
   * @throws SitoolsException
   *           If the SQL query canno't be created
   * @throws SQLException
   *           if there are some errors in the SQL queries
   * @throws IOException
   *           if there is an error while creating the file
   */
  private void createDatasetDescribe() throws SitoolsException, SQLException, IOException {
    String dicoName = model.getParameterByName("dictionary_name").getValue();
    List<Filter> filtersList = new ArrayList<Filter>();
    if (dicoName != null) {
      DictionaryMappingDTO dico = application.getColumnConceptMappingDTO(dicoName);
      if (dico != null) {
        // get the total number of records in the dataset
        DatabaseRequestParameters databaseParams = new DatabaseRequestParameters(application.getDataSource(), 0, false,
            true, ds, ds.getPredicat(), ds.getStructures(), null, null, ds.getColumnModel(), 0, null, null, null, ds.getColumnModel());

        DatabaseRequest databaseRequest = new DefaultDatabaseRequest(databaseParams);
        int totalCount = -1;

        // the totalCount is made during this method
        databaseRequest.getRequestAsString();
        // get the totalCount
        totalCount = databaseRequest.getTotalCount();

        for (Iterator<ColumnConceptMappingDTO> iterator = dico.getMapping().iterator(); iterator.hasNext();) {
          ColumnConceptMappingDTO mapping = iterator.next();
          Concept concept = mapping.getConcept();
          String colAlias = mapping.getColumnAlias();
          FilterType type;
          boolean unique = false;
          if (mapping.getConcept().getName().equals(CONCEPT_ENUM_NOT_UNIQUE_NAME)) {
            type = FilterType.enumeration;
            unique = false;
          }
          else if (mapping.getConcept().getName().equals(CONCEPT_ENUM_UNIQUE_NAME)) {
            type = FilterType.enumeration;
            unique = true;
          }
          else if (mapping.getConcept().getName().equals(CONCEPT_DATE_NAME)) {
            type = FilterType.date;
          }
          else if (mapping.getConcept().getName().equals(CONCEPT_NUMBER_NAME)) {
            type = FilterType.number;
          }
          else if (mapping.getConcept().getName().equals(CONCEPT_TEXT_NAME)) {
            type = FilterType.text;
          }
          else {
            type = null;
          }

          if (type != null) {
            filtersList.add(createFilter(colAlias, type, concept, unique, totalCount));
          }

        }

      }
    }
    JeoDescribe jeoDescribe = new JeoDescribe();
    jeoDescribe.setFilters(filtersList);
    Representation representation = getRepresentation(jeoDescribe, MediaType.APPLICATION_JSON);
    File file = new File(settings.getStoreDIR(Consts.APP_STORE_ENUM_FILTER) + "/" + ds.getId());

    if (!file.exists()) {
      file.createNewFile();
    }
    InputStream inputStr = null;
    FileOutputStream st = null;
    try {
      st = new FileOutputStream(file);
      inputStr = representation.getStream();
      int toPrint;
      do {
        toPrint = inputStr.read();
        if (toPrint != -1) {
          st.write(toPrint);
        }
      } while (toPrint != -1);
    }
    finally {
      if (inputStr != null) {
        inputStr.close();

      }
      if (st != null) {
        st.close();
      }
    }
  }

  /**
   * Fill in the values of a Record from the specified ResultSet
   * 
   * @param record
   *          the <code>Record</code>
   * @param rs
   *          the ResultSet
   * @throws SQLException
   *           if a database access error occurs or this method is called on a
   *           closed result set
   */
  private void setAttributeValues(Record record, ResultSet rs) throws SQLException {
    ResultSetMetaData resultMeta = rs.getMetaData();
    int columnCount = resultMeta.getColumnCount();
    int columnType;
    for (int i = 1; i <= columnCount; i++) {
      Object obj;
      // pour éviter bug double précision avec Postgresql
      columnType = resultMeta.getColumnType(i);
      if (java.sql.Types.DOUBLE == columnType || java.sql.Types.FLOAT == columnType) {
        obj = rs.getDouble(resultMeta.getColumnLabel(i));
      }
      else {
        obj = rs.getString(resultMeta.getColumnLabel(i));
      }
      if (obj != null) {
        record.getAttributeValues().add(new AttributeValue(resultMeta.getColumnLabel(i), obj.toString()));
      }
      else {
        record.getAttributeValues().add(new AttributeValue(resultMeta.getColumnLabel(i), null));
      }
    }
  }

  /**
   * Create a filter object for a column specified by the given colAlias
   * 
   * @param colAlias
   *          the column alias of a column
   * @param type
   *          The FilterType
   * @param concept
   *          the Concept
   * @param unique
   *          if the enumeration is unique or not
   * @param totalCount
   *          the number of records in the dataset
   * 
   * @return a filter object
   * @throws SQLException
   *           if there is an exception in the SQL request
   */
  private Filter createFilter(String colAlias, FilterType type, Concept concept, boolean unique, int totalCount)
    throws SQLException {
    Filter filter = new Filter();
    filter.setId(colAlias);
    filter.setTitle(colAlias);
    filter.setType(type);
    Property propValue = concept.getPropertyFromName(PROPERTY_VALUE_NAME);
    if (propValue != null && !"".equals(propValue.getValue())) {
      filter.setValue(propValue.getValue());
    }

    switch (type) {
      case enumeration:
        Column col = ds.findByColumnAlias(colAlias);
        List<Column> cols = new ArrayList<Column>();
        cols.add(col);
  
        RequestSql request = RequestFactory.getRequest(application.getDataSource().getDsModel().getDriverClass());
        String strReq = "SELECT " + request.getAttributes(cols) + ", count(*) as population";
        strReq += " FROM " + request.getFromClauseAdvanced(ds.getStructure());
        strReq += " WHERE 1=1 " + request.getWhereClause(ds.getPredicat(), ds.getColumnModel());
        strReq += " GROUP BY " + request.convertColumnToString(col);
  
        getLogger().log(Level.INFO, "REQUEST SQL FOR JEOBROWSER ENUM REFRESH : " + strReq);
  
        ResultSet rs = application.getDataSource().basicQuery(strReq, 0, 0);
  
        filter.setUnique(unique);
        filter.setPopulation(totalCount);
  
        List<EnumSon> son = new ArrayList<EnumSon>();
  
        while (rs.next()) {
          Record rec = new Record();
          setAttributeValues(rec, rs);
          EnumSon ason = new EnumSon();
          List<AttributeValue> attr = rec.getAttributeValues();
          for (Iterator<AttributeValue> it = attr.iterator(); it.hasNext();) {
            AttributeValue attributeValue = it.next();
            String value = (String) attributeValue.getValue();
            if (attributeValue.getName().equals(colAlias) && value != null) {
              ason.setId(value);
              ason.setTitle(value);
              ason.setValue(value);
            }
            else if (attributeValue.getName().equals("population") && value != null) {
              ason.setPopulation(Integer.parseInt(value));
            }
  
          }
          son.add(ason);
        }
        filter.setSon(son);
        break;
  
      case date:
        break;
      case text:
        Property propSize = concept.getPropertyFromName(PROPERTY_SIZE_NAME);
        if (propSize != null) {
          filter.setSize(Integer.parseInt(propSize.getValue()));
        }
        break;
      case number:
        Property propOperator = concept.getPropertyFromName(PROPERTY_OPERATOR_NAME);
        if (propOperator != null) {
          filter.setOperator(propOperator.getValue());
        }
  
        break;
      default:
        break;

    }

    return filter;

  }

  /**
   * Encode a JeoDescribe into a Representation according to the given media
   * type.
   * 
   * @param describe
   *          the JeoDescribe object to serialize
   * @param media
   *          the media
   * @return Representation
   */
  public Representation getRepresentation(JeoDescribe describe, MediaType media) {

    XStream xstream = XStreamFactory.getInstance().getXStream(media);
    xstream.autodetectAnnotations(false);
    xstream.alias("jeoDescribe", JeoDescribe.class);
    xstream.alias("error", ErrorDescription.class);
    XstreamRepresentation<JeoDescribe> rep = new XstreamRepresentation<JeoDescribe>(media, describe);
    rep.setXstream(xstream);
    return rep;
  }

  /**
   * Encode a JeoDescribe into a Representation according to the given variant
   * 
   * @param describe
   *          the error Object
   * @param variant
   *          the variant
   * @return Representation
   */
  public Representation getRepresentation(JeoDescribe describe, Variant variant) {
    MediaType defaultMediaType = this.getMediaType(variant);
    return getRepresentation(describe, defaultMediaType);
  }
}

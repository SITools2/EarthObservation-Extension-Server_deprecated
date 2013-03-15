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
package fr.cnes.sitools.ext.jeobrowser.upload;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Put;

import fr.cnes.sitools.common.model.Response;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.dto.DictionaryMappingDTO;
import fr.cnes.sitools.dataset.model.Column;
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.datasource.jdbc.business.SitoolsDataSourceFactory;
import fr.cnes.sitools.datasource.jdbc.model.AttributeValue;
import fr.cnes.sitools.datasource.jdbc.model.Record;
import fr.cnes.sitools.datasource.jdbc.model.Structure;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * Insert or update a Record in a dataset
 * 
 * 
 * @author m.gond
 */
public class JeoInsertRecordResource extends SitoolsParameterizedResource {
  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonGeoWKTField";
  /** String template for Insert statement */
  private String sqlTemplateInsert = "INSERT INTO \"{SCHEMA}\".{TABLE} ({COLUMNS}) VALUES ({VALUES})";
  /** String template for Update statement */
  private String sqlTemplateUpdate = "UPDATE \"{SCHEMA}\".{TABLE} SET {VALUES} WHERE {PK_KEY}=?";
  /** primary key column */
  private Column primaryKey;
  /** The dataset application */
  private DataSetApplication application;
  /** The dataset */
  private DataSet dataset;

  /** The columnAlias of the geometry column */
  private String columnAliasColGeometry = null;

  @Override
  public void doInit() {
    super.doInit();
    ResourceModel model = this.getModel();
    String dicoName = model.getParameterByName("dictionary_name").getValue();
    application = (DataSetApplication) getApplication();
    dataset = application.getDataSet();
    if (dicoName != null) {
      DictionaryMappingDTO dico = application.getColumnConceptMappingDTO(dicoName);
      if (dico != null) {
        List<String> columnsAlias = dico.getListColumnAliasMapped(CONCEPT_NAME);
        if (columnsAlias.size() == 0) {
          getContext().getLogger().log(Level.INFO, dataset.getName() + " no column mapped for concept " + CONCEPT_NAME);
        }
        else if (columnsAlias.size() > 1) {
          getContext().getLogger().log(Level.INFO,
              dataset.getName() + " too many columns mapped for concept " + CONCEPT_NAME);
        }
        else {
          columnAliasColGeometry = columnsAlias.get(0);
        }

      }
    }

  }

  @Override
  public void sitoolsDescribe() {
    setName("JeoBrowserInsertRecordResource");
    setDescription("Resource plugin for jeoBrowser to upload data");
  }

  /**
   * Insert or Update the given Record contained in the representation
   * 
   * @param representation
   *          a Representation representing a Record
   * @param variant
   *          the Variant of the Representation
   * @return a Representation of a Response
   */
  @SuppressWarnings("static-access")
  @Put
  public Representation insert(Representation representation, Variant variant) {
    Response response = null;
    if (!getApplication().getClass().equals(DataSetApplication.class)) {
      response = new Response(false, "not attached to a dataset");
      return getRepresentation(response, variant);
    }

    Record record = getObject(representation);
    if (record == null) {
      response = new Response(false, "No record sent");
      return getRepresentation(response, variant);
    }

    List<Structure> structures = dataset.getStructures();
    if (structures.size() > 1) {
      response = new Response(false, "Cannot work on a multiple table dataset");
      return getRepresentation(response, variant);
    }
    Structure struct = structures.get(0);
    primaryKey = getPrimaryKey(dataset);
    // check if the record exists
    boolean exists = checkRecordExists(dataset, record);
    String sqlString;
    if (!exists) {
      sqlString = createSQLInsert(struct, record);
    }
    else {
      sqlString = createSQLUpdate(struct, record, dataset);
    }

    // recuperation d’une connexion à la source de données
    Connection con = null;
    PreparedStatement stm = null;
    try {
      con = SitoolsDataSourceFactory.getInstance().getDataSource(dataset.getDatasource().getId()).getConnection();

      con.setReadOnly(false);

      stm = con.prepareStatement(sqlString);
      Column col;
      List<AttributeValue> attr = record.getAttributeValues();
      int i = 1;
      for (Iterator<AttributeValue> it = attr.iterator(); it.hasNext();) {
        AttributeValue attrValue = it.next();
        col = dataset.findByColumnAlias(attrValue.getName());
        if (col != null) {
          stm.setObject(i++, attrValue.getValue(), col.getJavaSqlColumnType());
        }
      }

      if (exists) {
        stm.setString(attr.size() + 1, record.getId());
      }

      getLogger().log(Level.INFO, "REAL SQL : " + stm.toString());
      stm.execute();
      if (stm.getWarnings() == null) {
        response = new Response(true, "Record inserted");
        getResponse().setStatus(Status.SUCCESS_CREATED);
      }
      else {
        response = new Response(false, "Created with warnings : " + stm.getWarnings());
        getResponse().setStatus(Status.SUCCESS_CREATED);
      }

    }
    catch (SQLException e) {
      response = new Response(false, e.getLocalizedMessage());
      e.printStackTrace();
    }
    finally {
      if (stm != null) {
        try {
          stm.close();
        }
        catch (SQLException e) {
          getLogger().severe(e.getMessage());
        }
      }
      if (con != null) {
        try {
          con.setReadOnly(true);
          con.close();
        }
        catch (SQLException e) {
          getLogger().severe(e.getMessage());
        }
      }
    }

    return getRepresentation(response, variant);

  }

  @Override
  public void describePut(MethodInfo info) {
    info.setDocumentation("Method to insert or update a record in a dataset");
    info.setIdentifier("insert_record_dataset");
    addStandardPostOrPutRequestInfo(info);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  /**
   * Create a String representing a SQL Insert statement for the following
   * structure and Record
   * 
   * @param struct
   *          the Structure
   * @param record
   *          the Record
   * @return a String representing a SQL Insert statement
   */
  private String createSQLInsert(Structure struct, Record record) {
    String sqlInsert = sqlTemplateInsert;

    sqlInsert = sqlInsert.replace("{SCHEMA}", struct.getSchemaName());
    sqlInsert = sqlInsert.replace("{TABLE}", struct.getName());

    String columnsStr = "";
    String valuesStr = "";

    List<AttributeValue> attr = record.getAttributeValues();
    // List<Column> cols = dataset.getColumnModel();
    boolean start = true;
    for (Iterator<AttributeValue> it = attr.iterator(); it.hasNext();) {
      AttributeValue attrValue = it.next();
      Column col = dataset.findByColumnAlias(attrValue.getName());
      if (col != null) {
        if (!start) {
          columnsStr += ',';
          valuesStr += ',';
        }
        else {
          start = false;
        }

        columnsStr += col.getDataIndex();

        // added for jeobrowser
        if (columnAliasColGeometry != null && attrValue.getName().equals(columnAliasColGeometry)) {
          valuesStr += "ST_GeomFromText(?, 4326)";
        }
        else {
          valuesStr += '?';
        }
      }
    }

    sqlInsert = sqlInsert.replace("{COLUMNS}", columnsStr);
    sqlInsert = sqlInsert.replace("{VALUES}", valuesStr);
    getLogger().log(Level.INFO, "SQL : " + sqlInsert);
    return sqlInsert;
  }

  /**
   * Create a String representing a SQL Update statement for the following
   * structure, Record and DataSet
   * 
   * @param struct
   *          the Structure
   * @param record
   *          the Record
   * @param dataset
   *          the DataSet
   * @return a String representing a SQL Update statement
   */
  private String createSQLUpdate(Structure struct, Record record, DataSet dataset) {
    String sqlUpdate = sqlTemplateUpdate;

    sqlUpdate = sqlUpdate.replace("{SCHEMA}", struct.getSchemaName());
    sqlUpdate = sqlUpdate.replace("{TABLE}", struct.getName());

    String valuesStr = "";
    List<AttributeValue> attr = record.getAttributeValues();
    boolean start = true;
    for (Iterator<AttributeValue> it = attr.iterator(); it.hasNext();) {
      AttributeValue attrValue = it.next();
      Column col = dataset.findByColumnAlias(attrValue.getName());
      if (col != null) {
        if (!start) {
          valuesStr += ',';
        }
        else {
          start = false;
        }

        // TODO attention specific
        if (columnAliasColGeometry != null && attrValue.getName().equals(columnAliasColGeometry)) {
          valuesStr += col.getDataIndex() + "=ST_GeomFromText(?, 4326)";
        }
        else {
          valuesStr += col.getDataIndex() + "=?";
        }
      }
    }
    sqlUpdate = sqlUpdate.replace("{PK_KEY}", primaryKey.getDataIndex());

    sqlUpdate = sqlUpdate.replace("{VALUES}", valuesStr);
    getLogger().log(Level.INFO, "SQL : " + sqlUpdate);
    return sqlUpdate;
  }

  /**
   * Gets Record object from Representation
   * 
   * @param representation
   *          of a Record
   * @return Record
   */
  public final Record getObject(Representation representation) {
    Record object = null;

    if (MediaType.APPLICATION_XML.isCompatible(representation.getMediaType())) {
      // Parse the XML representation to get the dataset bean
      object = new XstreamRepresentation<Record>(representation).getObject();

    }
    else if (MediaType.APPLICATION_JSON.isCompatible(representation.getMediaType())) {
      // Parse the JSON representation to get the bean
      object = new JacksonRepresentation<Record>(representation, Record.class).getObject();
    }

    else if (MediaType.APPLICATION_JAVA_OBJECT.isCompatible(representation.getMediaType())) {
      @SuppressWarnings("unchecked")
      ObjectRepresentation<Record> obj = (ObjectRepresentation<Record>) representation;
      try {
        object = obj.getObject();
      }
      catch (IOException e) {
        object = null;
      }
    }

    return object;
  }

  /**
   * Check whether the record exists or not in the dataset
   * 
   * @param dataset
   *          the DataSet to query
   * @param record
   *          the record to check
   * @return true if the record exists, false otherwise
   */
  private boolean checkRecordExists(DataSet dataset, Record record) {
    Request reqGET = new Request(Method.GET, RIAPUtils.getRiapBase() + dataset.getSitoolsAttachementForUsers()
        + "/records/" + record.getId());
    ArrayList<Preference<MediaType>> objectMediaType = new ArrayList<Preference<MediaType>>();
    objectMediaType.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
    reqGET.getClientInfo().setAcceptedMediaTypes(objectMediaType);
    org.restlet.Response response = null;

    response = getContext().getClientDispatcher().handle(reqGET);

    if (response == null || Status.isError(response.getStatus().getCode())) {
      RIAPUtils.exhaust(response);
      return false;
    }

    String rsp = response.getEntityAsText();
    boolean exists = !rsp.contains("{\"success\":false}");

    return exists;

  }

  /**
   * Gets the column which is the primary key of a dataset
   * 
   * @param dataset
   *          the DataSet
   * @return the primary key column of the dataset
   */
  private Column getPrimaryKey(DataSet dataset) {
    List<Column> col = dataset.getColumnModel();
    Column pkKey = null;
    for (Iterator<Column> iterator = col.iterator(); iterator.hasNext() && pkKey == null;) {
      Column column = iterator.next();
      if (column.isPrimaryKey()) {
        pkKey = column;
      }
    }
    return pkKey;
  }
}

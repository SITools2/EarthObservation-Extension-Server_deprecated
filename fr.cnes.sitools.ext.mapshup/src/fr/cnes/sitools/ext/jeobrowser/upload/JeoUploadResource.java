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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.data.Status;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Post;

import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.common.model.Response;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.dto.DictionaryMappingDTO;
import fr.cnes.sitools.dataset.model.BehaviorEnum;
import fr.cnes.sitools.dataset.model.Column;
import fr.cnes.sitools.dataset.model.ColumnRenderer;
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.datasource.jdbc.model.AttributeValue;
import fr.cnes.sitools.datasource.jdbc.model.Record;
import fr.cnes.sitools.ext.jeobrowser.upload.model.Entry;
import fr.cnes.sitools.ext.jeobrowser.upload.model.JsonEntry;
import fr.cnes.sitools.ext.jeobrowser.upload.model.MediaDetails;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.tasks.TaskUtils;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * Upload a Zip containing a JSON object and some medias object Insert the
 * records contained in the JSON object and copy the medias object to a
 * directory Also works with a single JSON file
 * 
 * @author m.gond
 */
public class JeoUploadResource extends SitoolsParameterizedResource {
  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonGeoWKTField";
  /** The zip file Path */
  private String filePath;
  /** The dataset */
  private DataSet dataset;
  /** The url of the datastorage */
  private String datastorageUrl;
  /** The SitoolsSettings */
  private SitoolsSettings settings;
  /** The clientInfo */
  private ClientInfo clientInfo;
  /** The dataset application */
  private DataSetApplication application;

  /** The columnAlias of the geometry column */
  private String columnAliasColGeometry = null;
  /** The url of the JeoInsertResource on the Entry dataset */
  private String entryUrlInsertResource;
  /** The url of the JeoInsertResource on the Media dataset */
  private String mediaUrlInsertResource;

  @Override
  public void doInit() {
    super.doInit();
    settings = ((SitoolsApplication) getApplication()).getSettings();
    clientInfo = getRequest().getClientInfo();
    setNegotiated(true);

    Form query = this.getRequest().getResourceRef().getQueryAsForm();

    datastorageUrl = query
        .getFirstValue(JeoUploadResourceModel.PARAM_DATASTORAGE_URL);
    if (datastorageUrl == null || datastorageUrl.equals("")) {
      datastorageUrl = getOverrideParameterValue(JeoUploadResourceModel.PARAM_DATASTORAGE_URL);
    }
    ResourceModel model = this.getModel();
    String dicoName = model.getParameterByName("dictionary_name").getValue();
    application = (DataSetApplication) this.getApplication();
    dataset = application.getDataSet();
    if (dicoName != null) {
      DictionaryMappingDTO dico = application
          .getColumnConceptMappingDTO(dicoName);
      if (dico != null) {
        List<String> columnsAlias = dico.getListColumnAliasMapped(CONCEPT_NAME);
        if (columnsAlias.size() == 0) {
          getContext().getLogger().log(
              Level.INFO,
              dataset.getName() + " no column mapped for concept "
                  + CONCEPT_NAME);
        }
        else if (columnsAlias.size() > 1) {
          getContext().getLogger().log(
              Level.INFO,
              dataset.getName() + " too many columns mapped for concept "
                  + CONCEPT_NAME);
        }
        else {
          columnAliasColGeometry = columnsAlias.get(0);
        }

      }
    }
    entryUrlInsertResource = model.getParameterByName("entryUrlInsertResource")
        .getValue();
    mediaUrlInsertResource = model.getParameterByName("mediaUrlInsertResource")
        .getValue();

  }

  @Override
  public void sitoolsDescribe() {
    setName("JeoBrowserUploadResource");
    setDescription("Resource plugin for jeoBrowser to upload data");
  }

  /**
   * Upload a Zip containing a JSON object and some medias object Insert the
   * records contained in the JSON object and copy the medias object to a
   * directory Also works with a single JSON file
   * 
   * @param representation
   *          the Entity sent
   * @param variant
   *          the variant needed
   * @return a Response
   */
  @Post
  public Representation upload(Representation representation, Variant variant) {
    fr.cnes.sitools.common.model.Response response = null;
    this.getRequestAttributes().keySet();
    if (!getApplication().getClass().equals(DataSetApplication.class)) {
      response = new fr.cnes.sitools.common.model.Response(false,
          "not attached to a dataset");
      return getRepresentation(response, variant);
    }
    dataset = ((DataSetApplication) getApplication()).getDataSet();

    if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
      JsonEntry jsonEntry = new JacksonRepresentation<JsonEntry>(
          representation, JsonEntry.class).getObject();
      Entry entry = jsonEntry.getEntry();

      response = insertEntryMetaData(entry);
    }
    else {

      // generate directory name
      String directoryName = DateUtils.format(new Date(),
          TaskUtils.getTimestampPattern());

      Representation rep = uploadFile(representation, directoryName);

      // if there was an error during the upload
      if (getResponse().getStatus().isError()) {
        return rep;
      }

      // get the zip
      if (filePath != null) {
        LocalReference fr = LocalReference.createFileReference(filePath);
        Reference zr = new Reference("zip:" + fr.toString());
        ClientResource crFile = new ClientResource(zr);
        Representation zipDetails = crFile.get();

        try {
          ReferenceList refList = new ReferenceList(zipDetails);
          // loop through the files of the zip
          Reference jsonRef = null;
          for (Iterator<Reference> iterator = refList.iterator(); iterator
              .hasNext() && jsonRef == null;) {
            Reference reference = iterator.next();
            if ("json".equals(reference.getExtensions())) {
              jsonRef = reference;
              iterator.remove();
            }
            if ("txt".equals(reference.getExtensions())) {
              jsonRef = reference;
              iterator.remove();
            }
          }
          crFile.release();
          crFile = new ClientResource(jsonRef);
          Representation json = crFile.get();

          JsonEntry jsonEntry = new JacksonRepresentation<JsonEntry>(json,
              JsonEntry.class).getObject();
          Entry entry = jsonEntry.getEntry();

          response = insertEntryMetaData(entry);
          if (entry.getMedia() != null) {
            if (response.isSuccess()) {
              response = insertMedia(entry);
            }
            if (response.isSuccess()) {
              response = copyMedia(entry, refList, datastorageUrl);
              if (response.isSuccess()) {
                response = new Response(true, "Zip successfully uploaded");
              }
            }
          }

        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    }

    // boolean folderDeleted = deleteTemporaryFolder(directoryName);
    // if (!folderDeleted) {
    // response = new Response(false, "temporary folder not deleted");
    // }
    if (!response.getSuccess()) {
      getResponse().setStatus(Status.SUCCESS_OK);
    }
    else {
      getResponse().setStatus(Status.SUCCESS_CREATED);
    }
    return getRepresentation(response, variant);
  }

  /**
   * Copy a set of media files contained in the zip to the specified
   * datastorageUrl
   * 
   * @param entry
   *          the Entry
   * @param refList
   *          the list of media files
   * @param datastorageUrl
   *          the storage url
   * @return a Response
   */
  private Response copyMedia(Entry entry, ReferenceList refList,
      String datastorageUrl) {
    Response response = null;
    if (entry == null || refList == null) {
      response = new Response(false,
          "No entry specified or reference list specified");
    }

    for (Iterator<Reference> iterator = refList.iterator(); iterator.hasNext();) {
      Reference reference = iterator.next();

      ClientResource crFile = new ClientResource(reference);
      Representation fileToCopy = crFile.get();

      int indexExcla = reference.getPath().lastIndexOf(
          "!/" + entry.getIdentifier());
      String fileUrl = reference.getPath().substring(
          indexExcla + 2 + entry.getIdentifier().length());

      Request reqPOST = new Request(Method.PUT, RIAPUtils.getRiapBase()
          + settings.getString(Consts.APP_DATASTORAGE_URL) + datastorageUrl
          + fileUrl, fileToCopy);

      reqPOST.setClientInfo(clientInfo);
      org.restlet.Response r = null;
      try {
        r = getContext().getClientDispatcher().handle(reqPOST);

        if (r == null) {
          response = new Response(false, "ERROR COPYING FILE : " + reference);
          break;
        }
        else if (Status.CLIENT_ERROR_FORBIDDEN.equals(r.getStatus())) {
          response = new Response(false, "CLIENT_ERROR_FORBIDDEN : "
              + reference);
          break;
        }
        else if (Status.CLIENT_ERROR_UNAUTHORIZED.equals(r.getStatus())) {
          response = new Response(false, "CLIENT_ERROR_UNAUTHORIZED : "
              + reference);
          break;
        }
        else if (Status.isError(r.getStatus().getCode())) {
          response = new Response(false, "ERROR : " + r.getStatus().getName()
              + " getting file : " + reference);
          break;
        }
        else {
          response = new Response(true, "file " + reference + " copied");
        }
      }
      finally {
        if (r != null) {
          RIAPUtils.exhaust(r);
        }
      }
    }

    return response;
  }

  /**
   * Insert the record contained in the following entry
   * 
   * @param entry
   *          the Entry
   * @return a Response
   */
  private Response insertEntryMetaData(Entry entry) {
    Response response = null;
    if (entry == null) {
      response = new Response(false, "No entry specified");
      return response;
    }

    // Entry
    Record recordEntry = getRecordFromEntry(entry);
    return insertRecord(recordEntry, dataset.getSitoolsAttachementForUsers(),
        entryUrlInsertResource);
  }

  /**
   * Insert the medias contained in the following entry
   * 
   * @param entry
   *          the entry
   * @return a Response
   */
  private Response insertMedia(Entry entry) {
    Response response = null;
    if (entry == null) {
      response = new Response(false, "No entry specified");
    }

    String datasetMediaUrl = null;
    List<Column> columns = dataset.getColumnModel();
    for (Iterator<Column> iterator = columns.iterator(); iterator.hasNext();) {
      Column column = iterator.next();
      ColumnRenderer columnRenderer = column.getColumnRenderer();
      if (columnRenderer != null
          && columnRenderer.getBehavior() != null
          && (columnRenderer.getBehavior() == BehaviorEnum.datasetIconLink || columnRenderer
              .getBehavior() == BehaviorEnum.datasetLink)) {
        datasetMediaUrl = columnRenderer.getDatasetLinkUrl();
      }
    }

    if (datasetMediaUrl != null) {
      do {
        // Medias
        // Video
        if (entry.getMedia().getVideo().size() > 0) {
          response = insertMediaRecord(entry.getMedia().getVideo(),
              datasetMediaUrl, "video", entry);
          if (response == null) {
            response = new Response(false, "Error while inserting media video");
            break;
          }
          else if (!response.isSuccess()) {
            break;
          }
        }
        // Photo
        if (entry.getMedia().getPhoto().size() > 0) {
          response = insertMediaRecord(entry.getMedia().getPhoto(),
              datasetMediaUrl, "photo", entry);
          if (response == null) {
            response = new Response(false, "Error while inserting media photo");
            break;
          }
          else if (!response.isSuccess()) {
            break;
          }
        }
        // audio
        if (entry.getMedia().getAudio().size() > 0) {
          response = insertMediaRecord(entry.getMedia().getAudio(),
              datasetMediaUrl, "audio", entry);
          if (response == null) {
            response = new Response(false, "Error while inserting media audio");
            break;
          }
          else if (!response.isSuccess()) {
            break;
          }
        }
      } while (false);
    }

    return response;
  }

  /**
   * Create a Record from an Entry
   * 
   * @param entry
   *          the Entry
   * @return a Record Object
   */
  private Record getRecordFromEntry(Entry entry) {
    Record record = new Record();
    record.setId(entry.getIdentifier());

    AttributeValue attrIdentifier = new AttributeValue("identifier",
        entry.getIdentifier());
    record.getAttributeValues().add(attrIdentifier);

    AttributeValue attrDate = new AttributeValue("date", entry.getDate());
    record.getAttributeValues().add(attrDate);

    AttributeValue attrCoord = new AttributeValue(columnAliasColGeometry,
        "POINT(" + entry.getLongitude() + " " + entry.getLatitude() + ")");
    record.getAttributeValues().add(attrCoord);

    AttributeValue attrEle = new AttributeValue("ele", entry.getEle());
    record.getAttributeValues().add(attrEle);

    AttributeValue attrBuildingId = new AttributeValue("building_identifier",
        entry.getBuilding().getIdentifier());
    record.getAttributeValues().add(attrBuildingId);

    AttributeValue attrBuildingPeopleNb = new AttributeValue(
        "building_peoplenb", entry.getBuilding().getPeoplesNb());
    record.getAttributeValues().add(attrBuildingPeopleNb);

    AttributeValue attrBuildingState = new AttributeValue("building_state",
        entry.getBuilding().getState());
    record.getAttributeValues().add(attrBuildingState);

    AttributeValue attrNote = new AttributeValue("notes", entry.getNotes());
    record.getAttributeValues().add(attrNote);

    return record;

  }

  /**
   * Create a Record from a MediaDetails
   * 
   * @param media
   *          the MediaDetails
   * @param type
   *          the type of the media (audio, video, photo)
   * @param entryId
   *          the id of the parent entry
   * @return a Record Object
   */
  private Record getRecordFromMedia(MediaDetails media, String type,
      String entryId) {
    Record record = new Record();
    record.setId(type + media.getIdentifier() + entryId);

    AttributeValue attrIdentifier = new AttributeValue("identifier",
        media.getIdentifier());
    record.getAttributeValues().add(attrIdentifier);

    AttributeValue attrName = new AttributeValue("name", media.getName());
    record.getAttributeValues().add(attrName);

    AttributeValue attrDirectory = new AttributeValue("directory",
        media.getDirectory());
    record.getAttributeValues().add(attrDirectory);

    AttributeValue attrType = new AttributeValue("type", type);
    record.getAttributeValues().add(attrType);

    AttributeValue attrEntryId = new AttributeValue("entry_id", entryId);
    record.getAttributeValues().add(attrEntryId);

    AttributeValue attrExtension = new AttributeValue("extension",
        media.getExtension());
    record.getAttributeValues().add(attrExtension);

    return record;

  }

  /**
   * Insert the given record in the given dataset
   * 
   * @param record
   *          the Record
   * @param datasetUrl
   *          the datasetUrl
   * @param resourceUrl
   *          the Url of the resource to call
   * @return the Response of the call
   */
  private Response insertRecord(Record record, String datasetUrl,
      String resourceUrl) {
    Request reqPUT = new Request(Method.PUT, RIAPUtils.getRiapBase()
        + datasetUrl + resourceUrl, new ObjectRepresentation<Record>(record));

    ArrayList<Preference<MediaType>> objectMediaType = new ArrayList<Preference<MediaType>>();
    objectMediaType.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
    reqPUT.getClientInfo().setAcceptedMediaTypes(objectMediaType);

    org.restlet.Response r = null;
    Response resp = null;
    try {
      r = getContext().getClientDispatcher().handle(reqPUT);

      @SuppressWarnings("unchecked")
      XstreamRepresentation<Response> repr = (XstreamRepresentation<Response>) r
          .getEntity();
      resp = (Response) repr.getObject();

    }
    finally {
      RIAPUtils.exhaust(r);
    }
    return resp;
  }

  /**
   * Insert a List of Media to the given dataset. Medias are of the following
   * type and correspond to the given entry
   * 
   * @param medias
   *          the List of medias
   * @param datasetUrl
   *          the dataset
   * @param type
   *          the type of medias
   * @param entry
   *          the Entry
   * @return a Response
   */
  private Response insertMediaRecord(List<MediaDetails> medias,
      String datasetUrl, String type, Entry entry) {
    Response response = null;
    for (Iterator<MediaDetails> iterator = medias.iterator(); iterator
        .hasNext();) {
      MediaDetails details = iterator.next();
      Record rec = getRecordFromMedia(details, type, entry.getIdentifier());
      response = insertRecord(rec, datasetUrl, mediaUrlInsertResource);
      if (!response.isSuccess()) {
        break;
      }
    }
    return response;
  }

  // /**
  // * Delete the temporary folder created
  // *
  // * @param directoryName
  // * the directory to delete
  // * @return true if the directory has been deleted, false otherwise
  // */
  // private boolean deleteTemporaryFolder(String directoryName) {
  //
  // File storeDirectory = new File(settings.getTmpFolderUrl() + "/" +
  // directoryName);
  // boolean fileDeleted = false;
  // if (storeDirectory.exists()) {
  // try {
  // fileDeleted = FileUtils.cleanDirectory(storeDirectory);
  // }
  // catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }
  //
  // return fileDeleted;
  //
  // }

  /**
   * Upload a File contained in the given entity to the temporary folder of the
   * Sitools Server at the given directory name
   * 
   * @param entity
   *          the file to upload
   * @param directoryName
   *          the directory
   * @return a Representation
   * 
   */
  private Representation uploadFile(Representation entity, String directoryName) {
    Representation rep = null;
    if (entity != null) {
      settings = ((SitoolsApplication) getApplication()).getSettings();
      File storeDirectory = new File(settings.getTmpFolderUrl() + "/"
          + directoryName);
      storeDirectory.mkdirs();

      if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
        // storeDirectory = "c:\\temp\\";

        // The Apache FileUpload project parses HTTP requests which
        // conform to RFC 1867, "Form-based File Upload in HTML". That
        // is, if an HTTP request is submitted using the POST method,
        // and with a content type of "multipart/form-data", then
        // FileUpload can parse that request, and get all uploaded files
        // as FileItem.

        // 1/ Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1000240);

        // 2/ Create a new file upload handler based on the Restlet
        // FileUpload extension that will parse Restlet requests and
        // generates FileItems.
        RestletFileUpload upload = new RestletFileUpload(factory);

        List<FileItem> items;

        // 3/ Request is parsed by the handler which generates a
        // list of FileItems
        try {
          items = upload.parseRequest(getRequest());
        }
        catch (FileUploadException e1) {
          e1.printStackTrace();
          items = new ArrayList<FileItem>();
        }

        // Says if at least one file has been handled
        boolean found = false;
        // List the files that haven't been uploaded
        List<String> oops = new ArrayList<String>();
        // count the number files
        int nbFiles = 0;

        for (final Iterator<FileItem> it = items.iterator(); it.hasNext();) {
          FileItem fi = it.next();
          // Process only the items that *really* contains an uploaded
          // file and save them on disk
          if (fi.getName() != null) {
            found = true;
            nbFiles++;
            File file = new File(storeDirectory, fi.getName());
            try {
              fi.write(file);
              filePath = file.getAbsolutePath();
            }
            catch (Exception e) {
              System.out.println("Can't write the content of " + file.getPath()
                  + " due to " + e.getMessage());
              oops.add(file.getName());
            }
          }
          else {
            // This is a simple text form input.
            System.out.println(fi.getFieldName() + " " + fi.getString());
          }
        }

        // Once handled, you can send a report to the user.
        StringBuilder sb = new StringBuilder();
        if (found) {
          sb.append(nbFiles);
          if (nbFiles > 1) {
            sb.append(" files sent");
          }
          else {
            sb.append(" file sent");
          }
          if (!oops.isEmpty()) {
            sb.append(", ").append(oops.size());
            if (oops.size() > 1) {
              sb.append(" files in error:");
            }
            else {
              sb.append(" file in error:");
            }
            for (int i = 0; i < oops.size(); i++) {
              if (i > 0) {
                sb.append(",");
              }
              sb.append(" \"").append(oops.get(i)).append("\"");
            }
          }
          sb.append(".");
          rep = new StringRepresentation(sb.toString(), MediaType.TEXT_PLAIN);
        }
        else {
          // Some problem occurs, sent back a simple line of text.
          rep = new StringRepresentation("no file uploaded",
              MediaType.TEXT_PLAIN);
        }
      }
      else {
        String fileName = "tmp_zip.zip";
        String resourceRef = RIAPUtils.getRiapBase()
            + settings.getString(Consts.APP_TMP_FOLDER_URL) + "/"
            + directoryName + "/" + fileName;
        filePath = settings.getTmpFolderUrl() + "/" + directoryName + "/"
            + fileName;
        // Transfer of PUT calls is only allowed if the readOnly flag is
        // not set.
        Request contextRequest = new Request(Method.PUT, resourceRef);

        // Add support of partial PUT calls.
        contextRequest.getRanges().addAll(getRanges());
        contextRequest.setEntity(entity);

        org.restlet.Response contextResponse = new org.restlet.Response(
            contextRequest);
        contextRequest.setResourceRef(resourceRef);
        getContext().getClientDispatcher().handle(contextRequest,
            contextResponse);
        setStatus(contextResponse.getStatus());

      }
    }
    else {
      // POST request with no entity.
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    return rep;
  }

}

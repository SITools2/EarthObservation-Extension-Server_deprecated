package fr.cnes.sitools.ext.jeobrowser.upload;

import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.plugins.resources.model.DataSetSelectionType;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * Resource model for jeoBrowser
 * 
 * @author jp.boignard (AKKA technologies)
 */
public class JeoUploadResourceModel extends ResourceModel {

  /** PARAM_MEDIAS_DIR */
  public static final String PARAM_DATASTORAGE_URL = "datastorage_url";

  /**
   * Constructor
   * 
   * TODO namespace for extensions
   * xmlns:sitools="http://sitools2.sourceforge.net/opensearchextensions/1.0/"
   * 
   * "sitools" + url > � rajouter dans les caract�ristiques d'un dictionary ? >
   * param�tre du model ?
   * 
   * le nom "sitools"
   * 
   * Niveau 0. "sitools" en dur dans le ftl + service description + service
   * describe.
   * 
   */
  public JeoUploadResourceModel() {
    super();

    setName("JeoUploadResourceModel");
    setDescription("Plugin resource to upload Jeobrowser files");
    setClassAuthor("AKKA technologies");
    setClassOwner("CNES");
    setClassVersion("0.1");
    setResourceClassName("fr.cnes.sitools.ext.jeobrowser.upload.JeoUploadResource");

    ResourceParameter datastorageUrl = new ResourceParameter(PARAM_DATASTORAGE_URL, "Url of the datastorage",
        ResourceParameterType.PARAMETER_USER_INPUT);
    datastorageUrl.setValue("/jeo/medias");

    this.getParameterByName("url").setValue("/jeo/upload");

    ResourceParameter dicoName = new ResourceParameter("dictionary_name", "The name of the dictionary to use",
        ResourceParameterType.PARAMETER_INTERN);
    dicoName.setValue("JeoDictionary");
    this.addParam(dicoName);

    ResourceParameter entryUrlInsertResource = new ResourceParameter("entryUrlInsertResource",
        "The url of the JeoInsertResource on the Entry dataset", ResourceParameterType.PARAMETER_INTERN);
    entryUrlInsertResource.setValue("/jeo/records/insert");
    this.addParam(entryUrlInsertResource);

    ResourceParameter mediaUrlInsertResource = new ResourceParameter("mediaUrlInsertResource",
        "The url of the JeoInsertResource on the Media dataset", ResourceParameterType.PARAMETER_INTERN);
    mediaUrlInsertResource.setValue("/jeo/records/insert");
    this.addParam(mediaUrlInsertResource);

    this.addParam(datastorageUrl);
    this.setApplicationClassName(DataSetApplication.class.getName());
    this.setDataSetSelection(DataSetSelectionType.NONE);

  }

}

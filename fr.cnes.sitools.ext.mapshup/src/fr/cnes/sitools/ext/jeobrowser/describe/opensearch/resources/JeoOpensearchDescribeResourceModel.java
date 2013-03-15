package fr.cnes.sitools.ext.jeobrowser.describe.opensearch.resources;

import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.ext.resources.freemarker.FreemarkerResourceModel;
import fr.cnes.sitools.plugins.resources.model.DataSetSelectionType;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * Resource model for jeoBrowser
 * 
 * @author jp.boignard (AKKA technologies)
 */
public class JeoOpensearchDescribeResourceModel extends FreemarkerResourceModel {

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
  public JeoOpensearchDescribeResourceModel() {
    super();

    setName("JeoOpensearchDescribeResourceModel");
    setDescription("Plugin resource for opensearch service desciption, entry point for jeoBrowser");
    setClassAuthor("AKKA technologies");
    setClassOwner("CNES");
    setClassVersion("0.1");
    setResourceClassName("fr.cnes.sitools.ext.jeobrowser.describe.opensearch.resources.JeoOpensearchDescribeResource");

    getParameterByName("templateFilename").setValue("opensearchTemplate.ftl");

    ResourceParameter searchName = new ResourceParameter("name", "name of the open search",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter searchLongName = new ResourceParameter("longname", "long name of the open search",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter searchDescription = new ResourceParameter("description", "description of the open search",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter searchTags = new ResourceParameter("tags", "tags associated",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter searchContact = new ResourceParameter("contact", "contact for this service",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter searchTemplate = new ResourceParameter("template", "template of the open search",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter dicoName = new ResourceParameter("dictionary_name", "The name of the dictionary to use",
        ResourceParameterType.PARAMETER_INTERN);
    ResourceParameter descriptionUrl = new ResourceParameter("url_description",
        "The Url of the search description service", ResourceParameterType.PARAMETER_INTERN);

    searchName.setValue("SITOOLS2 search");
    searchLongName.setValue("SITools example search");
    searchDescription.setValue("SITools2 connector example to jeoBrowser");
    searchTags.setValue("sitools2");
    searchContact.setValue("admin@sitools2.com");
    searchTemplate
        .setValue("/opensearch/search?q={searchTerms}&amp;pw={startPage?}&amp;count={count?}&amp;start={start?}&amp;bbox={geo:box?}&amp;format=json&amp;startDate={time:start?}&amp;completionDate={time:stop?}&amp;lat={geo:lat?}&amp;lon={geo:lon?}&amp;r={geo:radius?}");
    searchTemplate.setValueType("xs:url");
    descriptionUrl.setValue("/jeo/opensearch/describe");

    dicoName.setValue("JeoDictionary");

    this.addParam(searchName);
    this.addParam(searchLongName);
    this.addParam(searchDescription);
    this.addParam(searchTags);
    this.addParam(searchContact);
    this.addParam(searchTemplate);
    this.addParam(dicoName);
    this.addParam(descriptionUrl);

    this.getParameterByName("url").setValue("/jeo/opensearch.xml");
    this.setApplicationClassName(DataSetApplication.class.getName());
    this.setDataSetSelection(DataSetSelectionType.NONE);
  }

}

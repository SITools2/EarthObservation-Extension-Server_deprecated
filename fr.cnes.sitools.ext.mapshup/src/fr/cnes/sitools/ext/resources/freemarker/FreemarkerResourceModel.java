package fr.cnes.sitools.ext.resources.freemarker;

import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * Resource model for jeoBrowser
 * 
 * @author m.marseille (AKKA technologies)
 */
public class FreemarkerResourceModel extends ResourceModel {

  /**
   * Constructor
   */
  public FreemarkerResourceModel() {
    super();

    setName("FreemarkerModel");
    setDescription("Plugin resource for freemarker transformation");
    setClassAuthor("AKKA technologies");
    setClassOwner("CNES");
    setClassVersion("0.1");
    setResourceClassName("fr.cnes.sitools.ext.resources.freemarker.FreemarkerResource");

    ResourceParameter templateFilename = new ResourceParameter(
        "templateFilename",
        "freemarker template filename (file must be in TEMPLATE_DIR)",
        ResourceParameterType.PARAMETER_INTERN);
    addParam(templateFilename);
  }

}

/**
 * 
 */
package fr.cnes.sitools.ext.jeobrowser.describe.opensearch.resources;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.dto.DictionaryMappingDTO;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;

/**
 * Resource class for jeoBrowser resource plugin Fill in the params for
 * 
 * @author jp.boignard (AKKA technologies)
 */
public class JeoOpensearchDescribeResource extends SitoolsParameterizedResource {
  /** Concept name for specific date field */
  private static final String CONCEPT_DATE_NAME = "specificDateField";
  /** Concept name for specific number field */
  private static final String CONCEPT_NUMBER_NAME = "specificNumberField";
  /** Concept name for specific text field */
  private static final String CONCEPT_TEXT_NAME = "specificTextField";
  /** Concept name for specific enumeration with not only unique value field */
  private static final String CONCEPT_ENUM_NOT_UNIQUE_NAME = "specificEnumerationFieldNotUnique";
  /** Concept name for specific enumeration with unique value field */
  private static final String CONCEPT_ENUM_UNIQUE_NAME = "specificEnumerationFieldUnique";

  @Override
  public void doInit() {
    super.doInit();
  }

  @Override
  public void sitoolsDescribe() {
    setName("JeoDescriptionServiceResource");
    setDescription("Resource plugin for jeoBrowser opensearch template exposition");
    setNegotiated(false);
  }

  @Get
  @Override
  public Representation get() {
    ResourceModel model = this.getModel();
    String templateFTL = model.getParameterByName("templateFilename").getValue();
    templateFTL = ((SitoolsApplication) getApplication()).getSettings().getFormattedString(templateFTL);
    File template = new File(templateFTL);
    if (!template.exists()) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Template not found: " + templateFTL);
    }
    Representation opensearchFtl = new ClientResource(LocalReference.createFileReference(templateFTL)).get();

    DataSetApplication app = (DataSetApplication) getApplication();
    if (app == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "can not find DataSet Application");
    }
    String dicoName = model.getParameterByName("dictionary_name").getValue();
    if (dicoName != null) {
      DictionaryMappingDTO dico = app.getColumnConceptMappingDTO(dicoName);
      if (dico != null) {
        ResourceParameter paramUrl = model.getParameterByName("template");
        String url = paramUrl.getValue();
        // gets the dictionaryMappings for each concept

        url = addParamsToTemplate(url, CONCEPT_DATE_NAME, dico);
        url = addParamsToTemplate(url, CONCEPT_NUMBER_NAME, dico);
        url = addParamsToTemplate(url, CONCEPT_TEXT_NAME, dico);
        url = addParamsToTemplate(url, CONCEPT_ENUM_NOT_UNIQUE_NAME, dico);
        url = addParamsToTemplate(url, CONCEPT_ENUM_UNIQUE_NAME, dico);

        ResourceParameter paramUrlOverride = new ResourceParameter();
        paramUrlOverride.setDescription(paramUrl.getDescription());
        paramUrlOverride.setName(paramUrl.getName());
        paramUrlOverride.setType(paramUrl.getType());
        paramUrlOverride.setValue(url);
        paramUrlOverride.setValueObject(paramUrl.getValueObject());
        paramUrlOverride.setValueType(paramUrl.getValueType());
        

        this.getOverrideParams().add(paramUrlOverride);

      }
    }

    // Representation opensearchFtl = new
    // ClientResource(LocalReference.createClapReference(getClass().getPackage())
    // + "/opensearchTemplate.ftl").get();
    Representation repr = new TemplateRepresentation(opensearchFtl, this, MediaType.TEXT_XML);
    return repr;
  }

  @Override
  protected void describeGet(MethodInfo info) {
    this.addInfo(info);
  }

  /**
   * add the params mapped to the given conceptName in the
   * {@link DictionaryMappingDTO} to the given template
   * 
   * @param template
   *          the template
   * @param conceptName
   *          the concept name
   * @param dico
   *          the DictionaryMappingDTO
   * @return the template with the parameters added
   * @throws UnsupportedEncodingException
   */
  private String addParamsToTemplate(String template, String conceptName, DictionaryMappingDTO dico) {
    List<String> columnsAliasDate = dico.getListColumnAliasMapped(conceptName);
    for (Iterator<String> iterator = columnsAliasDate.iterator(); iterator.hasNext();) {
      String colAliasDate = (String) iterator.next();
      template += "&amp;" + colAliasDate + "={sitools:" + colAliasDate + "?}";
    }
    return template;
  }

}

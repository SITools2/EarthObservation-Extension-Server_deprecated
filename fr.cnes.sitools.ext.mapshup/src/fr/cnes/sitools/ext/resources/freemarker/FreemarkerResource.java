/**
 * 
 */
package fr.cnes.sitools.ext.resources.freemarker;

import java.io.File;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.common.resource.SitoolsParameterizedResource;
import fr.cnes.sitools.server.Consts;

/**
 * Resource class for jeoBrowser resource plugin
 * 
 * @author m.marseille (AKKA technologies)
 */
public class FreemarkerResource extends SitoolsParameterizedResource {

  @Override
  public void sitoolsDescribe() {
    setName("FreemarkerResource");
    setDescription("Resource plugin for freemarker template exposition");
    setNegotiated(false);
  }

  @Override
  public Representation get() {
    //
    String templatePath = ((SitoolsApplication) getApplication()).getSettings()
        .getString(Consts.TEMPLATE_DIR);
    templatePath += getModel().getParameterByName("template").getValue();
    File templateFile = new File(templatePath);
    if (!templateFile.exists()) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
          "Freemarker template not found: " + templatePath);
    }
    LocalReference ref = LocalReference.createFileReference(templatePath);

    Representation templateFtl = new ClientResource(ref).get();
    Representation repr = new TemplateRepresentation(templateFtl, this,
        MediaType.TEXT_XML);
    return repr;
  }

  @Override
  protected void describeGet(MethodInfo info) {
    this.addInfo(info);
  }

}

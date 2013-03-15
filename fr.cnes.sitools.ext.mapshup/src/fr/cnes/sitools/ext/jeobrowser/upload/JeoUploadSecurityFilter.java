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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;

import fr.cnes.sitools.security.filter.SecurityFilter;

/**
 * Filter that filter on uploaded file size
 * 
 * 
 * @author m.gond
 */
public class JeoUploadSecurityFilter extends SecurityFilter {
  /**
   * Upload max Size
   */
  private long maxSize;

  /**
   * Filter default constructor
   * 
   * @param context
   *          the Context
   */
  public JeoUploadSecurityFilter(Context context) {
    super(context);
    maxSize = getSettings().getLong("Security.filter.upload.maxsize");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.cnes.sitools.security.filter.SecurityFilter#beforeHandle(org.restlet
   * .Request, org.restlet.Response)
   */
  @Override
  protected int beforeHandle(Request request, Response response) {

    int status = super.beforeHandle(request, response);
    Form form = (Form) request.getAttributes().get("org.restlet.http.headers");
    MediaType media = MediaType.valueOf(form.getFirstValue("Content-Type"));

    if (MediaType.MULTIPART_FORM_DATA.isCompatible(media)) {
      String contentLengthStr = form.getFirstValue("Content-Length");
      if (contentLengthStr == null) {
        status = STOP;
        response.setStatus(Status.CLIENT_ERROR_LENGTH_REQUIRED, "No content length specified");
      }
      else {
        long contentLength = new Long(contentLengthStr);
        if (contentLength > maxSize) {
          status = STOP;
          response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE, "Your file was to big to be uploaded");
        }
      }
    }
    return status;
  }
}

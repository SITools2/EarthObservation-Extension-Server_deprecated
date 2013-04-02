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
package fr.cnes.sitools.ext.jeobrowser.search.resources;

import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * 
 * JeoSearchSpecificResourceModel
 * 
 * @author m.gond
 */
public class JeoSearchSpecificResourceModel extends JeoSearchResourceModel {

  /**
   * JeoSearchSpecificResourceModel
   */
  public JeoSearchSpecificResourceModel() {
    super();
    setName("JeoSearchSpecificResourceModel");
    setDescription("GEO JSON export on the fly specific for Building datasets");
    setResourceClassName("fr.cnes.sitools.ext.jeobrowser.search.resources.JeoSearchSpecificResource");

    setClassVersion("0.3");

    ResourceParameter paramColUrl = new ResourceParameter("col_url",
        "The column used as url for the media (in the media dataset)",
        ResourceParameterType.PARAMETER_INTERN);
    paramColUrl.setValue("url");
    paramColUrl.setValueType("xs:string");
    this.addParam(paramColUrl);

    ResourceParameter paramColMedia = new ResourceParameter("col_type",
        "The column used as the type of media (in the media dataset)",
        ResourceParameterType.PARAMETER_INTERN);
    paramColMedia.setValue("type");
    paramColMedia.setValueType("xs:string");
    this.addParam(paramColMedia);

    ResourceParameter paramColIdentifier = new ResourceParameter(
        "col_identifier",
        "The column used as identifier for the media (in the media dataset)",
        ResourceParameterType.PARAMETER_INTERN);
    paramColIdentifier.setValue("identifier");
    paramColIdentifier.setValueType("xs:string");
    this.addParam(paramColIdentifier);

    ResourceParameter paramColName = new ResourceParameter("col_name",
        "The column used as name for the media (in the media dataset)",
        ResourceParameterType.PARAMETER_INTERN);
    paramColName.setValue("name");
    paramColName.setValueType("xs:string");
    this.addParam(paramColName);

  }

}

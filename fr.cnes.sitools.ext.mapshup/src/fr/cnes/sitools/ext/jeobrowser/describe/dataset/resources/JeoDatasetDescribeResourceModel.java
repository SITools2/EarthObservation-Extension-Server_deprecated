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

import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.plugins.resources.model.DataSetSelectionType;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * Model resource for resource describing search parameters on datasets
 * 
 * 
 * @author m.gond
 */
public class JeoDatasetDescribeResourceModel extends ResourceModel {

  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Constructor
   */
  public JeoDatasetDescribeResourceModel() {
    super();

    setName("JeoDatasetDescribeResourceModel");
    setDescription("Resource plugin to retrieve dataset description in JSON format");
    setClassAuthor("AKKA technologies");
    setClassOwner("CNES");
    setClassVersion("0.1");
    setResourceClassName("fr.cnes.sitools.ext.jeobrowser.describe.dataset.resources.JeoDatasetDescribeResource");

    ResourceParameter dicoName = new ResourceParameter(dicoParamName, "The name of the dictionary to use",
        ResourceParameterType.PARAMETER_INTERN);
    dicoName.setValue("JeoDictionary");
    this.addParam(dicoName);

    this.getParameterByName("url").setValue("/jeo/opensearch/describe");
    this.setApplicationClassName(DataSetApplication.class.getName());
    this.setDataSetSelection(DataSetSelectionType.NONE);
  }

}

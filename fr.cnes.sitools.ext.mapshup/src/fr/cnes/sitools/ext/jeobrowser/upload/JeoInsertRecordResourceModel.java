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

import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.plugins.resources.model.DataSetSelectionType;
import fr.cnes.sitools.plugins.resources.model.ResourceModel;
import fr.cnes.sitools.plugins.resources.model.ResourceParameter;
import fr.cnes.sitools.plugins.resources.model.ResourceParameterType;

/**
 * ResourceModel for JeoBrowserInserRecordResource
 * 
 * 
 * @author m.gond
 */
public class JeoInsertRecordResourceModel extends ResourceModel {

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
  public JeoInsertRecordResourceModel() {
    super();

    setName("JeoBrowserInsertRecordsResourceModel");
    setDescription("Plugin resource to insert a record on a single table dataset");
    setClassAuthor("AKKA technologies");
    setClassOwner("CNES");
    setClassVersion("0.1");
    setResourceClassName("fr.cnes.sitools.ext.jeobrowser.upload.JeoInsertRecordResource");

    this.getParameterByName("url").setValue("/jeo/records/insert");

    ResourceParameter dicoName = new ResourceParameter("dictionary_name", "The name of the dictionary to use",
        ResourceParameterType.PARAMETER_INTERN);
    dicoName.setValue("JeoDictionary");

    this.addParam(dicoName);

    this.setApplicationClassName(DataSetApplication.class.getName());
    this.setDataSetSelection(DataSetSelectionType.NONE);

  }

}

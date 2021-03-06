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

import org.restlet.Context;
import org.restlet.representation.Representation;

import fr.cnes.sitools.dataset.converter.business.ConverterChained;
import fr.cnes.sitools.dataset.database.DatabaseRequestParameters;
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.ext.jeobrowser.search.representations.GeoJsonRepresentation;

/**
 * Classic implementation of JeoSearch resource
 * 
 * 
 * @author m.gond
 */
public class JeoSearchResource extends AbstractJeoSearchResource {

  @Override
  public Representation getRepresentation(DatabaseRequestParameters params, String geometryColName,
      ConverterChained converterChained, DataSet dataset, Context context) {
    return new GeoJsonRepresentation(params, geometryColName, converterChained, dataset, getContext());
  }

}

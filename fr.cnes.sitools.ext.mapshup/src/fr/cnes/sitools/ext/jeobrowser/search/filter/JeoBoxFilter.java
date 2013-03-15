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
package fr.cnes.sitools.ext.jeobrowser.search.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;

import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.exception.SitoolsException;
import fr.cnes.sitools.common.validator.ConstraintViolation;
import fr.cnes.sitools.common.validator.ConstraintViolationLevel;
import fr.cnes.sitools.common.validator.Validator;
import fr.cnes.sitools.dataset.DataSetApplication;
import fr.cnes.sitools.dataset.dto.DictionaryMappingDTO;
import fr.cnes.sitools.dataset.filter.business.AbstractFilter;
import fr.cnes.sitools.dataset.filter.model.FilterParameter;
import fr.cnes.sitools.dataset.filter.model.FilterParameterType;
import fr.cnes.sitools.dataset.model.Column;
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.dataset.model.Predicat;
import fr.cnes.sitools.dictionary.model.Dictionary;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * A filter for box querying
 * 
 * 
 * @author m.gond
 */
public class JeoBoxFilter extends AbstractFilter {

  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonGeoWKTField";

  /** the name of the parameter in the url */
  private String boxParamName = "bbox_param_name";
  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Default constructor
   */
  public JeoBoxFilter() {

    super();
    this.setName("JeoBoxFilter");
    this.setDescription("Required when using bbox JeoBrowser parameters");

    this.setClassAuthor("AKKA Technologies");
    this.setClassOwner("CNES");
    this.setClassVersion("0.1");
    this.setDefaultFilter(false);

    FilterParameter param = new FilterParameter(dicoParamName, "The name of the dictionary to use",
        FilterParameterType.PARAMETER_INTERN);
    param.setValue("JeoDictionary");

    this.addParam(param);

    FilterParameter parambbox = new FilterParameter(boxParamName,
        "The name of the url parameter to use for bbox search (bbox in bbox={geo:box})",
        FilterParameterType.PARAMETER_INTERN);
    parambbox.setValue("bbox");
    this.addParam(parambbox);

  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.dataset.filter.business.AbstractFilter#
   * getRequestParamsDescription()
   */
  @Override
  public HashMap<String, ParameterInfo> getRequestParamsDescription() {
    String boxParam = this.getParametersMap().get(boxParamName).getValue();
    HashMap<String, ParameterInfo> rpd = new HashMap<String, ParameterInfo>();
    ParameterInfo paramInfo = new ParameterInfo(
        boxParam,
        false,
        "xs:string",
        ParameterStyle.QUERY,
        "Replaced with the bounding box to search for geospatial results within. The box is defined by 'west, south, east, north' coordinates of longitude, latitude, in a EPSG:4326 decimal degrees. This is also commonly referred to by minX, minY, maxX, maxY (where longitude is the X-axis, and latitude is the Y-axis), or also SouthWest corner and NorthEast corner. ");
    rpd.put("0", paramInfo);
    return rpd;
  }

  @Override
  public Validator<?> getValidator() {
    return new Validator<AbstractFilter>() {

      @Override
      public Set<ConstraintViolation> validate(AbstractFilter item) {
        String dicoName = item.getParametersMap().get(dicoParamName).getValue();
        SitoolsSettings settings = (SitoolsSettings) getContext().getAttributes().get(ContextAttributes.SETTINGS);
        Dictionary dico = RIAPUtils.getObjectFromName(settings.getString(Consts.APP_DICTIONARIES_URL), dicoName,
            getContext());
        HashSet<ConstraintViolation> constraints = new HashSet<ConstraintViolation>();

        if (dico == null) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setMessage("Dictionary given does not exists");
          constraint.setInvalidValue(dicoName);
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setValueName(dicoParamName);
          constraints.add(constraint);
        }
        return constraints;
      }
    };
  }

  @Override
  public List<Predicat> createPredicats(Request request, List<Predicat> predicats) throws Exception {

    Form form = request.getResourceRef().getQueryAsForm();
    String boxParam = this.getParametersMap().get(boxParamName).getValue();
    String box = form.getFirstValue(boxParam);

    if (box != null) {
      DataSetApplication app = (DataSetApplication) getContext().getAttributes().get("DataSetApplication");
      if (app == null) {
        throw new SitoolsException("can not find DataSet Application");
      }
      DataSet ds = app.getDataSet();
      String dicoName = this.getParametersMap().get(dicoParamName).getValue();
      if (dicoName != null) {
        DictionaryMappingDTO dico = app.getColumnConceptMappingDTO(dicoName);
        if (dico != null) {
          // gets the dictionaryMapping
          List<String> columnsAlias = dico.getListColumnAliasMapped(CONCEPT_NAME);
          if (columnsAlias.size() == 0) {
            getContext().getLogger().log(Level.INFO, ds.getName() + " no column mapped for concept " + CONCEPT_NAME);
          }
          else if (columnsAlias.size() > 1) {
            getContext().getLogger().log(Level.INFO,
                ds.getName() + " too many columns mapped for concept " + CONCEPT_NAME);
          }
          else {
            String geometryColumnName = columnsAlias.get(0);
            if (geometryColumnName != null) {
              Column col = ds.findByColumnAlias(geometryColumnName);
              if (col != null) {
                String[] boxCoord = box.split(",");
                if (boxCoord.length == 4) {

                  double minX = new Double(boxCoord[0]);
                  double minY = new Double(boxCoord[1]);
                  double maxX = new Double(boxCoord[2]);
                  double maxY = new Double(boxCoord[3]);

                  String point1 = minX + " " + minY;
                  String point2 = maxX + " " + minY;
                  String point3 = maxX + " " + maxY;
                  String point4 = minX + " " + maxY;

                  String geomStr = "POLYGON((" + point1 + "," + point2 + "," + point3 + "," + point4 + "," + point1
                      + "))";

                  Predicat pred = new Predicat();
                  pred.setLeftAttribute(col);
                  pred.setCompareOperator("&&");
                  pred.setRightValue("ST_GeomFromText('" + geomStr + "', 4326)");
                  predicats.add(pred);
                }
              }
            }
          }
        }
      }

    }

    return null;
  }
}

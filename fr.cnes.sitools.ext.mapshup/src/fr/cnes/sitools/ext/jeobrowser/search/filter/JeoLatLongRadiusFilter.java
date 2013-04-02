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
import fr.cnes.sitools.dataset.model.DataSet;
import fr.cnes.sitools.dataset.model.Predicat;
import fr.cnes.sitools.dictionary.model.Dictionary;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.util.RIAPUtils;

/**
 * Filter to perform search around a specified point with a given radius
 * 
 * 
 * @author m.gond
 */
public class JeoLatLongRadiusFilter extends AbstractFilter {
  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonGeoWKTField";

  /** latitude parameter name */
  private String filterLatParam = "lat_param_name";
  /** longitude parameter name */
  private String filterLonParam = "lon_param_name";
  /** radius parameter name */
  private String filterRadiusParam = "r_param_name";
  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Default constructor
   */
  public JeoLatLongRadiusFilter() {

    super();
    this.setName("JeoBrowserLatLongRadiusFilter");
    this.setDescription("Required when using Lat, Lon, Radius JeoBrowser parameters");

    this.setClassAuthor("AKKA Technologies");
    this.setClassOwner("CNES");
    this.setClassVersion("0.1");
    this.setDefaultFilter(false);

    FilterParameter param = new FilterParameter(dicoParamName, "The name of the dictionary to use",
        FilterParameterType.PARAMETER_INTERN);
    param.setValue("JeoDictionary");
    this.addParam(param);

    FilterParameter paramLat = new FilterParameter(filterLatParam,
        "The name of the url parameter to use for lat parameter in the search (lat in lat={geo:lat?})",
        FilterParameterType.PARAMETER_INTERN);
    paramLat.setValue("lat");
    this.addParam(paramLat);

    FilterParameter paramLong = new FilterParameter(filterLonParam,
        "The name of the url parameter to use for lon parameter in the search (lon in lon={geo:lon?})",
        FilterParameterType.PARAMETER_INTERN);
    paramLong.setValue("lon");
    this.addParam(paramLong);

    FilterParameter paramR = new FilterParameter(filterRadiusParam,
        "The name of the url parameter to use for radius parameter in the search (r in r={geo:radius?})",
        FilterParameterType.PARAMETER_INTERN);
    paramR.setValue("r");
    this.addParam(paramR);

  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.dataset.filter.business.AbstractFilter#
   * getRequestParamsDescription()
   */
  @Override
  public HashMap<String, ParameterInfo> getRequestParamsDescription() {
    HashMap<String, ParameterInfo> rpd = new HashMap<String, ParameterInfo>();
    String latParam = this.getParametersMap().get(filterLatParam).getValue();
    ParameterInfo paramInfo = new ParameterInfo(
        latParam,
        false,
        "xs:double",
        ParameterStyle.QUERY,
        "Replaced with the 'latitude', respectively, in decimal degrees in EPSG:4326 (typical WGS84 coordinates as returned by a GPS receiver).");
    rpd.put("0", paramInfo);
    String lonParam = this.getParametersMap().get(filterLonParam).getValue();
    paramInfo = new ParameterInfo(
        lonParam,
        false,
        "xs:double",
        ParameterStyle.QUERY,
        "Replaced with the 'longitude', respectively, in decimal degrees in EPSG:4326 (typical WGS84 coordinates as returned by a GPS receiver).");
    rpd.put("1", paramInfo);
    String radiusParam = this.getParametersMap().get(filterRadiusParam).getValue();
    paramInfo = new ParameterInfo(
        radiusParam,
        false,
        "xs:double",
        ParameterStyle.QUERY,
        "The radius parameter, used with the "
            + filterLatParam
            + " and "
            + filterLonParam
            + " parameters, specifies the search distance from this point. The distance is in meters along the Earth's surface.");
    rpd.put("2", paramInfo);
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
    String latParam = this.getParametersMap().get(filterLatParam).getValue();
    String lat = form.getFirstValue(latParam);
    String lonParam = this.getParametersMap().get(filterLonParam).getValue();
    String longitude = form.getFirstValue(lonParam);
    String radiusParam = this.getParametersMap().get(filterRadiusParam).getValue();
    String radius = form.getFirstValue(radiusParam);

    if (lat != null && longitude != null && radius != null) {
      DataSetApplication app = (DataSetApplication) getContext().getAttributes().get("DataSetApplication");
      if (app == null) {
        throw new SitoolsException("can not find DataSet Application");
      }
      DataSet ds = app.getDataSet();
      // get the geometry column name
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
            String geometry = "POINT(" + lat + " " + longitude + ")";
            Predicat pred = new Predicat();
            pred.setStringDefinition(" AND ST_DWithin(" + geometryColumnName + ", ST_GeomFromText('" + geometry
                + "', 4326)," + radius + ")");
            predicats.add(pred);
          }
        }
      }

    }
    return predicats;
  }
}

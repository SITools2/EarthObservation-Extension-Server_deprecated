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
import fr.cnes.sitools.dataset.model.Operator;
import fr.cnes.sitools.dataset.model.Predicat;
import fr.cnes.sitools.dictionary.model.Dictionary;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.util.RIAPUtils;
import fr.cnes.sitools.util.SQLUtils;

/**
 * Filter for time parameters
 * 
 * 
 * @author m.gond
 */
public class JeoTimeFilter extends AbstractFilter {
  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonTimeField";
  /** the dateStartParameterName */
  private String dateStartParamName = "start_param_name";
  /** the dateEndParameterName */
  private String dateEndParamName = "end_param_name";
  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Default constructor
   */
  public JeoTimeFilter() {

    super();
    this.setName("JeoBrowserTimeFilter");
    this.setDescription("Required when using time JeoBrowser parameters");

    this.setClassAuthor("AKKA Technologies");
    this.setClassOwner("CNES");
    this.setClassVersion("0.1");
    this.setDefaultFilter(false);

    FilterParameter param = new FilterParameter(dicoParamName, "The name of the dictionary to use",
        FilterParameterType.PARAMETER_INTERN);
    param.setValue("JeoDictionary");
    this.addParam(param);

    FilterParameter paramStart = new FilterParameter(dateStartParamName,
        "The key of the parameter to use for time start search (start in dtstart={time:start})",
        FilterParameterType.PARAMETER_INTERN);
    paramStart.setValue("startDate");
    this.addParam(paramStart);

    FilterParameter paramEnd = new FilterParameter(dateEndParamName,
        "The key of the parameter to use for time start search (end in dtend={time:end})",
        FilterParameterType.PARAMETER_INTERN);
    paramEnd.setValue("completionDate");
    this.addParam(paramEnd);

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

    String startParam = this.getParametersMap().get(dateStartParamName).getValue();
    String endParam = this.getParametersMap().get(dateEndParamName).getValue();

    ParameterInfo paramInfo = new ParameterInfo(
        startParam,
        false,
        "xs:date",
        ParameterStyle.QUERY,
        "Replaced with a string of the beginning of the time slice of the search query. This string should match the RFC-3339 - Date and Time on the Internet: Timestamps, which is also used by the Atom syndication format. ");
    rpd.put("0", paramInfo);

    paramInfo = new ParameterInfo(
        endParam,
        false,
        "xs:date",
        ParameterStyle.QUERY,
        "Replaced with a string of the end of the time slice of the search query. This string should match the RFC-3339 - Date and Time on the Internet: Timestamps, which is also used by the Atom syndication format. ");
    rpd.put("1", paramInfo);

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
    String startParam = this.getParametersMap().get(dateStartParamName).getValue();
    String start = form.getFirstValue(startParam);
    String endParam = this.getParametersMap().get(dateEndParamName).getValue();
    String end = form.getFirstValue(endParam);

    if (start != null && end != null) {
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
                // escape the values to avoid SQL injection
                String value1 = "'" + SQLUtils.escapeString(start) + "'";
                String value2 = "'" + SQLUtils.escapeString(end) + "'";

                Predicat predicat = new Predicat();
                predicat.setLeftAttribute(col);
                predicat.setNbOpenedParanthesis(1);
                predicat.setNbClosedParanthesis(0);
                predicat.setCompareOperator(Operator.GT.value());
                predicat.setRightValue(value1);
                predicats.add(predicat);
                predicat = new Predicat();
                predicat.setLeftAttribute(col);
                predicat.setNbOpenedParanthesis(0);
                predicat.setNbClosedParanthesis(1);
                predicat.setCompareOperator(Operator.LT.value());
                predicat.setRightValue(value2);
                predicats.add(predicat);
              }
            }
          }
        }
      }
    }
    return predicats;

  }

}

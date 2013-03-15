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
 * Filter to search with searchTerm parameter on JeoBrowser
 * 
 * 
 * @author m.gond
 */
public class JeoSearchTermsFilter extends AbstractFilter {
  /** The name of the concept to look for in the dictionary mapping */
  private static final String CONCEPT_NAME = "commonSearchTermsField";

  /** the name of the parameter in the url */
  private String searchTermsParamName = "search_terms_param_name";
  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Default constructor
   */
  public JeoSearchTermsFilter() {

    super();
    this.setName("JeoSearchTermsFilter");
    this.setDescription("Required when using SearchTerms JeoBrowser parameters");

    this.setClassAuthor("AKKA Technologies");
    this.setClassOwner("CNES");
    this.setClassVersion("0.1");
    this.setDefaultFilter(false);

    FilterParameter param = new FilterParameter(dicoParamName, "The name of the dictionary to use",
        FilterParameterType.PARAMETER_INTERN);
    param.setValue("JeoDictionary");
    this.addParam(param);

    FilterParameter parambbox = new FilterParameter(searchTermsParamName,
        "The name of the url parameter to use for searchTerms search (q in q={searchTerms})",
        FilterParameterType.PARAMETER_INTERN);
    parambbox.setValue("q");
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
    String boxParam = this.getParametersMap().get(searchTermsParamName).getValue();
    HashMap<String, ParameterInfo> rpd = new HashMap<String, ParameterInfo>();
    ParameterInfo paramInfo = new ParameterInfo(boxParam, false, "xs:string", ParameterStyle.QUERY,
        "Replaced with the keyword or keywords desired by the search client.");
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
    String searchTermsParam = this.getParametersMap().get(searchTermsParamName).getValue();
    String searchTerms = form.getFirstValue(searchTermsParam);

    if (searchTerms != null) {
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
            String searchTermsColumnName = columnsAlias.get(0);
            if (searchTermsColumnName != null) {
              Column col = ds.findByColumnAlias(searchTermsColumnName);
              if (col != null) {
                String value = SQLUtils.escapeString(searchTerms);
                Predicat predicat = new Predicat();
                if (value != null) {
                  predicat.setLeftAttribute(col);
                  predicat.setNbOpenedParanthesis(0);
                  predicat.setNbClosedParanthesis(0);
                  // replace * by % in the searchTerms entry.
                  value = value.replace("*", "%");
                  predicat.setCompareOperator(Operator.LIKE.value());
                  predicat.setRightValue("'" + value + "'");
                  predicats.add(predicat);
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

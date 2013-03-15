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
import fr.cnes.sitools.dataset.dto.ColumnConceptMappingDTO;
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
 * Filter for specific Search
 * 
 * 
 * @author m.gond
 */
public class JeoSpecificSearchFilter extends AbstractFilter {
  /** Concept name for specific date field */
  private static final String CONCEPT_DATE_NAME = "specificDateField";
  /** Concept name for specific number field */
  private static final String CONCEPT_NUMBER_NAME = "specificNumberField";
  /** Concept name for specific text field */
  private static final String CONCEPT_TEXT_NAME = "specificTextField";
  /** Concept name for specific enumeration with not only unique value field */
  private static final String CONCEPT_ENUM_NOT_UNIQUE_NAME = "specificEnumerationFieldNotUnique";
  /** Concept name for specific enumeration with unique value field */
  private static final String CONCEPT_ENUM_UNIQUE_NAME = "specificEnumerationFieldUnique";

  /** The name of the parameter for dictionary name */
  private String dicoParamName = "dictionary_name";

  /**
   * Default constructor
   */
  public JeoSpecificSearchFilter() {

    super();
    this.setName("JeoSpecificSearchFilter");
    this.setDescription("Required when using specified JeoBrowser parameters search");

    this.setClassAuthor("AKKA Technologies");
    this.setClassOwner("CNES");
    this.setClassVersion("0.1");
    this.setDefaultFilter(false);

    FilterParameter param = new FilterParameter(dicoParamName, "The name of the dictionary to use",
        FilterParameterType.PARAMETER_INTERN);
    param.setValue("JeoDictionary");
    this.addParam(param);

  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.dataset.filter.business.AbstractFilter#
   * getRequestParamsDescription()
   */
  @Override
  public HashMap<String, ParameterInfo> getRequestParamsDescription() {
    String dicoName = this.getParametersMap().get(dicoParamName).getValue();
    HashMap<String, ParameterInfo> rpd = new HashMap<String, ParameterInfo>();
    if (dicoName != null) {
      DataSetApplication app = (DataSetApplication) getContext().getAttributes().get("DataSetApplication");
      if (app != null) {
        DictionaryMappingDTO dico = app.getColumnConceptMappingDTO(dicoName);

        List<ColumnConceptMappingDTO> list = dico.getMapping();
        for (ColumnConceptMappingDTO mapping : list) {
          String conceptName = mapping.getConcept().getName();
          if (CONCEPT_DATE_NAME.equals(conceptName) || CONCEPT_NUMBER_NAME.equals(conceptName)
              || CONCEPT_TEXT_NAME.equals(conceptName) || CONCEPT_ENUM_NOT_UNIQUE_NAME.equals(conceptName)
              || CONCEPT_ENUM_UNIQUE_NAME.equals(conceptName)) {

            ParameterInfo paramInfo = new ParameterInfo(mapping.getColumnAlias(), false, "xs:string",
                ParameterStyle.QUERY, "Specific parameter " + mapping.getColumnAlias());
            rpd.put(String.valueOf(rpd.size()), paramInfo);
          }

        }

      }
    }

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

    DataSetApplication app = (DataSetApplication) getContext().getAttributes().get("DataSetApplication");
    if (app == null) {
      throw new SitoolsException("can not find DataSet Application");
    }
    String dicoName = this.getParametersMap().get(dicoParamName).getValue();
    if (dicoName != null) {
      DictionaryMappingDTO dico = app.getColumnConceptMappingDTO(dicoName);
      if (dico != null) {
        DataSet ds = app.getDataSet();
        
        
        Form form = request.getResourceRef().getQueryAsForm();
        List<ColumnConceptMappingDTO> list = dico.getMapping();
        for (ColumnConceptMappingDTO mapping : list) {
          String conceptName = mapping.getConcept().getName();
          String colAlias = mapping.getColumnAlias();
          if (CONCEPT_DATE_NAME.equals(conceptName)) {
            String value = form.getFirstValue(colAlias);
            Column col = ds.findByColumnAlias(colAlias);
            predicats.addAll(JeoSpecificPredicats.getPredicatsDate(value, col));
          }
          else if (CONCEPT_NUMBER_NAME.equals(conceptName)) {
            String value = form.getFirstValue(colAlias);
            Column col = ds.findByColumnAlias(colAlias);
            predicats.addAll(JeoSpecificPredicats.getPredicatsNumber(value, col, mapping.getConcept()));
          }
          else if (CONCEPT_TEXT_NAME.equals(conceptName)) {
            String value = form.getFirstValue(colAlias);
            Column col = ds.findByColumnAlias(colAlias);
            predicats.addAll(JeoSpecificPredicats.getPredicatsText(value, col));
          }
          else if (CONCEPT_ENUM_NOT_UNIQUE_NAME.equals(conceptName) || CONCEPT_ENUM_UNIQUE_NAME.equals(conceptName)) {
            String value = form.getFirstValue(colAlias);
            Column col = ds.findByColumnAlias(colAlias);
            predicats.addAll(JeoSpecificPredicats.getPredicatsEnum(value, col));
          }

        }

      }
    }

    return null;
  }
}

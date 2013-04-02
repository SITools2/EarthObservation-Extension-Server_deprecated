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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.dataset.model.Column;
import fr.cnes.sitools.dataset.model.Operator;
import fr.cnes.sitools.dataset.model.Predicat;
import fr.cnes.sitools.dictionary.model.Concept;
import fr.cnes.sitools.util.Property;
import fr.cnes.sitools.util.SQLUtils;

/**
 * Util classes to create predicats for specific JeoBrowser parameters
 * 
 * 
 * @author m.gond
 */
public final class JeoSpecificPredicats {
  /** The operator property name */
  private static final String OPERATOR_PROP_NAME = "operator";

  /** Private constructor for utility class */
  private JeoSpecificPredicats() {
  }

  /**
   * Create predicats for date parameter Handle a date or a date interval. Date
   * interval are splitted with a /
   * 
   * 
   * @param value
   *          the String value
   * @param col
   *          the Column from the dataset
   * @return the list of Predicats
   * @throws Exception
   *           if there is an error while escaping the string value
   */
  public static List<Predicat> getPredicatsDate(String value, Column col) throws Exception {
    List<Predicat> predicats = new ArrayList<Predicat>();
    if (value != null && col != null) {

      if (value.contains("/")) {
        String[] values = value.split("/");
        // escape the values to avoid SQL injection
        String value1 = "'" + SQLUtils.escapeString(values[0]) + "'";
        String value2 = "'" + SQLUtils.escapeString(values[1]) + "'";
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
      else {
        // escape the values to avoid SQL injection
        value = "'" + SQLUtils.escapeString(value) + "'";
        Predicat predicat = new Predicat();
        predicat.setLeftAttribute(col);
        predicat.setNbOpenedParanthesis(0);
        predicat.setNbClosedParanthesis(0);
        predicat.setCompareOperator(Operator.EQ.value());
        predicat.setRightValue(value);
        predicats.add(predicat);

      }

    }
    return predicats;
  }

  /**
   * Create predicats for Number parameter
   * 
   * 
   * @param valueStr
   *          the String value
   * @param col
   *          the Column from the dataset
   * @param concept
   *          The concept
   * @return the list of Predicats
   * @throws Exception
   *           if the value given is not a Number
   */
  public static List<Predicat> getPredicatsNumber(String valueStr, Column col, Concept concept) throws Exception {
    List<Predicat> predicats = new ArrayList<Predicat>();
    if (valueStr != null && col != null && concept != null) {
      Property operatorProp = concept.getPropertyFromName(OPERATOR_PROP_NAME);
      boolean isBetween = valueStr.contains("/");
      boolean isOpBetween = operatorProp.getValue().equals("bt");
      if (!isOpBetween && !isBetween) {
        Double value;
        try {
          value = Double.valueOf(valueStr);
        }
        catch (NumberFormatException e) {
          throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Not numeric value entered", e);
        }

        if (operatorProp != null) {
          String operator = operatorProp.getValue();
          Operator op = null;
          if (operator.equals("eq")) {
            op = Operator.EQ;
          }
          else if (operator.equals("gt")) {
            op = Operator.GT;
          }
          else if (operator.equals("lt")) {
            op = Operator.LT;
          }

          if (op != null) {
            Predicat predicat = new Predicat();
            predicat.setLeftAttribute(col);
            predicat.setNbOpenedParanthesis(0);
            predicat.setNbClosedParanthesis(0);
            predicat.setCompareOperator(op.value());
            predicat.setRightValue(value);
            predicats.add(predicat);
          }
        }

      }

      else if (isOpBetween && isBetween) {
        String[] values = valueStr.split("/");
        if (values.length == 2) {
          Double value1;
          Double value2;
          try {
            value1 = Double.valueOf(values[0]);
            value2 = Double.valueOf(values[1]);
          }
          catch (NumberFormatException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Not numeric value entered", e);
          }
          Predicat predicat = new Predicat();
          predicat.setLeftAttribute(col);
          predicat.setNbOpenedParanthesis(1);
          predicat.setNbClosedParanthesis(0);
          predicat.setCompareOperator(Operator.GTE.value());
          predicat.setRightValue(value1);
          predicats.add(predicat);
          predicat = new Predicat();
          predicat.setLeftAttribute(col);
          predicat.setNbOpenedParanthesis(0);
          predicat.setNbClosedParanthesis(1);
          predicat.setCompareOperator(Operator.LTE.value());
          predicat.setRightValue(value2);
          predicats.add(predicat);

        }

      }
    }
    return predicats;
  }

  /**
   * Create predicats for text parameter
   * 
   * 
   * @param value
   *          the String value
   * @param col
   *          the Column from the dataset
   * @return the list of Predicats
   * @throws Exception
   *           if there is an error while escaping the string value
   */
  public static List<Predicat> getPredicatsText(String value, Column col) throws Exception {
    List<Predicat> predicats = new ArrayList<Predicat>();
    if (value != null && col != null) {
      // escape the values to avoid SQL injection
      value = "'" + SQLUtils.escapeString(value) + "'";
      Predicat predicat = new Predicat();
      predicat.setLeftAttribute(col);
      predicat.setNbOpenedParanthesis(0);
      predicat.setNbClosedParanthesis(0);
      predicat.setCompareOperator(Operator.EQ.value());
      predicat.setRightValue(value);
      predicats.add(predicat);
    }
    return predicats;
  }

  /**
   * Create predicats for enumeration parameters. Differents values are splitted
   * with a | characters
   * 
   * 
   * @param value
   *          the String value
   * @param col
   *          the Column from the dataset
   * @return the list of Predicats
   * @throws Exception
   *           if there is an error while escaping the string value
   */
  public static List<Predicat> getPredicatsEnum(String value, Column col) throws Exception {
    List<Predicat> predicats = new ArrayList<Predicat>();
    if (value != null && col != null) {
      Predicat predicat = new Predicat();
      predicat.setLeftAttribute(col);
      predicat.setNbOpenedParanthesis(0);
      predicat.setNbClosedParanthesis(0);
      predicat.setCompareOperator(Operator.IN.value());

      String[] values = value.split("\\|");

      String in = "";
      String glue = "";
      for (String val : values) {
        // escape every value to avoid SQL injections
        in += glue + "'" + SQLUtils.escapeString(val) + "'";
        glue = ", ";
      }
      predicat.setRightValue("(" + in + ")");

      predicats.add(predicat);

    }
    return predicats;
  }
}

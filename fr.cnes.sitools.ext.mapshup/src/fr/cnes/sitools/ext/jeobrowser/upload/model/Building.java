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
package fr.cnes.sitools.ext.jeobrowser.upload.model;

/**
 * Model object for Building
 * 
 * @author m.gond
 */
public class Building {
  /** The identifier */
  private String identifier;
  /** The peoplesNb field */
  private Integer peoplesNb;
  /** The state */
  private String state;

  /**
   * Gets the identifier value
   * 
   * @return the identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Sets the value of identifier
   * 
   * @param identifier
   *          the identifier to set
   */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * Gets the peoplesNb value
   * 
   * @return the peoplesNb
   */
  public Integer getPeoplesNb() {
    return peoplesNb;
  }

  /**
   * Sets the value of peoplesNb
   * 
   * @param peoplesNb
   *          the peoplesNb to set
   */
  public void setPeoplesNb(Integer peoplesNb) {
    this.peoplesNb = peoplesNb;
  }

  /**
   * Gets the state value
   * 
   * @return the state
   */
  public String getState() {
    return state;
  }

  /**
   * Sets the value of state
   * 
   * @param state
   *          the state to set
   */
  public void setState(String state) {
    this.state = state;
  }

}

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

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Entry object
 * 
 * 
 * @author m.gond
 */
public class Entry {
  /** Entry identifier */
  private String identifier;
  /** Entry date */
  private Date date;
  /** Longitude */
  private Double longitude;
  /** latitude */
  private Double latitude;
  /** Elevation */
  private Double ele;
  /** Building object */
  private Building building;
  /** Media object */
  private Media media;
  /** Notes */
  private String notes;

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
   * Gets the date value
   * 
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * Sets the value of date
   * 
   * @param date
   *          the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * Gets the longitude value
   * 
   * @return the longitude
   */
  @JsonProperty("long")
  public Double getLongitude() {
    return longitude;
  }

  /**
   * Sets the value of longitude
   * 
   * @param longitude
   *          the longitude to set
   */
  @JsonProperty("long")
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  /**
   * Gets the latitude value
   * 
   * @return the latitude
   */
  @JsonProperty("lat")
  public Double getLatitude() {
    return latitude;
  }

  /**
   * Sets the value of latitude
   * 
   * @param latitude
   *          the latitude to set
   */
  @JsonProperty("lat")
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   * Gets the building value
   * 
   * @return the building
   */
  public Building getBuilding() {
    return building;
  }

  /**
   * Sets the value of building
   * 
   * @param building
   *          the building to set
   */
  public void setBuilding(Building building) {
    this.building = building;
  }

  /**
   * Gets the media value
   * 
   * @return the media
   */
  public Media getMedia() {
    return media;
  }

  /**
   * Sets the value of media
   * 
   * @param media
   *          the media to set
   */
  public void setMedia(Media media) {
    this.media = media;
  }

  /**
   * Sets the value of ele
   * 
   * @param ele
   *          the ele to set
   */
  public void setEle(Double ele) {
    this.ele = ele;
  }

  /**
   * Gets the ele value
   * 
   * @return the ele
   */
  public Double getEle() {
    return ele;
  }

  /**
   * Sets the value of notes
   * 
   * @param notes
   *          the notes to set
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Gets the notes value
   * 
   * @return the notes
   */
  public String getNotes() {
    return notes;
  }

}

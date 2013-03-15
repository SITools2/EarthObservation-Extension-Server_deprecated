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

import java.util.List;

/**
 * List of Medias
 * 
 * 
 * @author m.gond
 */
public class Media {
  /** list of video medias */
  private List<MediaDetails> video;
  /** List of photo medias */
  private List<MediaDetails> photo;
  /** list of audio medias */
  private List<MediaDetails> audio;

  /**
   * Gets the video value
   * 
   * @return the video
   */
  public List<MediaDetails> getVideo() {
    return video;
  }

  /**
   * Sets the value of video
   * 
   * @param video
   *          the video to set
   */
  public void setVideo(List<MediaDetails> video) {
    this.video = video;
  }

  /**
   * Gets the photo value
   * 
   * @return the photo
   */
  public List<MediaDetails> getPhoto() {
    return photo;
  }

  /**
   * Sets the value of photo
   * 
   * @param photo
   *          the photo to set
   */
  public void setPhoto(List<MediaDetails> photo) {
    this.photo = photo;
  }

  /**
   * Gets the audio value
   * 
   * @return the audio
   */
  public List<MediaDetails> getAudio() {
    return audio;
  }

  /**
   * Sets the value of audio
   * 
   * @param audio
   *          the audio to set
   */
  public void setAudio(List<MediaDetails> audio) {
    this.audio = audio;
  }

}

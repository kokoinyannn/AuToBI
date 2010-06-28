/*  ContourCenterOfGravityFeatureExtractor.java

    Copyright (c) 2009-2010 Andrew Rosenberg

    This file is part of the AuToBI prosodic analysis package.

    AuToBI is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AuToBI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AuToBI.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cuny.qc.speech.AuToBI;

import java.util.List;
import java.util.ArrayList;

/**
 * ContourCenterOfGravityFeatureExtractor identifies the "Center of Gravity" in time of a contour.
 * <p/>
 * This has typically been used on f0 contours and called "Tonal Center of Gravity" or ToCG.
 * <p/>
 * Here we allow any contour -- intensity, spectral tilt, etc -- to be processed using the same notion.
 */
public class ContourCenterOfGravityFeatureExtractor extends FeatureExtractor {
  private String attribute_name;       // the contour attribute name
  private List<TimeValuePair> values;  // the contour to extract features over

  public ContourCenterOfGravityFeatureExtractor(List<TimeValuePair> values, String attribute_name) {
    this.values = new ArrayList<TimeValuePair>();
    this.values.addAll(values);
    this.attribute_name = attribute_name;
    extracted_features.add(attribute_name + "_cog");
  }

  /**
   * Extracts center of gravity features for each region from the contour.
   *
   * @param regions The regions to extract features from.
   * @throws FeatureExtractorException if something goes wrong
   */
  public void extractFeatures(List regions) throws FeatureExtractorException {
    try {
      TimeValuePairUtils.assignValuesToRegions(regions, values, attribute_name);

      for (Region r : (List<Region>) regions) {
        List<TimeValuePair> contour  = (List<TimeValuePair>) r.getAttribute(attribute_name);

        double num = 0.0;
        double denom = 0.0;
        for (TimeValuePair tvp : contour) {
          num += tvp.getTime() * tvp.getValue();
          denom += tvp.getValue();
        }
        r.setAttribute(attribute_name + "_cog", num / denom);
      }
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
  }
}

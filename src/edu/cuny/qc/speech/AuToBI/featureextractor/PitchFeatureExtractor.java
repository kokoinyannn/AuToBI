/*  PitchFeatureExtractor.java

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
package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.IntensityExtractor;
import edu.cuny.qc.speech.AuToBI.PitchExtractor;
import edu.cuny.qc.speech.AuToBI.core.*;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PitchFeatureExtractor extracts pitch information from a given WavData object.
 * <p/>
 * v1.4 PitchFeatureExtractor has changed to attach full pitch contours to each region rather than cutting down to size
 * This is a more effective route to extracting context.
 */
@SuppressWarnings("ALL")
public class PitchFeatureExtractor extends FeatureExtractor {
  public static final String moniker = "f0";

  private String feature_name;  // the name of the feature to hold pitch information
  private double threshold;     // the intensity threshold to determine silence.

  @Deprecated
  public PitchFeatureExtractor(String feature_name) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = Double.NaN;
  }

  public PitchFeatureExtractor() {
    this.feature_name = moniker;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = Double.NaN;
  }

  @Deprecated
  public PitchFeatureExtractor(String feature_name, double threshold) {
    this.feature_name = feature_name;

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = threshold;
  }

  public PitchFeatureExtractor(double threshold) {
    this.feature_name = moniker + "[" + ((Double) threshold).toString() + "]";

    this.required_features.add("wav");
    this.extracted_features.add(feature_name);
    this.threshold = threshold;
  }

  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {

    try {
      // Identify all regions which are associated with each wav data.
      HashMap<WavData, List<Region>> wave_region_map = new HashMap<WavData, List<Region>>();
      for (Region r : (List<Region>) regions) {
        WavData wav = (WavData) r.getAttribute("wav");
        if (wav != null) {
          if (!wave_region_map.containsKey(wav)) {
            wave_region_map.put(wav, new ArrayList<Region>());
          }
          wave_region_map.get(wav).add(r);
        }
      }

      for (WavData wav : wave_region_map.keySet()) {
        PitchExtractor extractor = new PitchExtractor(wav);
        Contour pitch_contour = extractor.soundToPitch();
        if (!Double.isNaN(threshold)) {
          // Interpolate over non-silent regions
          IntensityExtractor int_extractor = new IntensityExtractor(wav);
          Contour intensity = int_extractor.soundToIntensity();
          pitch_contour = ContourUtils.interpolate(pitch_contour, intensity, threshold);
        }

        // Assign pointer to the full contour to all data points.
        for (Region r : wave_region_map.get(wav)) {
          r.setAttribute(feature_name, pitch_contour);
          Contour c = ContourUtils.getSubContour(pitch_contour, r.getStart(), r.getEnd());
        }
      }
    } catch (AuToBIException e) {
      throw new FeatureExtractorException(e.getMessage());
    }
  }
}

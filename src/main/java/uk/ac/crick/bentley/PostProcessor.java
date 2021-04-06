/*
 * Alexandros Doganis
 * The Post-Processor for the OIRTuftSegmentation plugin
 * Copyright (C) 2021 Alexandros Doganis
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of  MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.crick.bentley;

import ij.IJ;
import ij.ImagePlus;
import io.scif.services.DatasetIOService;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * The post-processor for the OIRTuftSegmentation plugin
 * @author alexandrosdoganis
 */
public class PostProcessor {

    // Private vars

    private final String originalName;           // Title of original image
    private final ImagePlus predictionImagePlus;
    private String thresholdMethodName;
    private boolean removeOutliers;
    private ImagePlus postProcessedImage;

    // Parameters

    // For logging errors
    @Parameter
    private final LogService logService;

    // For opening and saving images.
    @Parameter
    private DatasetIOService datasetIOService;

    /**
     * Constructor
     * @param ctx The SciJava application context
     * @param predictionImage the ilastik prediction ImagePlus to apply post-processing to
     **/
    public PostProcessor(Context ctx, ImagePlus predictionImage, String imageName) {
        logService = ctx.getService(LogService.class);
        logService.info("Initializing Post-Processor...");

        predictionImagePlus = predictionImage;
        originalName = imageName;
    }

    /**
     * Run post-processing steps
     */
    public void run() {
        logService.info("Running Post-Processor...");

        // Invert Lookup Table (we want white ROI on black background) - useful depending on how ilastik model exports
//        logService.info("Inverting LUT...");
//        IJ.run(predictionImagePlus, "Invert LUT", "");
//        logService.info("LUT inverted.");

        // Apply specified Auto-Threshold method
        logService.info("Thresholding...");
        String thresholdOptionString = "method=" + thresholdMethodName + " white stack";
        System.out.println(thresholdOptionString);
//        String thresholdOptionString = "method=Huang white stack";
        // BUG - feeding option parameters to automate this step results in garbage output
        // Temp solution is to have user input to default thresholding ui
        IJ.run("Auto Threshold", "method=Huang white stack");
//        IJ.run("Auto Threshold");
        logService.info("Done thresholding.");

        // Duplicate virtual stack to convert to memory stack
        logService.info("Duplicating...");
        String duplicateOptionString = "title=" +
                originalName.replaceAll("\\.tif$", "-RTS" + ".tif") + " duplicate";
        IJ.run(predictionImagePlus, "Duplicate...", duplicateOptionString);
        logService.info("Done duplicating.");

        // Get focused duplicate window to denoise
        ImagePlus rawPrediction = IJ.getImage();

        // Apply denoise steps if indicated
        if(removeOutliers) {
            logService.info("Removing outliers...");
            IJ.run(rawPrediction, "Remove Outliers...", "radius=2 threshold=50 which=Bright stack");
            logService.info("Outliers removed.");
        }

        postProcessedImage = rawPrediction;

        logService.info("Post-Processor finished");
    }

    // Mutators

    /**
     * Mutator for threhold method name
     * @param tmn String threshold method name
     */
    public void setThresholdMethodName(String tmn) { thresholdMethodName = tmn; }

    /**
     * Mutator for remove outliers
     * @param ro boolean if user wishes to denoise: remove outliers
     */
    public void setRemoveOutliers(boolean ro) { removeOutliers = ro; }

    // Accessors

    /**
     * Accessor for post-processed Imageplus
     * @return ImagePlus post-processed image
     */
    public ImagePlus getPostProcessedImage() { return postProcessedImage; }
}

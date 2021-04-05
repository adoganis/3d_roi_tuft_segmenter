/*
 * Alexandros Doganis
 * The ilastik predictor for the OIRTuftSegmentation plugin
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

import java.io.File;

/**
 * The ilastik predictor the OIRTuftSegmentation plugin
 * @author alexandrosdoganis
 */
public class IlastikPredictor {

    // Private vars
    private File ilastikProjectFile;
    private final ImagePlus scaledImagePlus;
    private ImagePlus scaledPredictionImagePlus;

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
     * @param scaledImage the scaled ImagePlus to segment
     **/
    public IlastikPredictor(Context ctx, ImagePlus scaledImage) {
        logService = ctx.getService(LogService.class);
        logService.info("Initializing IlastikPredictor");

        datasetIOService = ctx.getService(DatasetIOService.class);
        scaledImagePlus = scaledImage;
    }

    /**
     * Run prediction process
     */
    public void run() {
        logService.info("Running Ilastik Prediction...");

        // Predict tufts in image
        String ilastikOptionString = buildIlastikOptionsString(ilastikProjectFile);
        logService.info("Predicting...");
        System.out.println(ilastikOptionString);
        IJ.run(scaledImagePlus, "Run Pixel Classification Prediction", ilastikOptionString);
        logService.info("Done predicting.");

        scaledPredictionImagePlus = IJ.getImage();

        logService.info("Ilastik Prediction finished");
    }

    // Helpers

    /**
     * Helper to build ilastik prediction command option string
     * @param ipf the ilastik project filepath
     * @return a string describing the options for the ilastik command
     */
    private String buildIlastikOptionsString(File ipf) {
        String optionString = "";

        // Add filepath to project, escape backslashes and spaces
        String filePath = ipf.getAbsolutePath();
        if(File.separator == "\\") { filePath = filePath.replace("\\", "\\\\"); }   // escape '\'
        optionString += "projectfilename=[" + filePath + "] ";

        // Add input image title
        optionString += "inputimage=" + scaledImagePlus.getTitle() + " ";

        // Add segmentation specification
        optionString += "pixelclassificationtype=Segmentation";
        return optionString;
    }

    // Mutators

    /**
     * Mutator for ilastik project file
     * @param ipf trained ilastik project fiile
     */
    public void setIlastikProjectFile(File ipf) { ilastikProjectFile = ipf; }

    // Accessors

    /**
     * Accessor for predicted ImagePlus
     * @return predicted ImagePlus
     */
    public ImagePlus getScaledPredictionImagePlus() { return scaledPredictionImagePlus; }
}

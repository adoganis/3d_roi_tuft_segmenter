/*
 * Alexandros Doganis
 * The Pre-Processor for the OIRTuftSegmentation plugin
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
import net.imagej.ImgPlus;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import java.util.Arrays;
import java.util.List;

/**
 * The pre-processor the OIRTuftSegmentation plugin
 * Currently only downscales given image
 * @author alexandrosdoganis
 */
public class PreProcessor<T extends RealType<T>> {

    // Private vars
    private final ImagePlus workingImagePlus;
    private ImagePlus preProcessedImagePlus;
    private double scaleFactor;

    // Parameters

    // For logging errors
    @Parameter
    private final LogService logService;

    /**
     * Constructor
     * @param ctx The SciJava application context
     * @param imp the ImgPlus to pre-process
     **/
    public PreProcessor(Context ctx, ImgPlus<T> imp) {
        logService = ctx.getService(LogService.class);
        logService.info("Initializing Pre-Processor...");
        // Private vars
        workingImagePlus = ImageJFunctions.wrap(imp, imp.getName());
    }

    /**
     * Run pre-processing steps
     * At the moment only scales image
     */
    public void run() {
        logService.info("Running Pre-Processor...");

        // Downscale image
        String scaleOptionsString = buildScaleOptionsString(workingImagePlus);
        logService.info("Scaling...");
        IJ.run(workingImagePlus, "Scale...", scaleOptionsString);
        preProcessedImagePlus = IJ.getImage();
        logService.info("Done scaling.");

        logService.info("Pre-Processor finished");
    }

    // Helpers

    /**
     * Helper to build Scale command option string
     * @param ip the ImagePlus to scale
     * @return a string describing the options for the Scale command
     */
    private String buildScaleOptionsString(ImagePlus ip) {
        String optionString = "";
        // Add scale factors and strip leading zeros
        String scaleFactorNoTrailingZeros = Double.toString(scaleFactor).replaceAll("^0+", "");
        optionString +=
            "x=" + scaleFactorNoTrailingZeros +
            " y=" + scaleFactorNoTrailingZeros +
            " z=" + scaleFactorNoTrailingZeros;

        // resulting dimensions
        List<Integer> resizedDimensions = getResizedDimensions(ip, scaleFactor);
        optionString += " width=" + resizedDimensions.get(0);
        optionString += " height=" + resizedDimensions.get(1);
        optionString += " depth=" + resizedDimensions.get(2);

        optionString += " interpolation=Bilinear average process ";

        optionString += "title=" +ip.getTitle()
            .replaceAll("\\.tif$", "-SCALED" + ".tif");
        return optionString;
    }

    /**
     * Helper to get scaled dimensions
     * @param ip ImagePlus to scale
     * @param sf scale factor
     * @return list of scaled dimensions (x, y, z)
     */
    private List<Integer> getResizedDimensions(ImagePlus ip, double sf) {
        int x = (int) Math.round(ip.getWidth() * sf);
        int y = (int) Math.round(ip.getHeight() * sf);
        int z = (int) Math.round(ip.getStackSize() * sf);
        return Arrays.asList(x, y, z);
    }

    // Mutators

    /**
     * Mutator for scale factor
     * @param sf the factor by which to scale image
     */
    public void setScaleFactor(double sf) { scaleFactor = sf; }

    // Accessors

    /**
     * Accessor for scaled ImagePlus
     * @return scaled ImagePlus
     */
    public ImagePlus getPreProcessedImagePlus() { return preProcessedImagePlus; }

}

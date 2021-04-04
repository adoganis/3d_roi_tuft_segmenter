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

import ij.ImagePlus;
import ij.plugin.Scaler;
import net.imagej.ImgPlus;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * The ilastik predictor the OIRTuftSegmentation plugin
 */
public class IlastikPredictor {

    // Private vars

    // Parameters

    // For logging errors
    @Parameter
    private LogService logService;

    /**
     * Constructor
     * @param ctx The SciJava application context
     * @param scaledImage the scaled ImagePlus to segment
     **/
    public IlastikPredictor(Context ctx, ImagePlus scaledImage) {

    }
}

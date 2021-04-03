/*
 * Alexandros Doganis
 * A FIJI plugin to automatically segment 3D ROIs of OIR blood vessel tufting in MicroCT stacks via ilastik
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
 *
 * This program is based on ImageJ's example plugin "Simple Commands"
 * <https://github.com/imagej/tutorials/tree/master/maven-projects/simple-commands>
 */

package uk.ac.crick.bentley;

import io.scif.services.DatasetIOService;

import ij.*;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;
import org.scijava.ui.UIService;
import org.ilastik.ilastik4ij.IlastikOptions;
import org.ilastik.ilastik4ij.IlastikPixelClassificationPrediction;
import mcib3d.*;
import mcib3d.image3d.ImageInt;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;

/**
 * This example illustrates how to create an ImageJ {@link Command} plugin.
 * <p>
 * The code here is a simple Gaussian blur using ImageJ Ops.
 * </p>
 * <p>
 * You should replace the parameter fields with your own inputs and outputs,
 * and replace the {link run} method implementation with your own logic.
 * </p>
 */
@Plugin(type = Command.class, menuPath = "Plugins>OIRTuftSegmentation")
public class OIRTuftSegmentation<T extends RealType<T>> implements Command {
    // Variables

    // Plugin GUI
    private static DialogGUI dialog;

    // Parameters

    // For opening and saving images.
    @Parameter
    private DatasetIOService datasetIOService;

    // For logging errors.
    @Parameter
    private LogService logService;

    @Parameter
    private Context ctx;

    // Input image to open
    @Parameter(label = "Image to load")
    private File imageFile;

    // Working image
    @Parameter(type = ItemIO.OUTPUT)
    private Dataset image;

    @Override
    public void run() {
        try {
            image = datasetIOService.open(imageFile.getAbsolutePath());

            SwingUtilities.invokeLater(() -> {
                if (dialog == null) { dialog = new DialogGUI(ctx, image); }
                dialog.setVisible(true);
            });

        }
        catch (final IOException exc) {
            // Use the LogService to report the error.
            logService.error(exc);
        }
    }

    /**
     * Main function for dev purposes
     *
     * @param args needed but ultimately ignored
     * @throws Exception some exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

//        // ask the user for a file to open
//        final File file = ij.ui().chooseFile(null, "open");
//
//        if (file != null) {
//            // load the dataset
//            final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());
//
//            // show the image
//            ij.ui().show(dataset);
//
//            // invoke the plugin
//            ij.command().run(OIRTuftSegmentation.class, true);
//        }
    }

}

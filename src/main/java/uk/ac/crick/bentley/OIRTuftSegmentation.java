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

import ij.IJ;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 * The controller of the OIRTuftSegmentation plugin
 * @author Alexandros Doganis
 */
@Plugin(type = Command.class, menuPath = "Plugins>OIR Tuft Segmentation")
public class OIRTuftSegmentation<T extends RealType<T>> implements Command {

    // Classes to control
    private DialogGUI<T> dialog;                    // Plugin GUI
    private PreProcessor<T> preProcessor;           // Pre-Processor
    private IlastikPredictor ilastikPredictor;      // ilastik Predictor
    private PostProcessor postProcessor;            // Post-Processor

    // Private vars
    private ImgPlus<T> imp;
    private File saveDir;
    private File ilastikProjectFile;
    private String thresholdMethodName;
    private double scaleFactor;
    private boolean removeOutliers;

    // Parameters

    @Parameter
    private DatasetService datasetService;

    // For logging errors.
    @Parameter
    private LogService logService;

    // Our SciJava context
    @Parameter
    private Context ctx;

    // Already opened image
    @Parameter
    private Dataset currentData;

    // Working image
    @Parameter(type = ItemIO.OUTPUT)
    private Dataset image;

    @Override
    public void run() {
        logService = ctx.getService(LogService.class);
        logService.info("Starting OIRTuftSegmentation...");

        // If no open image, prompt user to open image to start workflow
        final ImgPlus<T> openImage = ImgPlus.wrap((Img<T>) currentData.getImgPlus());

        // Start GUI
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialog = new DialogGUI<T>(ctx, openImage);
                dialog.setVisible(true);
            });
        }
        catch (InterruptedException | InvocationTargetException e) { logService.error(e); }


        // Wait for user to input to GUI and confirm run or exit
        // Slightly poor form but much more elegant than thread synching
        while(!dialog.runSegmentation() && !dialog.isQueuedForDisposal()) {
            try { Thread.sleep(200); }
            catch (InterruptedException ex) { logService.error(ex); }
        }
        // User exited window, kill self
        if(dialog.isQueuedForDisposal()) {
            logService.info("Exiting OIRTuftSegmentation");
            return;
        }

        // Set global vars
        setSelectedImgPlus(dialog.getSelectedImgPlus());
        setSaveDir(dialog.getSaveDir());
        setIlastikProjectFile(dialog.getIlastikProjectFile());
        setThresholdMethodName(dialog.getThresholdMethodName());
        setScaleFactor(dialog.getScaleFactor());
        setRemoveOutliers(dialog.getRemoveOutliers());

        // Apply Pre-Processing
        preProcessor = new PreProcessor<T>(ctx, imp);
        preProcessor.setScaleFactor(scaleFactor);
        preProcessor.run();

        // Run ilastik Prediction
        ilastikPredictor = new IlastikPredictor(ctx, preProcessor.getPreProcessedImagePlus());
        ilastikPredictor.setIlastikProjectFile(ilastikProjectFile);
        ilastikPredictor.run();

        // Apply Post-Processing
        postProcessor = new PostProcessor(ctx, ilastikPredictor.getPredictionImagePlus(), imp.getName());
        postProcessor.setThresholdMethodName(thresholdMethodName);
        postProcessor.setRemoveOutliers(removeOutliers);
        postProcessor.run();

        // Save post-processed output for user
        IJ.save(postProcessor.getPostProcessedImage(), saveDir.getAbsolutePath());

        // Open in 3D Manager - must use macro as 3D suite is external plugin
        IJ.runMacro("run(\"3D Manager\");\n" +
                "Ext.Manager3D_Segment(128, 255);\n" +
                "wait(5000);\n" +
                "Ext.Manager3D_AddImage();");

        logService.info("OIRTuftSegmentation finished");
    }

    // Mutators

    /**
     * Mutator for selected image
     * @param ip ImgPlus<T> to set global var
     */
    public void setSelectedImgPlus(ImgPlus<T> ip) { imp = ip; }

    /**
     * Mutator for save directory
     * @param sd File to save output to
     */
    public void setSaveDir(File sd) { saveDir = sd; }

    /**
     * Mutator for scale factor
     * @param sf double scale factor to set global var
     */
    public void setScaleFactor(double sf) { scaleFactor = sf; }

    /**
     * Mutator for ilastik project file
     * @param ipf File trained ilastik project to set global var
     */
    public void setIlastikProjectFile(File ipf) { ilastikProjectFile = ipf; }

    /**
     * Mutator for auto-threshold method type
     * @param name String auto-threshold method name to apply
     */
    public void setThresholdMethodName(String name) { thresholdMethodName = name; }

    /**
     * Mutator for remove outliers operation
     * @param ro boolean whether to remove outliers to set global var
     */
    public void setRemoveOutliers(boolean ro) { removeOutliers = ro; }

    /**
     * Main function for dev purposes
     * @param args needed but ultimately ignored
     * @throws Exception some exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // ask the user for a file to open
        final File file = ij.ui().chooseFile(null, "open");

        if (file != null) {
            // load the dataset
            final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());

            // show the image
            ij.ui().show(dataset);

            // invoke the plugin
            ij.command().run(OIRTuftSegmentation.class, true);
        }
    }

}

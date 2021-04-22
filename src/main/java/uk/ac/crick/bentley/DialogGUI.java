/*
 * Alexandros Doganis
 * The GUI of the OIRTuftSegmentation plugin
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

import net.imagej.ImgPlus;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.DialogPrompt.Result;
import org.scijava.ui.UIService;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * The GUI of the OIRTuftSegmentation plugin
 * @author alexandrosdoganis
 */
public class DialogGUI<T extends RealType<T>> extends JFrame {
    // private vars
    private JPanel mainPanel;
    private JLabel preProcessLabel;
    private JLabel predictionLabel;
    private JLabel postProcessLabel;
    private JPanel preProcessPanel;
    private JLabel scaleLabel;
    private JTextField scaleValue;
    private JPanel predictionPanel;
    private JPanel postProcessPanel;
    private JLabel thresholdMethod;
    private JComboBox thresholdComboBox;
    private JLabel denoiseLabel;
    private JCheckBox removeNoise;
    private JButton runButton;
    private JLabel ilastikLabel;
    private JTextField ilastikPathText;
    private JButton ilastikBrowseButton;
    private JLabel targetImageLabel;
    private JComboBox imageComboBox;
    private JPanel imagePanel;
    private JLabel saveDirLabel;
    private JTextField saveDirPathText;
    private JButton saveDirBrosweButton;
    private JLabel lutLabel;
    private JCheckBox invertLUT;

    private Context context;
    private boolean runSegmentation = false;    // Segmentation confirmation flag
    private boolean queuedForDisposal = false;   // Disposal queue flag

    private ImgPlus<T> imp;
    private File saveDir;
    private double scaleFactor;
    private File ilastikProjectFile;
    private String thresholdMethodName;
    private boolean invertPredictionLUT;
    private boolean removeOutliers;

    // For logging errors
    @Parameter
    private final LogService logService;


    /**
     * Constructor
     * @param ctx The SciJava application context
     * @param img the current open ImgPlus to segment
     **/
    public DialogGUI(final Context ctx, ImgPlus<T> img) {
        context = ctx;
        logService = context.getService(LogService.class);
        logService.info("Initializing GUI...");

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        setTitle("OIR Tuft Segmenter");
        String imgName = img.getName();
        if(!imgName.isEmpty()) { imageComboBox.addItem(imgName); }
        imageComboBox.setSelectedIndex(imageComboBox.getItemCount()-1);     // Last added item is our image, select it
        saveDirBrosweButton.addActionListener(e -> {
            chooseAndGetFile("Save image to directory", saveDirPathText, true);
        });
        ilastikBrowseButton.addActionListener(e -> {
            chooseAndGetFile("Trained ilastik project file", ilastikPathText, false);
        });
        runButton.addActionListener(e -> {
            // validate form
            if(!dataIsValid()) { return; }

            // get confirmation
            if(getRunConfirmation()) {
                // run
                updatePublicVars(img);
                runSegmentation = true;

                // Close window
                setVisible(false);
            } else { logService.info("Run command confirmation cancelled by user."); }
        });
        this.addWindowListener(new WindowAdapter(){
            // User closed window, signal to controller we are queued for disposal to avoid busy-waiting
            public void windowClosing(WindowEvent e){ queuedForDisposal = true; }
        });

        logService.info("Done.");

    }

    /**
     * Helper to prompt user to choose file/directory path and update given text field
     * @param title String title of file chooser
     * @param textField JTextField to update with chosen path
     * @param directoriesOnly boolean if JFileChooser
     */
    private void chooseAndGetFile(String title, JTextField textField, boolean directoriesOnly) {
        // open file picker
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        if(directoriesOnly) { fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); }

        // update text field with chosen path
        int returnVal = fc.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());

            // log file selection
            logService.info("File selected: " + selectedFile.getName() + ".");
        } else { logService.info("File selection cancelled by user."); }
    }

    /**
     * Public var update helper
     * @param ip dataset selected by user
     */
    private void updatePublicVars(ImgPlus<T> ip) {
        imp = ip;
        saveDir = new File(saveDirPathText.getText());
        scaleFactor = Double.parseDouble(scaleValue.getText());
        ilastikProjectFile = new File(ilastikPathText.getText());
        thresholdMethodName = thresholdComboBox.getSelectedItem().toString();
        invertPredictionLUT = invertLUT.isSelected();
        removeOutliers = removeNoise.isSelected();
    }

    /**
     * Data validation helper
     * @return boolean describing if the inputs are valid
     */
    private boolean dataIsValid() {
        logService.info("Validating inputs...");
        boolean dataIsValid = true;
        String validationMsg = "Invalid inputs:\n";

        // Validate selected image
        Object selectedImage = imageComboBox.getSelectedItem();
        if(selectedImage == null || selectedImage.toString().equals("Select image to segment...")) {
            // No image selected
            dataIsValid = false;
            logService.info("Invalid input: no image selected to segment");
            validationMsg += "You must select an image to segment.\n";
        } else { logService.info(targetImageLabel.getText() + " " + selectedImage.toString()); }

        // Validate save directory
        File saveDir = new File(saveDirPathText.getText());
        if(saveDir.exists() && saveDir.isDirectory()) {
            logService.info(saveDirLabel.getText() + " " + saveDir.getName());
        } else {
            // File doesnt exist or is not a directory
           String saveDirInfo = saveDirLabel.getText() + " " + saveDirPathText.getText();
            logService.info("Invalid input: '" + saveDirInfo + "' is not a directory");
            validationMsg += saveDirInfo + " must be an existing directory.\n";
        }

        // Validate scale factor
        try {
            double result = Double.parseDouble(scaleValue.getText());
            logService.info(scaleLabel.getText() + " " + result);
        }
        catch (NumberFormatException nfe) {
            // invalid scale factor
            dataIsValid = false;
            String scaleValueInfo = scaleLabel.getText() + " " + scaleValue.getText();
            logService.info("Invalid input: '" + scaleValueInfo + "' is not a Double");
            validationMsg += scaleValueInfo + " must be a Double.\n";
        }

        // Validate ilastik file
        String ilastikInfo = ilastikLabel.getText() + " " + ilastikPathText.getText();
        String ilastikFileTypePattern = ".*\\.ilp$";
        if(ilastikPathText.getText().isEmpty()) {
            // empty ilastik project field
            dataIsValid = false;
            logService.info("Invalid input: '" + ilastikInfo + "' is empty");
            validationMsg += ilastikInfo + "must not be empty.\n";
        } else if(!ilastikPathText.getText().matches(ilastikFileTypePattern)) {
            // not ilastik file
            dataIsValid = false;
            logService.info("Invalid input: '" + ilastikInfo + "' is not an ilastik project file");
            validationMsg += ilastikInfo + " must be an ilastik project file \".ilp\".\n";
        } else {
            // check if file exists
            File ilastikFile = new File(ilastikPathText.getText());
            if(!ilastikFile.exists() || !ilastikFile.isFile()) {
                dataIsValid = false;
                logService.info("Invalid input: '" + ilastikInfo + " is not a file");
                validationMsg += ilastikInfo + " must be an existing file.\n";
            } else { logService.info(ilastikInfo); }
        }

        if(!dataIsValid) {
            final MessageType messageType = MessageType.WARNING_MESSAGE;
            final OptionType optionType = OptionType.DEFAULT_OPTION;

            // Prompt for confirmation.
            final UIService uiService = context.getService(UIService.class);
            final Result result =
                uiService.showDialog(validationMsg, "Validation failed!", messageType, optionType);
        }

        logService.info("Done.");
        return dataIsValid;
    }

    /**
     * Confirmation helper
     * <p> Gets confirmation if the user wishes to run the segmentation </p>
     * @return boolean confirming that user wishes to run segmentation
     */
    private boolean getRunConfirmation() {
        final String explanation = "You are about to run the segmentation workflow.\nThis can take a while.";
        final String confirmation = "Are you sure you want to run?";
        final String message = explanation + "\n" + confirmation;

        final MessageType messageType = MessageType.WARNING_MESSAGE;
        final OptionType optionType = OptionType.YES_NO_OPTION;

        // Prompt for confirmation.
        final UIService uiService = context.getService(UIService.class);
        final Result result =
                uiService.showDialog(message, "Confirmation", messageType, optionType);

        // Cancel the command execution if the user does not agree.
        return result == Result.YES_OPTION;
    }

    // Accessors

    /**
     * Accessor for run ocnfirmation flag
     * @return boolean describing if segmentation command confirmed by user
     */
    public boolean runSegmentation() { return runSegmentation; }

    /**
     * Accessor for run disposal queue flag
     * @return boolean describing if window is queued for disposal
     */
    public boolean isQueuedForDisposal() { return queuedForDisposal; }

    /**
     * Accessor for selected image
     * @return ImagePlus selected by user
     */
    public ImgPlus<T> getSelectedImgPlus() { return imp; }

    /**
     * Accessor for save directory
     * @return ImagePlus selected by user
     */
    public File getSaveDir() { return saveDir; }

    /**
     * Accessor for scale factor
     * @return double scale factor provided by user
     */
    public double getScaleFactor() { return scaleFactor; }

    /**
     * Accessor for ilastik project file
     * @return ilastik project file selected by user
     */
    public File getIlastikProjectFile() { return ilastikProjectFile; }

    /**
     * Accessor for auto-threshold method type
     * @return String name of auto-threshold method
     */
    public String getThresholdMethodName() { return thresholdMethodName; }

    /**
     * Accessor for invert LUT operation
     * @return boolean indicating if user wishes to invert the LUT in post-processing
     */
    public boolean getInvertLUT() { return invertPredictionLUT; }

    /**
     * Accessor for remove outliers operation
     * @return boolean indicating if user wishes to remove outliers
     */
    public boolean getRemoveOutliers() { return removeOutliers; }
}

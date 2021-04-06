3D Tuft Segmenter
==========================
This is FIJI plugin to automatically segment 3D ROIs of OIR blood vessel tufting in MicroCT stacks via ilastik.
It was built by Alexandros Doganis for the Cellular Adaptive Behaviour Lab at the Francis Crick Institute to supplement their research on OIR.  
It is based on [ImageJ's Example Command](https://github.com/imagej/example-imagej-command).

Requirements
------------

* [Fiji](https://imagej.net/Fiji/Downloads) build of ImageJ 2.1.0 or later
* [ilastik](https://www.ilastik.org/download.html) 1.3.3 or later

Development
-----------

The Mavenized plugin is intended to be developed within an IDE. 

* In [Eclipse](http://eclipse.org), for example, it is as simple as
  _File &#8250; Import... &#8250; Existing Maven Project_.

* In [NetBeans](http://netbeans.org), it is even simpler:
  _File &#8250; Open Project_.

* The same works in [IntelliJ](http://jetbrains.net).

* If [jEdit](http://jedit.org) is your preferred IDE, you will need the
  [Maven Plugin](http://plugins.jedit.org/plugins/?MavenPlugin).

Die-hard command-line developers can use Maven directly by calling `mvn`
in the project root.

Installation
--------------

However you build the project, in the end you will have the `.jar` file
(called *artifact* in Maven speak) in the `target/` subdirectory.

To copy the artifact into the correct place, you can call
`mvn -Dscijava.app.directory="/path/to/Fiji.app/plugins"`.
This will not only copy your artifact, but also all the dependencies.
Restart your ImageJ or call *Help &#8250; Refresh Menus* to see your
plugin in the menus.

Developing plugins in an IDE is convenient, especially for debugging. To
that end, the plugin contains a `main` method which sets the `plugins.dir`
system property (so that the plugin is added to the Plugins menu), starts
ImageJ, loads an image and runs the plugin. See also
[this page](https://imagej.net/Debugging#Debugging_plugins_in_an_IDE_.28Netbeans.2C_IntelliJ.2C_Eclipse.2C_etc.29)
for information how ImageJ makes it easier to debug in IDEs.

As this project is under the [GNU](https://www.gnu.org/licenses/gpl-3.0.en.html) license, it is in the public domain.

Running
-------

1. Open Fiji and open the target image to segment.
2. Run the plugin at `Plugins > OIR Tuft Segmentation`.
3. Input desired paramters to the GUI and confirm `Run segmentation`.  
   The trained ilastik project file can be found at `/path/to/3d_roi_tuft_segmenter/ilastik/oir-tuft-pixel-classification-minimal_seg_export`
5. When the workflow completes, confirm the objects in 3D Manager.


The Workflow
=============

Pre-Processing
------------------

1. Downscale the open image by the specified user amount to decrease memory usage and speed up workflow.

Prediction
--------------

2. Run [ilastik](https://www.ilastik.org/index.html) Pixel Classification Prediciton on the downscaled image.

Post-Processing
-------------------

3. Apply the user specified auto-threshold method to resulting prediction.
   This normalizes the ilastik LUT output for further processing.

4. If indicated by user, denoise the output with Fiji's "Remove Outliers" process.

Segmentation
-------------

5. Using the external plugin "3D Manager" the (part of [3D ImageJ Suite](https://imagejdocu.tudor.lu/plugin/stacks/3d_ij_suite/start)), label the proessed prediction objects.

6. Add the labelled image to 3D Manager for user inspection.

------------------

Bugs
----
* Auto-thresholding produces poor output when run automatically. Inline with GIGO principles, rest of workflow produces nonsense. - Currently prompting user to avoid this
* File does not auto-save at end of workflow.

------------------

**Acknowledgements**

Huge thanks to Dr. Bentley and the whole CAB lab for their support and inputs.

#CVImageProcessor
================

Source code for the DHBW Computer Vision lecture

## Features

* read any PGM image
* invert color
* blur image
* save modified images
* display histograms

## Requirements

* git[http://git-scm.com/downloads]
* maven2[http://maven.apache.org/download.cgi]
* JDK 1.6[http://www.oracle.com/technetwork/java/javase/downloads/index.html]
* internet connection during execution to display histograms

Installing packages from your package manager is highly recommended!

## Build
1. Install all requirements
1. Open a terminal
	1. Clone the git repo with git clone https://github.com/thuringia/Car2Square.git
	1. Install the project's dependencies with mvn install
	1. Create a runnable jar with mvn package
	1. Create Javadoc with mvn javadoc:javadoc
	
## Run CVImageProcessor
1. Navigate to PROJECT_CLONE_DIR/target
1. Launch CVImageProcessor by double-clicking CVImageProcessor-1.0-SNAPSHOT.jar
	1. Alternatively issue java -jar CVImageProcessor-1.0-SNAPSHOT.jar from the terminal
	
## Use CVImageProcessor
* Before doing anything you need to open an image.
* If an image was opened, it is displayed in the top left
	* Its metadata is visible to the right in the 'Metadata' tab
	* The histograms are available from the 'Histogram' tab
* To manipulate go to the 'Image' tab at the bottom
	* Invert your image by clicking 'Invert'
	* Blur the image by clicking 'Blur' with adjustable kernel sizes using the slider
	* Switch between the original, inverted and blurred image by using the 'View' tab to the right
* Save an inverted or blurred image by clicking the appropriate buttons in the 'File' tab at the bottom

#Libraries
* Apache Commons IO[http://commons.apache.org/proper/commons-io/]
* Apache Imaging[http://commons.apache.org/proper/commons-imaging/index.html]
* Apache log4j[http://logging.apache.org/log4j/1.2/]
* Chart4j[https://code.google.com/p/charts4j/]
* Jerry Huxtable's 'Blurring for Beginners'[http://www.jhlabs.com/ip/blurring.html]
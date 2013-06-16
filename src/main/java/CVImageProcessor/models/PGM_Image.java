package CVImageProcessor.models;

import com.googlecode.charts4j.*;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.jhlabs.image.GaussianFilter;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static java.lang.Math.PI;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Robert Wawrzyniak
 *         Date: 6/1/13
 *         Time: 6:39 PM
 */
public class PGM_Image implements Cloneable {

    private final Logger logger = Logger.getLogger(PGM_Image.class);

    public int width;
    public int height;
    public int depth;
    private String comment = "";
    public String magicNumber;

    public String fileName;
    private final long fileSize;

    private int[][] data;

    public final File fileRef;

    public BufferedImage bufferedImage;

    /**
     * Instantiate a new PGM_Image object
     *
     * @param ref the File to read
     */
    public PGM_Image(File ref) {
        this.fileRef = ref;

        this.fileName = fileRef.getName();
        this.fileSize = fileRef.length();

        // get the header information
        this.readHeaderFromInputStream();

        // create BufferedImage from File
        try {
            this.bufferedImage = Imaging.getBufferedImage(fileRef);

            // set array size
            this.data = new int[width][height];

            // copy the individual pixels
            for (int row = 0; row < width; row++) {
                for (int col = 0; col < height; col++) {
                    int val = (bufferedImage.getRGB(row, col) & 0xff);
                    this.data[row][col] = val;
                }
            }
            logger.debug("copied pixel values");
        } catch (ImageReadException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Reads the PGM header from the File specified on instantiation
     */
    private void readHeaderFromInputStream() {
        try {
            logger.debug("opening image");
            InputStream f = FileUtils.openInputStream(fileRef);
            BufferedReader d = new BufferedReader(new InputStreamReader(f));

            this.magicNumber = d.readLine();    // first line contains P2 or P5
            logger.debug("found magic number: " + magicNumber);

            String line = d.readLine();     // second line contains height and width
            while (line.startsWith("#")) {
                line = d.readLine();
                this.comment = line;
                logger.debug("found comment: \n" + comment);
            }

            Scanner s = new Scanner(line);

            this.width = s.nextInt();
            this.height = s.nextInt();
            logger.debug("found width/height: " + width + "/" + height);

            line = d.readLine();// third line contains maxVal
            s = new Scanner(line);
            this.depth = s.nextInt();

            s.close();
            d.close();
            f.close();

        } catch (IOException e) {
            logger.error("error while reading header\n", e);
        }
    }

    /**
     * Returns a String representation for this object
     *
     * @return "FILENAME (size: FILESIZE)"
     */
    @Override
    public String toString() {
        return String.format("%s (size: %d)", this.fileName, this.fileSize);
    }

    /**
     * Calculates the values for the Histogram
     *
     * @return HashMap<Integer, Integer> as PIXEL_VALUE, COUNT
     */
    private HashMap<Integer, Integer> calculateHistogramData() {
        logger.debug("creating histogram data");
        HashMap<Integer, Integer> histData = new HashMap<Integer, Integer>();

        // prepopulate the HashMap
        for (int i = 0; i < depth + 1; i++) {
            histData.put(i, 0);
        }
        logger.debug("pre-populated the HasMap");

        int pixel, val, row, col;
        try {
            for (row = 0; row < width; row++) {
                for (col = 0; col < height; col++) {
                    pixel = data[row][col];
                    val = histData.get(pixel);
                    val++;
                    histData.put(pixel, val);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        logger.debug("created the HashMap");

        return histData;
    }

    /**
     * Downloads a {@link BarChart} showing the color distribution using the Google Chart API
     *
     * @param floored boolean indicating whether to use floored scaling or not
     * @return a {@link BufferedImage} with the histogram
     */
    public BufferedImage getHistogram(boolean floored) {
        // get the data
        HashMap<Integer, Integer> histData = calculateHistogramData();

        com.googlecode.charts4j.Color black = com.googlecode.charts4j.Color.BLACK;

        // create a plot
        Data data;
        if (floored) {
            data = Data.newData(scaleFloor(new ArrayList<Number>(histData.values())));
        } else {
            data = Data.newData(scale(new ArrayList<Number>(histData.values())));
        }
        //
        BarChartPlot plot = Plots.newBarChartPlot(data, black);

        // init the chart
        BarChart chart = GCharts.newBarChart(plot);

        // Defining axis info and styles
        AxisStyle axisStyle = AxisStyle.newAxisStyle(black, 13, AxisTextAlignment.CENTER);
        AxisLabels intensity = AxisLabelsFactory.newAxisLabels("Intensity", 50.0);
        intensity.setAxisStyle(axisStyle);
        AxisLabels value = AxisLabelsFactory.newAxisLabels("Value", 50.0);
        value.setAxisStyle(axisStyle);

        // Adding axis info to chart.
        //chart.addXAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0, 255, 10));
        chart.addYAxisLabels(AxisLabelsFactory.newNumericRangeAxisLabels(0, 100));
        chart.addYAxisLabels(intensity);
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("255", 100));
        chart.addXAxisLabels(value);

        chart.setSize(400, 200);
        chart.setBarWidth(1);
        chart.setSpaceWithinGroupsOfBars(0);
        chart.setSpaceBetweenGroupsOfBars(0);
        chart.setTitle("Histogram", black, 16);
        String url = chart.toURLString();
        logger.debug("chart url:\n" + url);

        try {
            return Imaging.getBufferedImage(new URL(url).openStream());
        } catch (ImageReadException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        // if we get here something went very wrong
        return null;
    }

    /**
     * Scales the data set
     *
     *
     *
     * @param data a {@link java.util.List<? extends Number>} with the values to scale
     * @return double[] with the scaled values
     */
    private double[] scale(final List<? extends Number> data) {
        final double[] d = getDoubleArray(data);
        final double[] scaledData = new double[d.length];
        for (int j = 0; j < d.length; j++) {
            scaledData[j] = ((d[j] - 0.0) / (100.0 - 0.0));
        }
        return scaledData;
    }

    /**
     * Creates a double[] based on a List
     *
     * @param data the data for the array
     * @return double[] with data
     */
    private double[] getDoubleArray(List<? extends Number> data) {
        final double[] d = new double[data.size()];
        for (int i = 0; i < d.length; i++) {
            d[i] = data.get(i).doubleValue();
        }
        return d;
    }

    /**
     * Scales the data set using {@link Math.floor()}
     *
     * @param data a {@link List<? extends Number>} with the values to scale
     * @return double[] with the scaled values
     */
    private double[] scaleFloor(final List<? extends Number> data) {
        final double[] d = getDoubleArray(data);
        final double[] scaledData = new double[d.length];
        for (int j = 0; j < d.length; j++) {
            scaledData[j] = Math.floor(255 * Math.log(d[j] + 1) / (Math.log(Integer.MAX_VALUE)));
        }
        return scaledData;
    }

    /**
     * Creates a shallow copy of this object
     *
     * @return a new {@link PGM_Image}
     * @throws CloneNotSupportedException
     */
    @Override
    protected PGM_Image clone() throws CloneNotSupportedException {
        return new PGM_Image(fileRef);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * invert the image
     *
     * @return a new {@link PGM_Image} containing the inverted image
     */
    public PGM_Image invert() {
        PGM_Image inverted = null;

        try {
            inverted = this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // stop if the cloning failed
        if (inverted == null) return null;

        // change the file name
        inverted.fileName = inverted.fileName.substring(0, inverted.fileName.lastIndexOf(".")) + "_INVERTED" + inverted.fileName.substring(inverted.fileName.lastIndexOf("."));

        // invert the pixels and adjust the buffered image
        for (int row = 0; row < inverted.width; row++) {
            for (int col = 0; col < inverted.height; col++) {
                // invert
                Color oldColor = new Color(inverted.bufferedImage.getRGB(row, col));
                Color newColor = new Color(255 - oldColor.getRed(), 255 - oldColor.getGreen(), 255 - oldColor.getBlue());

                // set the new color in the BufferedImage
                inverted.bufferedImage.setRGB(row, col, newColor.getRGB());

                // adjust the data array
                inverted.data[row][col] = newColor.getRGB() & 0xFF;
            }
        }

        return inverted;
    }

    /**
     * blur this image
     *
     * @param radius blur kernel size
     * @return a new {@link PGM_Image} with the blurred image
     */
    public PGM_Image blur(float radius) {
        PGM_Image blurred = null;

        try {
            blurred = this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // stop if the cloning failed
        if (blurred == null) return null;

        // change the file name
        blurred.fileName = blurred.fileName.substring(0, blurred.fileName.lastIndexOf(".")) + "_BLURRED" + blurred.fileName.substring(blurred.fileName.lastIndexOf("."));

        // blur
        GaussianFilter gaussianFilter = new GaussianFilter(radius);
        gaussianFilter.filter(this.bufferedImage, blurred.bufferedImage);

        // update histogram data
        updateHistogramData(blurred);

        return blurred;
    }

    /**
     * Cycle through all the pixels to genereate the histogramm data
     * @param image the image whose histogram data needs updating
     */
    private void updateHistogramData(PGM_Image image) {
        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {
                image.data[row][col] = image.bufferedImage.getRGB(row, col) & 0xFF;
            }
        }
    }

    /**
     * detect lines in this image
     * based on the openCV Hough transform tutorial {@link http://docs.opencv.org/doc/tutorials/imgproc/imgtrans/hough_lines/hough_lines.html#hough-lines}
     * using the JavaCV wrapper {@link https://code.google.com/p/javacv/}
     * @return new {@link PGM_Image} with detected lines drawn in
     */
    public PGM_Image detectLines(int hough_threshold, int method /*, boolean use_canny, int cv_threshold*/) {
        logger.debug("pre-loading C libraries");
        Loader.load(opencv_objdetect.class);

        logger.debug("starting line detection");

        PGM_Image detectedLines;

        try {
            detectedLines = this.clone();
            logger.debug("cloned image");
        } catch (CloneNotSupportedException e) {
            logger.error("Clone not supported\n", e);

            return null;
        }

        // change the file name
        detectedLines.fileName = detectedLines.fileName.substring(0, detectedLines.fileName.lastIndexOf(".")) + "_DETECTED_LINES" + ".pgm";
        logger.debug("file name changed from: " + this.fileName + "\nto: " + detectedLines.fileName);

        // create image for opencv
        IplImage image = cvLoadImage(this.fileRef.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
        logger.debug("image opened for opencv");

        // Objects allocated with a create*() or clone() factory method are automatically released
        // by the garbage collector, but may still be explicitly released by calling release().
        // You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on objects allocated this way.
        CvMemStorage storage = CvMemStorage.create();
        logger.debug("created opencv memory storage");

        if (image != null) {
            // threshold the image
            //cvThreshold(image, image, threshold, 255, opencv_imgproc.CV_THRESH_BINARY);
            //logger.debug("image thresholded");


            CvSeq lines;

            logger.debug("detecting edges");
            //if(use_canny) {
                cvCanny(image, image, 50, 200, 3);
            /*} else  {
                // Let's find some contours! but first some thresholding...
                cvThreshold(image, image, cv_threshold, 255, CV_THRESH_BINARY);

                // To check if an output argument is null we may call either isNull() or equals(null).
                CvSeq contour = new CvSeq(null);
                cvFindContours(image, storage, contour, Loader.sizeof(CvContour.class),
                        CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
                while (contour != null && !contour.isNull()) {
                    if (contour.elem_size() > 0) {
                        CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
                                storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 0);
                        cvDrawContours(grabbedImage, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
                    }
                    contour = contour.h_next();
                }
            }
            */


            logger.debug("applying hough transformation");
            /*
            public static native CvSeq cvHoughLines2(CvArr image, Pointer line_storage, int method,
            double rho, double theta, int threshold, double param1/*=0*///, double param2/*=0*/);
            lines = cvHoughLines2(image, storage, method, 1, PI/180, hough_threshold, 0, 0);

            logger.debug("detecting and drawing lines");
            for (int i = 0; i < lines.total(); i++) {
                CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(lines, i));

                float rho=point.x();
                float theta=point.y();

                double a = Math.cos((double) theta), b = Math.sin((double) theta);
                double x0 = a * rho, y0 = b * rho;
                CvPoint pt1 = new CvPoint(
                        (int) Math.round(x0 + 1000 * (-b)),
                        (int) Math.round(y0 + 1000 * (a))),
                        pt2 = new CvPoint(
                                (int) Math.round(x0 - 1000 * (-b)),
                                (int) Math.round(y0 - 1000 * (a)));

                logger.debug("line detected with rho=" + rho + " and theta=" + theta);
                cvLine(image, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
            }
        }

        // update BufferedImage
        assert image != null;
        detectedLines.bufferedImage = image.getBufferedImage();

        // update histogram data
        updateHistogramData(detectedLines);

        logger.debug("updated BufferedImage and histogram data -> exiting method");
        return detectedLines;
    }
}

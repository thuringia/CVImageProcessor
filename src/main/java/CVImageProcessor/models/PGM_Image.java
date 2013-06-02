package CVImageProcessor.models;

import com.googlecode.charts4j.*;
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

/**
 * Created with IntelliJ IDEA.
 *
 * @author Robert Wawrzyniak
 *         Date: 6/1/13
 *         Time: 6:39 PM
 */
public class PGM_Image implements Cloneable {

    Logger logger = Logger.getLogger(PGM_Image.class);

    public int width;
    public int height;
    public int depth;
    public String comment = "";
    public String magicNumber;

    public String fileName;
    public long fileSize;

    public int[][] data;

    public File fileRef;

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
            data = Data.newData(scale(new ArrayList<Number>(histData.values()), 0.0, 100.0));
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // if we get here something went very wrong
        return null;
    }

    /**
     * Scales the data set
     *
     * @param data a {@link List<? extends Number>} with the values to scale
     * @param min  the lowest value
     * @param max  the highest value
     * @return double[] with the scaled values
     */
    private double[] scale(final List<? extends Number> data, final double min, final double max) {
        final double[] d = getDoubleArray(data);
        final double[] scaledData = new double[d.length];
        for (int j = 0; j < d.length; j++) {
            scaledData[j] = ((d[j] - min) / (max - min));
        }
        return scaledData;
    }

    /**
     * Creates a double[] based on a List
     *
     * @param data
     * @return
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
}

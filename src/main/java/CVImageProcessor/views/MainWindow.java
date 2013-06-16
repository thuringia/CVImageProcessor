package CVImageProcessor.views;

import CVImageProcessor.Exec;
import CVImageProcessor.models.PGM_Image;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.Imaging;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_PROBABILISTIC;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_STANDARD;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Robert Wawrzyniak
 *         Date: 6/1/13
 *         Time: 6:23 PM
 */
public class MainWindow {
    public JPanel mainPanel;
    private JLabel imageLabel;
    private JTabbedPane dataPanel;
    private JPanel metadataTab;
    private JPanel histogramTab;
    private JTabbedPane menuTabs;
    private JButton openButton;
    private JButton quitButton;
    private JTable metadataTable;
    private JPanel appPanel;
    private JPanel imagePanel;
    private JPanel menuPanel;
    private JLabel histogramLabel;
    private JCheckBox flooredCheckBox;
    private JButton invertButton;
    private JPanel fileTab;
    private JPanel imageTab;
    private JPanel viewPanel;
    private JRadioButton originalImageRadioButton;
    private JRadioButton invertedImageRadioButton;
    private JRadioButton blurredImageRadioButton;
    private JSlider blurSlider;
    private JLabel blurKernelLabel;
    private JButton blurImageButton;
    private JButton saveBlurredImageButton;
    private JButton detectLinesButton;
    private JRadioButton detectedLinesRadioButton;
    private JSlider thresholdSlider;
    private JRadioButton standardHoughRadioButton;
    private JRadioButton probalisticHoughRadioButton;
    private JButton saveImageButton;

    private final ActionListener viewButtonsActionListener;
    private final ActionListener houghMethodActionListener;
    private final PropertyChangeListener propertyChangeListener;

    //private PGM_Image image = null;
    private ImageIcon hist;
    private ImageIcon histFloored;

    //private PGM_Image invertedImage = null;
    private ImageIcon invertedHist;
    private ImageIcon invertedHistFloored;

    //private PGM_Image blurredImage = null;
    private ImageIcon blurredHist;
    private ImageIcon blurredHistFloored;

    //private PGM_Image detectedLinesImage = null;
    private ImageIcon detectedLinesHist;
    private ImageIcon detectedLinesHistFloored;

    private static final String histogramError = "No histogram because of missing internet connection.";

    private static final Logger logger = Logger.getLogger(MainWindow.class);

    private HashMap<IMAGES, PGM_Image> images;

    /**
     * enum used to select and save the images
     */
    private enum IMAGES {STANDARD, BLURRED, INVERTED, LINES};

    public MainWindow() {
        images = new HashMap<IMAGES, PGM_Image>();

        propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                showImage();
                showHistogram();
            }
        };

        viewButtonsActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage();
                showHistogram();
            }
        };

        houghMethodActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getDetectedLines();
            }
        };

        openButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser opener = new JFileChooser();
                opener.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return ((f.isDirectory() || f.getName().endsWith("pgm")));
                    }

                    @Override
                    public String getDescription() {
                        return "Directories and PGMs";
                    }
                });

                if (opener.showDialog(mainPanel, "Open") == JFileChooser.APPROVE_OPTION) {
                    images.put(IMAGES.STANDARD, new PGM_Image(opener.getSelectedFile()));
                }

                if (images.get(IMAGES.STANDARD) != null) {
                    showImage();
                    showMetadata();
                    getHistograms();

                    menuTabs.setEnabledAt(1, true);
                    dataPanel.setEnabledAt(1, true);
                }

                originalImageRadioButton.addActionListener(viewButtonsActionListener);
                invertedImageRadioButton.addActionListener(viewButtonsActionListener);
                blurredImageRadioButton.addActionListener(viewButtonsActionListener);
                detectedLinesRadioButton.addActionListener(viewButtonsActionListener);
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });

        dataPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Exec.getFrame().pack();
            }
        });

        flooredCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                showHistogram();
            }
        });

        invertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                saveImageButton.setEnabled(true);

                PGM_Image invertedImage = images.get(IMAGES.STANDARD).invert();
                images.put(IMAGES.INVERTED, invertedImage);
                try {
                invertedHist = new ImageIcon(invertedImage.getHistogram(false));
                invertedHistFloored = new ImageIcon(invertedImage.getHistogram(true));
                }
                catch (Exception e) {}
                dataPanel.setEnabledAt(2, true);
                invertedImageRadioButton.setEnabled(true);
                invertedImageRadioButton.setSelected(true);

                showImage();
                showHistogram();
            }
        });

        blurImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                saveImageButton.setEnabled(true);
                dataPanel.setEnabledAt(1, true);
                dataPanel.setEnabledAt(2, true);
                blurSlider.setEnabled(true);
                blurredImageRadioButton.setEnabled(true);
                blurredImageRadioButton.setSelected(true);
                getBlurredImage(blurSlider.getValue());

            }
        });

        blurSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                getBlurredImage(blurSlider.getValue());
            }
        });

        detectLinesButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImageButton.setEnabled(true);
                dataPanel.setEnabledAt(1, true);
                dataPanel.setEnabledAt(2, true);
                detectedLinesRadioButton.setEnabled(true);
                detectedLinesRadioButton.setSelected(true);
                thresholdSlider.setEnabled(true);
                standardHoughRadioButton.setEnabled(true);
                probalisticHoughRadioButton.setEnabled(true);

                standardHoughRadioButton.addActionListener(houghMethodActionListener);
                probalisticHoughRadioButton.addActionListener(houghMethodActionListener);

                getDetectedLines();
            }
        });

        thresholdSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                getDetectedLines();
            }
        });
        saveImageButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Object> options = new ArrayList<Object>();

                // check which images are available
                for (PGM_Image img : images.values()) {
                    options.add(img.fileName);
                }


                int file_to_save = JOptionPane.showOptionDialog(
                        Exec.getFrame(),
                        "Please choose the image to save.",
                        "Choose image",
                        JOptionPane.OK_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options.toArray(),
                        options.get(0));

                JFileChooser saver = new JFileChooser(images.get(IMAGES.STANDARD).fileRef.getParentFile());
                saver.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (saver.showSaveDialog(Exec.getFrame()) == JFileChooser.APPROVE_OPTION) {
                    // save to this folder
                    File dest = saver.getSelectedFile();

                    // get the image to save
                    for (PGM_Image img : images.values()) {
                        if (options.get(file_to_save).equals(img.fileName)) {
                            dest = new File(dest.getAbsolutePath() + File.separator + img.fileName);
                            try {
                                Imaging.writeImage(img.bufferedImage, dest, ImageFormat.IMAGE_FORMAT_PGM, null);
                            } catch (Exception ex) {
                                logger.error("error saving " + img.fileName + "\n", ex);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * helper method used to detect lines in an image
     * using a {@link SwingWorker} for concurrency
     */
    private void getDetectedLines() {
        SwingWorker<PGM_Image, Void> worker = new SwingWorker<PGM_Image, Void>() {
            @Override
            protected PGM_Image doInBackground() throws Exception {
                int hough_method = standardHoughRadioButton.isSelected() ? CV_HOUGH_STANDARD : CV_HOUGH_PROBABILISTIC;
                int hough_treshold = thresholdSlider.getValue();
                return images.get(IMAGES.STANDARD).detectLines(hough_treshold, hough_method);  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected void done() {
                try {
                    PGM_Image detectedLinesImage = get();
                    images.put(IMAGES.LINES, detectedLinesImage);
                    detectedLinesHist = new ImageIcon(detectedLinesImage.getHistogram(false));
                    detectedLinesHistFloored = new ImageIcon(detectedLinesImage.getHistogram(true));

                    if (detectedLinesHist == null) histogramLabel.setText(histogramError);

                    showImage();
                    showHistogram();
                    firePropertyChange("done", false, true);
                } catch (Exception e) {
                    logger.error("concurrency issue @ line detection worker:\n", e);
                }
            }
        };

        worker.addPropertyChangeListener(propertyChangeListener);
        worker.execute();
    }

    /**
     * helper method to blur a image using a {@link SwingWorker} for concurrency
     * @param radius the blur radius (kernel)
     */
    private void getBlurredImage(final int radius) {
        SwingWorker<PGM_Image, Void> worker = new SwingWorker<PGM_Image, Void>() {
            @Override
            protected PGM_Image doInBackground() throws Exception {
                return images.get(IMAGES.STANDARD).blur(radius);
            }

            /**
             * Executed on the <i>Event Dispatch Thread</i> after the {@code doInBackground}
             * method is finished. The default
             * implementation does nothing. Subclasses may override this method to
             * perform completion actions on the <i>Event Dispatch Thread</i>. Note
             * that you can query status inside the implementation of this method to
             * determine the result of this task or whether this task has been cancelled.
             *
             * @see #doInBackground
             * @see #isCancelled()
             * @see #get
             */
            @Override
            protected void done() {
                try {
                    PGM_Image blurredImage = get();
                    images.put(IMAGES.BLURRED, blurredImage);
                    blurredHist = new ImageIcon(blurredImage.getHistogram(false));
                    blurredHistFloored = new ImageIcon(blurredImage.getHistogram(true));

                    if (blurredHist == null) histogramLabel.setText(histogramError);

                    showImage();
                    showHistogram();
                    firePropertyChange("done", false, true);
                } catch (Exception e) {
                    logger.error("concurrency issue @ blur worker:\n", e);
                }
            }
        };

        worker.addPropertyChangeListener(propertyChangeListener);
        worker.execute();
    }

    /**
     * helper method that creates the histograms
     */
    private void getHistograms() {
        histogramLabel.setText("");

        try {
        hist = new ImageIcon(images.get(IMAGES.STANDARD).getHistogram(false));
        histFloored = new ImageIcon(images.get(IMAGES.STANDARD).getHistogram(true));
        }
        catch (Exception e) {

        }
        if (hist == null || histFloored == null) histogramLabel.setText(histogramError);

        showHistogram();
    }

    /**
     * helper method to determine which histogram to show
     */
    private void showHistogram() {
        try {
            if (originalImageRadioButton.isSelected()) {
                if (flooredCheckBox.isSelected()) {
                    histogramLabel.setIcon(histFloored);
                } else {
                    histogramLabel.setIcon(hist);
                }
            } else if (invertedImageRadioButton.isSelected()) {
                if (flooredCheckBox.isSelected()) {
                    histogramLabel.setIcon(invertedHistFloored);
                } else {
                    histogramLabel.setIcon(invertedHist);
                }
            } else if (blurredImageRadioButton.isSelected()) {
                if (flooredCheckBox.isSelected()) {
                    histogramLabel.setIcon(blurredHistFloored);
                } else {
                    histogramLabel.setIcon(blurredHist);
                }
            } else {
                if (flooredCheckBox.isSelected()) {
                    histogramLabel.setIcon(detectedLinesHistFloored);
                } else {
                    histogramLabel.setIcon(detectedLinesHist);
                }
            }
        } catch (Exception e) {
            // catch null pointer from URL
            // no reaction necessary
        }

        Exec.getFrame().pack();
    }

    /**
     * helper method that determinbes which image to show
     */
    private void showImage() {
        imageLabel.setText("");
        if (originalImageRadioButton.isSelected()) {
            imageLabel.setIcon(new ImageIcon(images.get(IMAGES.STANDARD).bufferedImage));
        } else if (invertedImageRadioButton.isSelected()) {
            imageLabel.setIcon(new ImageIcon(images.get(IMAGES.INVERTED).bufferedImage));
        } else if (blurredImageRadioButton.isSelected()) {
            imageLabel.setIcon(new ImageIcon(images.get(IMAGES.BLURRED).bufferedImage));
        } else {
            imageLabel.setIcon(new ImageIcon(images.get(IMAGES.LINES).bufferedImage));
        }

        Exec.getFrame().pack();
    }

    /**
     * helper method loading the image's metadata into the GUI's {@link JTable}
     */
    private void showMetadata() {
        PGM_Image image = images.get(IMAGES.STANDARD);
        DefaultTableModel model = new DefaultTableModel(6, 2);

        Object[] columnTitles = new Object[]{"Property", "Value"};
        model.setColumnIdentifiers(columnTitles);

        String[] properties = new String[]{"File Name:", "Magic Number:", "Width:", "Height:", "Depth:", "Size:"};
        String[] values = new String[]{image.fileName, image.magicNumber, String.valueOf(image.width), String.valueOf(image.height), String.valueOf(image.depth), String.valueOf(image.fileRef.length())};

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(properties[i], i, 0);
            model.setValueAt(values[i], i, 1);
        }

        metadataTable.setModel(model);
    }

}

package CVImageProcessor.views;

import CVImageProcessor.Exec;
import CVImageProcessor.models.PGM_Image;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

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
    private JButton saveInvertedImageButton;
    private JButton quitButton;
    private JTable metadataTable;
    private JPanel appPanel;
    private JPanel imagePanel;
    private JPanel menuPanel;
    private JLabel histogramLabel;
    private JCheckBox flooredCheckBox;
    private JButton invertButton;
    private JCheckBox showOriginalCheckBox;
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
    private JCheckBox blurImageCheckBox;

    private ActionListener viewButtonsActionListener;

    private PGM_Image image = null;
    private ImageIcon hist;
    private ImageIcon histFloored;

    private PGM_Image invertedImage = null;
    private ImageIcon invertedHist;
    private ImageIcon invertedHistFloored;

    private PGM_Image blurredImage = null;
    private ImageIcon blurredHist;
    private ImageIcon blurredHistFloored;

    public MainWindow() {
        viewButtonsActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showImage();
                showHistogram();
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
                        return ((f.isDirectory() || f.getName().endsWith("pgm") ? true : false));  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public String getDescription() {
                        return "Directories and PGMs";  //To change body of implemented methods use File | Settings | File Templates.
                    }
                });

                if (opener.showDialog(mainPanel, "Open") == JFileChooser.APPROVE_OPTION) {
                    image = new PGM_Image(opener.getSelectedFile());
                }

                if (image != null) {
                    showImage();
                    showMetadata();
                    getHistograms();

                    menuTabs.setEnabledAt(1, true);
                }

                originalImageRadioButton.addActionListener(viewButtonsActionListener);
                invertedImageRadioButton.addActionListener(viewButtonsActionListener);
                blurredImageRadioButton.addActionListener(viewButtonsActionListener);
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
                saveInvertedImageButton.setEnabled(true);

                invertedImage = image.invert();
                invertedHist = new ImageIcon(invertedImage.getHistogram(false));
                invertedHistFloored = new ImageIcon(invertedImage.getHistogram(true));

                dataPanel.setEnabledAt(2, true);
                invertedImageRadioButton.setEnabled(true);
                invertedImageRadioButton.setSelected(true);

                showImage();
                showHistogram();
            }
        });

        saveInvertedImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File invertedFile = new File(invertedImage.fileRef.getParent() + File.separator + invertedImage.fileName);
                try {
                    if (invertedFile.exists()) FileUtils.deleteQuietly(invertedFile);
                    Imaging.writeImage(invertedImage.bufferedImage, invertedFile, ImageFormat.IMAGE_FORMAT_PGM, null);
                } catch (ImageWriteException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        blurImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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
        saveBlurredImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File blurredFile = new File(blurredImage.fileRef.getParent() + File.separator + blurredImage.fileName);
                try {
                    if (blurredFile.exists()) FileUtils.deleteQuietly(blurredFile);
                    Imaging.writeImage(blurredImage.bufferedImage, blurredFile, ImageFormat.IMAGE_FORMAT_PGM, null);
                } catch (ImageWriteException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    private void getBlurredImage(int radius) {
        blurredImage = image.blur(Float.valueOf(radius));
        blurredHist = new ImageIcon(blurredImage.getHistogram(false));
        blurredHistFloored = new ImageIcon(blurredImage.getHistogram(true));

        showImage();
        showHistogram();
    }

    private void getHistograms() {
        histogramLabel.setText("");

        hist = new ImageIcon(image.getHistogram(false));
        histFloored = new ImageIcon(image.getHistogram(true));

        showHistogram();
    }

    private void showHistogram() {
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
        } else {
            if (flooredCheckBox.isSelected()) {
                histogramLabel.setIcon(blurredHistFloored);
            } else {
                histogramLabel.setIcon(blurredHist);
            }
        }

        Exec.getFrame().pack();
    }

    private void showImage() {
        imageLabel.setText("");
        if (originalImageRadioButton.isSelected()) {
            imageLabel.setIcon(new ImageIcon(image.bufferedImage));
        } else if (invertedImageRadioButton.isSelected()) {
            imageLabel.setIcon(new ImageIcon(invertedImage.bufferedImage));
        } else {
            imageLabel.setIcon(new ImageIcon(blurredImage.bufferedImage));
        }

        Exec.getFrame().pack();
    }

    private void showMetadata() {
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

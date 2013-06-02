package CVImageProcessor.views;

import CVImageProcessor.Exec;
import CVImageProcessor.models.PGM_Image;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
    private JButton saveButton;
    private JButton quitButton;
    private JTable metadataTable;
    private JPanel appPanel;
    private JPanel imagePanel;
    private JPanel menuPanel;
    private JLabel histogramLabel;
    private JCheckBox flooredCheckBox;

    private PGM_Image image = null;
    private ImageIcon hist;
    private ImageIcon flooredHist;

    public MainWindow() {
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
                }
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
    }

    private void getHistograms() {
        histogramLabel.setText("");

        hist = new ImageIcon(image.getHistogram(false));
        flooredHist = new ImageIcon(image.getHistogram(true));

        showHistogram();
    }

    private void showHistogram() {
        if (flooredCheckBox.getModel().isSelected()) {
            histogramLabel.setIcon(flooredHist);
        } else {
            histogramLabel.setIcon(hist);
        }

        Exec.getFrame().pack();
    }

    private void showImage() {
        imageLabel.setText("");
        imageLabel.setIcon(new ImageIcon(image.bufferedImage));

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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

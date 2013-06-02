package CVImageProcessor.views;

import CVImageProcessor.models.PGM_Image;
import CVImageProcessor.views.ImageView;

import javax.swing.*;
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

    private PGM_Image image = null;

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

                if (image != null) showImage();
            }
        });
    }

    private void showImage() {
        ImageView imageView = new ImageView(image);

        imageLabel.setVisible(false);

        imagePanel.setSize(image.width, image.height);
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.PAGE_AXIS));
        imagePanel.add(imageView);

        DefaultTableModel model = new DefaultTableModel(6,2);

        Object[] columnTitles = new Object[] {"Property", "Value"};
        model.setColumnIdentifiers(columnTitles);

        String[] properties = new String[] {"File Name:", "Magic Number:", "Width:", "Height:", "Depth:", "Size:"};
        String[] values = new String[] {image.fileName, image.magicNumber, String.valueOf(image.width), String.valueOf(image.height), String.valueOf(image.depth), String.valueOf(image.fileRef.length())};

        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(properties[i], i, 0);
            model.setValueAt(values[i], i, 1);
        }

        metadataTable.setModel(model);
    }
}

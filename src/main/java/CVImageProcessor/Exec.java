package CVImageProcessor;

import CVImageProcessor.views.MainWindow;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: robbie
 * Date: 02.06.13
 * Time: 16:35
 * To change this template use File | Settings | File Templates.
 */
public class Exec {
    public static void main(String[] args) {
        // set the native look and feel for the UI
        // if the "native" LNF is Metal, e.g. on KDE, set it to Nimbus (if that is available)
        String LNF = UIManager.getSystemLookAndFeelClassName();
        try {
            if (!LNF.contains("metal")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        JFrame frame = new JFrame("CVImageProcessor");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

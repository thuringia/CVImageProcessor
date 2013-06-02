package CVImageProcessor.views;

import CVImageProcessor.models.PGM_Image;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Robert Wawrzyniak
 *         Date: 6/1/13
 *         Time: 7:02 PM
 */
public class ImageView extends JPanel {
    Logger logger = Logger.getLogger(ImageView.class);

    private PGM_Image img;

    public ImageView(PGM_Image img) {
        this.img = img;

        this.setSize(img.width, img.height);
        logger.debug("set image and bounds");
    }

    private class DrawWorker extends SwingWorker<Void, Void> {
        private Graphics g;

        public DrawWorker(Graphics g) {
            this.g = g;
        }

        @Override
        protected Void doInBackground() throws Exception {
            return null;
        }
    }

    /**
     * Invoked by Swing to draw components.
     * Applications should not invoke <code>paint</code> directly,
     * but should instead use the <code>repaint</code> method to
     * schedule the component for redrawing.
     * <p/>
     * This method actually delegates the work of painting to three
     * protected methods: <code>paintComponent</code>,
     * <code>paintBorder</code>,
     * and <code>paintChildren</code>.  They're called in the order
     * listed to ensure that children appear on top of component itself.
     * Generally speaking, the component and its children should not
     * paint in the insets area allocated to the border. Subclasses can
     * just override this method, as always.  A subclass that just
     * wants to specialize the UI (look and feel) delegate's
     * <code>paint</code> method should just override
     * <code>paintComponent</code>.
     *
     * @param g the <code>Graphics</code> context in which to paint
     * @see #paintComponent
     * @see #paintBorder
     * @see #paintChildren
     * @see #getComponentGraphics
     * @see #repaint
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        logger.debug("starting to paint image");
        for (int row = 0; row < img.width; row++) {
            for (int col = 0; col < img.height; col++) {
                int val = img.data[row][col];
                Color color = new Color(val, val, val);

                g.setColor(color);
                g.drawLine(row, col, row, col);
            }
        }
        logger.debug("finished painting");
    }
}

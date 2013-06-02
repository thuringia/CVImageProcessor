package CVImageProcessor;

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
    private PGM_Image img;

    public ImageView (PGM_Image img) {
        this.img = img;

        this.setBounds(0,0,img.width, img.height);
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

        for (int row = 0; row < img.height; row++) {
            for (int col = 0; col < img.width; col++) {
                int val = img.data[row][col];
                Color color = new Color(val, val, val);

                g.setColor(color);
                g.drawLine(row,col,row,col);
            }
        }
    }
}

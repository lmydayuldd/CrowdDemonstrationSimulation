package graphics;

import javax.swing.*;
import java.awt.*;

/**
 * @author Konrad Hopek JPanel with background
 */
class ImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final int width;
    private int height;
    private final Image img;

    public ImagePanel(String img, int x, int y, int width, int height) {
        this(new ImageIcon(img).getImage(), x, y, width, height);
    }

    private ImagePanel(Image img, int x, int y, int width, int height) {
        this.img = img;
        if (width == -1)
            this.width = img.getWidth(null);
        else
            this.width = width;
        if (height == -1)
            this.height = img.getHeight(null);
        else
            this.height = height;
        setLocation(x, y);
        setDimension();
        setLayout(null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, this.width, this.height, null);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private void setDimension() {
        Dimension size = new Dimension(this.width, this.height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
    }

    void setHeight() {
        Component[] components = this.getComponents();
        int newHeight = 0;
        for (Component comp : components)
            newHeight += comp.getHeight() + 3;
        this.height = newHeight;
    }

    public void updateHeight() {
        setHeight();
        setDimension();
    }

    public void updateHeight(int delay) {
        setHeight();
        this.height += delay;
        setDimension();
    }

}
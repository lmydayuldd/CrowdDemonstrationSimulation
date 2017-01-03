package graphics;

import javax.swing.*;
import java.awt.*;

class ImageButton extends JButton {

    private static final long serialVersionUID = 1L;

    public ImageButton(String img, String title, int x, int y) {
        this(new ImageIcon(img), title, x, y);

    }

    private ImageButton(ImageIcon img, String title, int x, int y) {
        this.setIcon(img);
        this.setBounds(x, y, img.getIconWidth(), img.getIconHeight());
        JLabel label = new JLabel(title, JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setSize(170, 30);
        label.setLocation((img.getIconWidth() - label.getWidth()) / 2,
                (img.getIconHeight() - label.getHeight()) / 2);
        label.setForeground(Color.white);
        add(label);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setRolloverEnabled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setLayout(null);
    }


}

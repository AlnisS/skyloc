package org.alniss.skyloc;

import javax.swing.*;
import java.awt.*;

class ImageComponent extends JComponent {
    private static final long serialVersionUID = 1L;
    private final Image image;

    ImageComponent(Image image) {
        this.image = image;
    }

    public void paintComponent(Graphics g) {
        if (image == null)
            return;
        g.drawImage(image, 0, 0, this);
    }
}

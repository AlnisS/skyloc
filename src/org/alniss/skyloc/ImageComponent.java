package org.alniss.skyloc;

import javax.swing.*;
import java.awt.*;

public class ImageComponent extends JComponent {
    private static final long serialVersionUID = 1L;
    private Image image;

    ImageComponent(Image image) {
        this.image = image;
    }

    public void paintComponent (Graphics g){
        if (image == null)
            return;
        g.drawImage(image, 0, 0, this);
    }
}

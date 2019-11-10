package org.alniss.skyloc;

import javax.swing.*;
import java.awt.*;

class ImageFrame extends JFrame {
    static final int DEFAULT_WIDTH = 800;
    static final int DEFAULT_HEIGHT = 700;

    ImageFrame(){
        setTitle("ImageTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        Image image = MiscUtils.matToImage(InversePerspectiveTransform.bookTransform());
        ImageComponent component = new ImageComponent(image);
        add(component);
    }

    static Runnable getRunnable() {
        return new Runnable() {
            public void run(){
                ImageFrame frame = new ImageFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        };
    }
}


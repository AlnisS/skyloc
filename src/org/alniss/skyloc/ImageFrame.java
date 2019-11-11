package org.alniss.skyloc;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;

import static org.alniss.skyloc.MiscUtils.matToImage;
import static org.alniss.skyloc.MiscUtils.saveMat;

class ImageFrame extends JFrame {
    static final int DEFAULT_WIDTH = 800;
    static final int DEFAULT_HEIGHT = 700;

    ImageFrame(){
        setTitle("ImageTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//        Image image = MiscUtils.matToImage(InversePerspectiveTransform.bookTransform());\
        Mat mat = InversePerspectiveTransform.testTransform();
        Image image = matToImage(mat);
        saveMat(mat, "png", "first_field_test.png");
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


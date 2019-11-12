package org.alniss.skyloc;

import javax.swing.*;
import java.awt.*;

class ImageFrame extends JFrame {
    static final int DEFAULT_WIDTH = 450;
    static final int DEFAULT_HEIGHT = 450;

    ImageFrame(){
        setTitle("ImageTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        StoneWrangler stoneWrangler = new StoneWrangler();
        stoneWrangler.analyze(StoneWranglerUtils.loadMat(StoneWranglerUtils.dataFile("test_frame_003.jpg")));
        Image image = StoneWranglerUtils.matToImage(stoneWrangler.getCurrentBirdsEyeView());
        StoneWranglerUtils.saveMat(stoneWrangler.getCurrentBirdsEyeView(), "png", "second_field_test.png");

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


package org.alniss.skyloc;

import javax.swing.*;
import java.awt.*;

class ImageFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 450;
    private static final int DEFAULT_HEIGHT = 450;

    private ImageFrame(){
        setTitle("ImageTest");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        StoneWrangler stoneWrangler = new StoneWrangler();
        stoneWrangler.analyze(StoneWranglerUtils.loadMat(StoneWranglerUtils.dataFile("test_frame_003.jpg")));
        Image image = StoneWranglerUtils.matToImage(stoneWrangler.getVisualization());
        StoneWranglerUtils.saveMat(stoneWrangler.getVisualization(), "png", "second_field_test.png");

        ImageComponent component = new ImageComponent(image);
        add(component);
    }

    static Runnable getRunnable() {
        //noinspection Convert2Lambda
        return new Runnable() {
            public void run(){
                ImageFrame frame = new ImageFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        };
    }
}


package org.alniss.skyloc;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
//        EventQueue.invokeLater(ImageFrame.getRunnable());
        StoneWrangler stoneWrangler = new StoneWrangler();
        stoneWrangler.analyze(StoneWranglerUtils.loadMat(StoneWranglerUtils.dataFile("test_frame_003.jpg")));
    }
}

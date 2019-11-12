package org.alniss.skyloc;

import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
//        EventQueue.invokeLater(ImageFrame.getRunnable());
        File frames_proc = StoneWranglerUtils.dataFile("frames_proc/");
        if (!frames_proc.exists())
            frames_proc.mkdir();
        StoneWrangler stoneWrangler = new StoneWrangler();
        for (int i = 1; i <= 405; i++) {
            stoneWrangler.analyze(StoneWranglerUtils.loadMat(StoneWranglerUtils.dataFile(
                    "frames/" + StoneWranglerUtils.padLeftZeros(i + "", 4) + ".jpg")));
            Image image = StoneWranglerUtils.matToImage(stoneWrangler.getCurrentBirdsEyeView());
            StoneWranglerUtils.saveMat(stoneWrangler.getCurrentBirdsEyeView(), "png",
                    "frames_proc/" + StoneWranglerUtils.padLeftZeros(i + "", 4) + ".png");
            System.out.println("processed: " + i);
        }
        System.out.println("done!");
    }
}

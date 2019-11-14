package org.alniss.skyloc;

// import java.awt.*;
import java.io.File;

class Main {
    public static void main(String[] args) {
//        EventQueue.invokeLater(ImageFrame.getRunnable());
        File frames_proc = StoneWranglerUtils.dataFile("frames_proc/");
        if (!frames_proc.exists())
            if (!frames_proc.mkdir())
                System.out.println("problem making directory: " + frames_proc.toString());
        StoneWrangler stoneWrangler = new StoneWrangler();
        for (int i = 1; i <= 405; i++) {
            stoneWrangler.analyze(StoneWranglerUtils.loadMat(StoneWranglerUtils.dataFile(
                    "frames/" + StoneWranglerUtils.padLeftZeros(i + "", 4) + ".jpg")));
//            Image image = StoneWranglerUtils.matToImage(stoneWrangler.getVisualization());
            StoneWranglerUtils.saveMat(stoneWrangler.getVisualization(), "png",
                    "frames_proc/" + StoneWranglerUtils.padLeftZeros(i + "", 4) + ".png");
            System.out.println("processed: " + i);
        }
        System.out.println("done!");
    }
}

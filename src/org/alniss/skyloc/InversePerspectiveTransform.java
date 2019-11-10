package org.alniss.skyloc;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class InversePerspectiveTransform {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static Mat inversePerspectiveTransform() {
        Mat im_src = loadMat(new File(System.getProperty("user.dir") + "/data/book2.jpg"));
        MatOfPoint2f pts_src = new MatOfPoint2f();
        pts_src.fromArray(new org.opencv.core.Point(141, 131), new org.opencv.core.Point(480, 159), new org.opencv.core.Point(493, 630), new org.opencv.core.Point(64, 601));
        Mat im_dst = loadMat(new File(System.getProperty("user.dir") + "/data/book1.jpg"));
        MatOfPoint2f pts_dst = new MatOfPoint2f();
        pts_dst.fromArray(new org.opencv.core.Point(318, 256), new org.opencv.core.Point(534, 372), new org.opencv.core.Point(316, 670), new Point(73, 473));
        Mat h = Calib3d.findHomography(pts_src, pts_dst);
//        Mat im_out = new Mat(im_src.size(), CvType.CV_8UC3);
        Mat im_out = new Mat();
        warpPerspective(im_src, im_out, h, im_dst.size());
//        System.out.println(im_out);
//        imwrite(System.getProperty("user.dir") + "/data/book_out.jpg", im_out);
//        System.out.println(System.getProperty("user.dir"));
        saveMat(im_out);
        return im_out;
    }

    public static Mat loadMat(File file) {
        try {
            return bufferedImageToMat(ImageIO.read(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static void saveMat(Mat mat) {
        Image image = ImagePanel.toBufferedImage(mat);
        try {
            ImageIO.write(toBufferedImage(image), "png", new File(System.getProperty("user.dir") + "/data/out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}

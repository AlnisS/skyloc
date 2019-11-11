package org.alniss.skyloc;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

class MiscUtils {
    static Mat loadMat(File file) {
        try {
            return bufferedImageToMat(ImageIO.read(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void saveMat(Mat mat) {
        try {
            ImageIO.write(imageToBufferedImage(matToImage(mat)),
                    "png", new File(System.getProperty("user.dir") + "/data/out.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveMat(Mat mat, String formatName, String fileName) {
        try {
            ImageIO.write(imageToBufferedImage(matToImage(mat)),
                    formatName, dataFile(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    static BufferedImage imageToBufferedImage(Image img) {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    static Image matToImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    static File dataFile(String name) {
        return new File(System.getProperty("user.dir") + "/data/" + name);
    }

    static double median(java.util.List list) {
        Collections.sort(list);
        double median;
        if (list.size() % 2 == 0)
            median = ((double) list.get(list.size() / 2) + (double) list.get(list.size() / 2 - 1)) / 2;
        else
            median = (double) list.get(list.size() / 2);
        return median;
    }
}

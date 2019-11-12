package org.alniss.skyloc;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoneWranglerUtils {
    static MatOfPoint2f createMatOfPoint2f(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        MatOfPoint2f result = new MatOfPoint2f();
        result.fromArray(new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2),
                new org.opencv.core.Point(x3, y3), new org.opencv.core.Point(x4, y4));
        return result;
    }

    static Mat denoiseMat(Mat src) {
        Mat denoised = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.GaussianBlur(src, denoised, StoneWranglerConstants.GAUSSIAN_DENOISE_K, 0);
        return denoised;
    }

    static Mat maskByHSVThreshhold(Mat src, Scalar lower, Scalar upper) {
        Mat hsvFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src, hsvFrame, Imgproc.COLOR_BGR2HSV, 3);
        Mat mask = new Mat(hsvFrame.rows(), hsvFrame.cols(), CvType.CV_8U, new Scalar(3));
        Core.inRange(hsvFrame, lower, upper, mask);
        return mask;
    }

    static Mat convert(Mat src, int conversion) {
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, conversion);
        return dst;
    }

    static List<Scalar> houghLinesMatToList(Mat lines) {
        List<Scalar> result = new ArrayList<>();
        for (int x = 0; x < lines.rows(); x++) {
            double theta = lines.get(x, 0)[1];
            double rho   = lines.get(x, 0)[0];
            result.add(new Scalar(theta, rho));
        }
        return result;
    }

    static void drawLine(Mat dst, double theta, double rho, Scalar color, int thickness) {
        double a = Math.cos(theta);
        double b = Math.sin(theta);
        double x0 = a * rho, y0 = b * rho;
        Point pt1 = new Point(Math.round(x0 + 1000 * -b), Math.round(y0 + 1000 * a));
        Point pt2 = new Point(Math.round(x0 - 1000 * -b), Math.round(y0 - 1000 * a));
        Imgproc.line(dst, pt1, pt2, color, thickness, Imgproc.LINE_AA, 0);
    }

    static double median(List list) {
        if (list.size() == 0)
            return -1;
        Collections.sort(list);
        double median;
        if (list.size() % 2 == 0)
            median = ((double) list.get(list.size() / 2) + (double) list.get(list.size() / 2 - 1)) / 2;
        else
            median = (double) list.get(list.size() / 2);
        return median;
    }

    // -------------------- not refactored - copy paste from old version --------------------

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

    // -------------------- some line stuff --------------------

    static List<Scalar> integerPointsAlongLine(double theta, double rho, int width, int height) {
        List<Scalar> result = new ArrayList<>();
        double lineSlopeAngle = theta + Math.PI * 0.5;
        if (Math.abs(Math.tan(lineSlopeAngle)) < 1) {
            double pointX    = Math.cos(theta) * rho;
            double pointY    = Math.sin(theta) * rho;
            double lineSlope = Math.tan(lineSlopeAngle);
            double y = pointY - lineSlope * pointX;  // start at the y intercept
            for (int x = 0; x < width; x++, y += lineSlope)
                if (Math.round(y) >= 0 && Math.round(y) < height)
                    result.add(new Scalar(Math.round(x), Math.round(y)));
        } else {
            double pointX    =     Math.cos(theta) * rho;
            double pointY    =     Math.sin(theta) * rho;
            double lineSlope = 1 / Math.tan(lineSlopeAngle);
            double x = pointX - lineSlope * pointY;  // start at the x intercept
            for (int y = 0; y < height; y++, x += lineSlope)
                if (Math.round(x) >= 0 && Math.round(x) < width)
                    result.add(new Scalar(Math.round(x), Math.round(y)));
        }
        return result;
    }
}

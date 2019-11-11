package org.alniss.skyloc;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.alniss.skyloc.MiscUtils.*;
import static org.opencv.imgproc.Imgproc.*;

public class InversePerspectiveTransform {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static double stoneX, stoneY;

    static Mat bookTransform() {
        Mat im_src = loadMat(dataFile("book2.jpg"));
        Mat im_dst = loadMat(dataFile("book1.jpg"));
        MatOfPoint2f pts_src = createMatOfPoint2f(141, 131, 480, 159, 493, 630, 64, 601);
        MatOfPoint2f pts_dst = createMatOfPoint2f(318, 256, 534, 372, 316, 670, 73, 473);
        return inversePerspectiveTransform(im_src, im_dst, pts_src, pts_dst);
    }

    static Mat testTransform() {
        Mat im_src = loadMat(dataFile("test_frame_002.jpg"));

        long startTime = System.currentTimeMillis();
        Mat im_dst = new Mat(600, 600, CvType.CV_8UC3);

//        MatOfPoint2f pts_src = createMatOfPoint2f(493, 719 - 322, 740, 719 - 342, 852, 719 - 153, 537, 719 - 113);
        MatOfPoint2f pts_src = createMatOfPoint2f(310, 719 - 376, 720, 719 - 369, 770, 719 - 268, 67, 719 - 277);
        MatOfPoint2f pts_dst = createMatOfPoint2f(200, 200, 285, 200, 285, 315, 200, 315);

        Mat transformed = inversePerspectiveTransform(im_src, im_dst, pts_src, pts_dst);
        Mat filtered = filterForStone(transformed, true);
        Mat edges = edgeFilter(filtered);
        Mat houghLines = houghLinesFilter(edges);
        System.out.println(System.currentTimeMillis() - startTime);
        return houghLines;
    }

    static Mat inversePerspectiveTransform(Mat im_src, Mat im_dst, MatOfPoint2f pts_src, MatOfPoint2f pts_dst) {
        Mat h = Calib3d.findHomography(pts_src, pts_dst);
        Mat im_out = new Mat();
        warpPerspective(im_src, im_out, h, im_dst.size());
        return im_out;
    }

    static MatOfPoint2f createMatOfPoint2f(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        MatOfPoint2f result = new MatOfPoint2f();
        result.fromArray(new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2),
                new org.opencv.core.Point(x3, y3), new org.opencv.core.Point(x4, y4));
        return result;
    }

    static Mat filterForStone(Mat src, boolean returnOnlyMask) {
        // TODO: investigate filtering using ground instead of stone

        final Size ksize = new Size(3, 3);
        Mat denoised = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.GaussianBlur(src, denoised, ksize, 0);

//        final Scalar lower = new Scalar(.12 * 179, .731 * 255, .6 * 255); // .025, .731, .110
//        final Scalar upper = new Scalar(.14 * 179, 1 * 255, 1 * 255); // .168, 1.000, 1.000
        final Scalar lower = new Scalar(.04 * 179, .7 * 255, 0.1* 255); // .025, .731, .110
        final Scalar upper = new Scalar(.14 * 179, 1. * 255, 1. * 255); // .168, 1.000, 1.000

        Mat hsvFrame = new Mat(denoised.rows(), denoised.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(denoised, hsvFrame, Imgproc.COLOR_BGR2HSV, 3);
        Mat stoneMask = new Mat(hsvFrame.rows(), hsvFrame.cols(), CvType.CV_8U, new Scalar(3));
        Core.inRange(hsvFrame, lower, upper, stoneMask);

        if (returnOnlyMask) {
            Mat dst = new Mat();
            Imgproc.cvtColor(stoneMask, dst, COLOR_GRAY2BGR);
            return dst;
        } else {
            Mat dst = new Mat();
            Core.bitwise_and(src, src, dst, stoneMask);
            return dst;
        }
    }

    static Mat edgeFilter(Mat frame) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayImage, grayImage, 1, 3, 3, false);
        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);
        frame.copyTo(dest, grayImage);
        return dest;
    }

    static Mat houghLinesFilter(Mat frame) {
        Mat cdst = frame.clone();
        Mat dst = new Mat();
        Imgproc.cvtColor(cdst, dst, COLOR_BGR2GRAY);
//        MatOfPoint2f lines = new MatOfPoint2f();
        Mat lines = new Mat();
        Imgproc.HoughLines(dst, lines, 1, Math.PI / 180, 20);
        List<Double> thetaDiffs = new ArrayList<>();
        Map<Double, Scalar> linesMap = new HashMap<>();
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
//            drawLine(cdst, rho, theta);
            double thetaDiff = (theta - Math.PI * .5) * (theta - Math.PI * .5);
            thetaDiffs.add(thetaDiff);
            linesMap.put(thetaDiff, new Scalar(theta, rho));
        }
        Collections.sort(thetaDiffs);
        for (Double thetaDiff : thetaDiffs) {
//            System.out.println("thetaDiff: " + thetaDiff + " theta: " + linesMap.get(thetaDiff).val[0] + " rho: " + linesMap.get(thetaDiff).val[1]);
        }

        double thetaDiff = thetaDiffs.get(0);
        double theta = linesMap.get(thetaDiff).val[0];
        double rho = linesMap.get(thetaDiff).val[1];
//        System.out.println("the theta: " + theta + " the rho: " + rho);
//        drawLine(cdst, theta, rho);

        List<Double> hitpoints = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            Mat rowOfInterest = frame.row((int) Math.round(rho + i));
            for (int j = 0; j < rowOfInterest.cols(); j++) {
                if (rowOfInterest.get(0, j)[0] > 0)
                    hitpoints.add((double) j);
            }
        }

        stoneX = median(hitpoints);
        stoneY = rho;

        System.out.println("stoneX: " + stoneX + " stoneY: " + stoneY);

        drawLine(cdst, 0, stoneX);
        drawLine(cdst, Math.PI * .5, stoneY);

        return cdst;
    }

    static void drawLine(Mat cdst, double theta, double rho) {
        double a = Math.cos(theta), b = Math.sin(theta);
        double x0 = a*rho, y0 = b*rho;
        Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
        Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
        Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 1, Imgproc.LINE_AA, 0);
    }
}

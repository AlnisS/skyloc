package org.alniss.skyloc;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.*;

class StoneWrangler {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }  // load OpenCV library

    private MatOfPoint2f calibrationFramePoints;
    private MatOfPoint2f calibrationWorldPoints;
    private Mat homography;

    private double stonePixelX, stonePixelY, stoneWorldX, stoneWorldY, stoneTheta, stoneRho;
    private Mat currentBirdsEyeView;

    StoneWrangler() {
        calibrationFramePoints = StoneWranglerConstants.getCalF();
        calibrationWorldPoints = StoneWranglerConstants.getCalW();
        homography = Calib3d.findHomography(calibrationFramePoints, calibrationWorldPoints);
    }

    double getStonePixelX() { return stonePixelX; }
    double getStonePixelY() { return stonePixelY; }
    Mat getCurrentBirdsEyeView() { return currentBirdsEyeView; }

    void analyze(Mat src_) {
        long startMillis = System.currentTimeMillis();
        Mat src = new Mat();
        Core.flip(src_, src, 0);
        Mat transformed = inversePerspectiveTransform(src);
        currentBirdsEyeView = transformed;
        Mat filtered = filterForStone(transformed);
        Mat edges = cannyEdgeDetection(filtered);
//        currentBirdsEyeView = edges;
        Mat houghLines = houghLines(edges);
        List<Scalar> lines = StoneWranglerUtils.houghLinesMatToList(houghLines);
        Scalar result = concludeStonePosition(lines, filtered);
//        for (Scalar s : lines)
//            StoneWranglerUtils.drawLine(currentBirdsEyeView, s.val[0], s.val[1], StoneWranglerConstants.RED_SCALAR, 1);
        StoneWranglerUtils.drawLine(currentBirdsEyeView, stoneTheta, stoneRho, StoneWranglerConstants.BLUE_SCALAR, 1);
        if (result != null) {
            stonePixelX = result.val[0];
            stonePixelY = result.val[1];
            stoneWorldX = stonePixelX * StoneWranglerConstants.PIXEL_SIZE - StoneWranglerConstants.AREA_X_DIMENSION / 2;
            stoneWorldY = StoneWranglerConstants.AREA_Y_DIMENSION - stonePixelY * StoneWranglerConstants.PIXEL_SIZE;
        }
        reportStonePosition(currentBirdsEyeView);
        System.out.println("ran in " + (System.currentTimeMillis() - startMillis) + " millis");
    }

    Mat inversePerspectiveTransform(Mat src) {
        Mat dst = new Mat(
                new Size(
                (int) (StoneWranglerConstants.AREA_X_DIMENSION / StoneWranglerConstants.PIXEL_SIZE),
                (int) (StoneWranglerConstants.AREA_Y_DIMENSION / StoneWranglerConstants.PIXEL_SIZE)),
                CvType.CV_8UC3);
        Imgproc.warpPerspective(src, dst, homography, dst.size());
        return dst;
    }

    Mat filterForStone(Mat src) {
        Mat denoised = StoneWranglerUtils.denoiseMat(src);
        Mat mask = StoneWranglerUtils.maskByHSVThreshhold(
                denoised, StoneWranglerConstants.STONE_HSV_LOWER, StoneWranglerConstants.STONE_HSV_UPPER);
        Mat result = StoneWranglerUtils.convert(mask, Imgproc.COLOR_GRAY2BGR);
        return result;
    }

    Mat cannyEdgeDetection(Mat src) {
        Mat grayImage = StoneWranglerUtils.convert(src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayImage, grayImage, 1, 3, 3, false);
        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);
        src.copyTo(dest, grayImage);
        return dest;
    }

    Mat houghLines(Mat src) {
        Mat cdst = src.clone();
        Mat dst = StoneWranglerUtils.convert(cdst, Imgproc.COLOR_BGR2GRAY);
        Mat lines = new Mat();
        Imgproc.HoughLines(dst, lines,
                StoneWranglerConstants.HOUGH_LINES_RHO_STEP,
                StoneWranglerConstants.HOUGH_LINES_THETA_STEP,
                StoneWranglerConstants.HOUGH_LINES_THRESHHOLD);
        return lines;
    }

    Scalar concludeStonePosition(List<Scalar> lines, Mat edges) {

        List<List<Scalar>> bins = StoneWranglerUtils.binLines(lines);
        List<Scalar> binMedians = new ArrayList<>();
        for (List<Scalar> bin : bins) {
            List<Double> thetas = new ArrayList<>();
            List<Double> rhos = new ArrayList<>();
            for (Scalar s : bin) {
                thetas.add(s.val[0]);
                rhos.add(s.val[1]);
            }
            binMedians.add(new Scalar(StoneWranglerUtils.median(thetas), StoneWranglerUtils.median(rhos)));
        }

        List<Double> thetaDiffs = new ArrayList<>();
        Map<Double, Scalar> linesMap = new HashMap<>();
        for (Scalar s : binMedians) {
            double thetaDiff = Math.pow(s.val[0] - Math.PI / 2, 2);
            thetaDiffs.add(thetaDiff);
            linesMap.put(thetaDiff, s);
//            StoneWranglerUtils.drawLine(currentBirdsEyeView, s.val[0], s.val[1], StoneWranglerConstants.RED_SCALAR, 1);
        }
        Collections.sort(thetaDiffs);

        List<Scalar> candidateLines = new ArrayList<>();

        for (int i = 0; i < thetaDiffs.size(); i++) {
            double thetaDiff = thetaDiffs.get(i);
            Scalar line = linesMap.get(thetaDiff);
            double y = StoneWranglerUtils.findLineCenter(edges, line.val[0], line.val[1]).val[1];
            double fudgeFactor = (StoneWranglerConstants.AREA_Y_DIMENSION / StoneWranglerConstants.PIXEL_SIZE - y) / 200;
            if (thetaDiff < (Math.PI * 45 / 180) / (fudgeFactor))
                candidateLines.add(line);
        }
        if (candidateLines.size() == 0)
            return null;
        Scalar currentCandidate = candidateLines.get(0);
//        double bestY = 0;
        for (int i = 1; i < candidateLines.size(); i++) {
//            Scalar line = candidateLines.get(i);
//            double y = StoneWranglerUtils.findLineCenter(edges, line.val[0], line.val[1]).val[1];
//            System.out.println(line.val[1] + "    " + y);
//            if (y < bestY) {
//                currentCandidate = line;
//                bestY = y;
//            }
        }

//        double thetaDiff = thetaDiffs.get(0);
//        double theta     = linesMap.get(thetaDiff).val[0];
//        double rho       = linesMap.get(thetaDiff).val[1];
        double theta = currentCandidate.val[0];
        double rho = currentCandidate.val[1];
        stoneTheta = theta;
        stoneRho = rho;

//        List<Double>
//                xvals = new ArrayList<>(),
//                yvals = new ArrayList<>();
//        for (int i = -1; i <= 1; i++) {
//            List<Scalar> points = StoneWranglerUtils.integerPointsAlongLine(theta, rho + i, edges.width(), edges.height());
//            for (Scalar s : points) {
//                if (edges.get((int) s.val[1], (int) s.val[0])[0] > 0) {
//                    xvals.add(s.val[0]);
//                    yvals.add(s.val[1]);
//                }
//            }
//        }
//        double stoneX = StoneWranglerUtils.median(xvals);
//        double stoneY = StoneWranglerUtils.median(yvals);
        Scalar center = StoneWranglerUtils.findLineCenter(edges, theta, rho);
        return new Scalar(center.val[0], center.val[1]);
    }

    void reportStonePosition(Mat dst) {
        System.out.println("stone world X: " + stoneWorldX + " stone world Y: " + stoneWorldY);
        StoneWranglerUtils.drawLine(dst, 0, stonePixelX, StoneWranglerConstants.GREEN_SCALAR, 1);
        StoneWranglerUtils.drawLine(dst, Math.PI * .5, stonePixelY, StoneWranglerConstants.GREEN_SCALAR, 1);
    }
}

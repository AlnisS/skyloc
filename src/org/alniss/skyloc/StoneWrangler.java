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
    private Mat visualization;

    StoneWrangler() {
        calibrationFramePoints = StoneWranglerConstants.getCalF();
        calibrationWorldPoints = StoneWranglerConstants.getCalW();
        homography = Calib3d.findHomography(calibrationFramePoints, calibrationWorldPoints);
    }

    double getStonePixelX() { return stonePixelX; }
    double getStonePixelY() { return stonePixelY; }
    Mat getVisualization() { return visualization; }

    void analyze(Mat src_) {
        long startMillis = System.currentTimeMillis();
        Mat src = new Mat();
        Core.flip(src_, src, 0);
        Mat transformed = inversePerspectiveTransform(src);
        visualization = transformed;

        Mat filtered = filterForStone(transformed);
        Mat edges = cannyEdgeDetection(filtered);
        Mat houghLines = houghLines(edges);
        List<Scalar> lines = StoneWranglerUtils.houghLinesMatToList(houghLines);
        Scalar result = concludeStonePosition(lines, filtered);
        updateStonePosition(result);
        reportStonePosition();
        visualization = generateVisualization(src, transformed.size());
        System.out.println("ran in " + (System.currentTimeMillis() - startMillis) + " millis");
    }

    private Mat inversePerspectiveTransform(Mat src) {
        Size size =
                new Size(
                (int) (StoneWranglerConstants.AREA_X_DIMENSION / StoneWranglerConstants.PIXEL_SIZE),
                (int) (StoneWranglerConstants.AREA_Y_DIMENSION / StoneWranglerConstants.PIXEL_SIZE));
        return StoneWranglerUtils.warpPerspective(src, size, homography);
    }

    private Mat filterForStone(Mat src) {
        Mat denoised = StoneWranglerUtils.denoiseMat(src);
        Mat mask = StoneWranglerUtils.maskByHSVThreshold(
                denoised, StoneWranglerConstants.STONE_HSV_LOWER, StoneWranglerConstants.STONE_HSV_UPPER);
        return StoneWranglerUtils.convert(mask, Imgproc.COLOR_GRAY2BGR);
    }

    private Mat cannyEdgeDetection(Mat src) {
        Mat grayImage = StoneWranglerUtils.convert(src, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayImage, grayImage, 1, 3, 3, false);
        Mat dest = new Mat();
        Core.add(dest, Scalar.all(0), dest);
        src.copyTo(dest, grayImage);
        return dest;
    }

    private Mat houghLines(Mat src) {
        Mat cdst = src.clone();
        Mat dst = StoneWranglerUtils.convert(cdst, Imgproc.COLOR_BGR2GRAY);
        Mat lines = new Mat();
        Imgproc.HoughLines(dst, lines,
                StoneWranglerConstants.HOUGH_LINES_RHO_STEP,
                StoneWranglerConstants.HOUGH_LINES_THETA_STEP,
                StoneWranglerConstants.HOUGH_LINES_THRESHOLD);
        return lines;
    }

    private Scalar concludeStonePosition(List<Scalar> lines, Mat edges) {
        List<Scalar> binMedians = StoneWranglerUtils.binMedians(StoneWranglerUtils.binLines(lines));

        List<Scalar> candidateLines = new ArrayList<>();
        for (Scalar s : binMedians) {
            if (Math.pow(s.val[0] - Math.PI / 2, 2) < (Math.PI * 45) / 180)
                candidateLines.add(s);
        }
        if (candidateLines.size() == 0)
            return null;
        Scalar currentCandidate = candidateLines.get(0);

        for (int i = 1; i < candidateLines.size(); i++) {
            Scalar proposedCandidate = candidateLines.get(i);
            double currentY  = StoneWranglerUtils.findLineCenter(edges, currentCandidate.val[0],  currentCandidate.val[1]).val[1];
            double proposedY = StoneWranglerUtils.findLineCenter(edges, proposedCandidate.val[0], proposedCandidate.val[1]).val[1];
            if (proposedY > currentY)
                currentCandidate = proposedCandidate;
        }

        double theta = currentCandidate.val[0];
        double rho = currentCandidate.val[1];
        stoneTheta = theta;
        stoneRho = rho;

        Scalar center = StoneWranglerUtils.findLineCenter(edges, theta, rho);
        return new Scalar(center.val[0], center.val[1]);
    }

    private void reportStonePosition() {
        System.out.println("stone world X: " + stoneWorldX);
        System.out.println("stone world Y: " + stoneWorldY);
    }

    private void updateStonePosition(Scalar result) {
        if (result != null) {
            stonePixelX = result.val[0];
            stonePixelY = result.val[1];
            stoneWorldX = stonePixelX * StoneWranglerConstants.PIXEL_SIZE - StoneWranglerConstants.AREA_X_DIMENSION / 2;
            stoneWorldY = StoneWranglerConstants.AREA_Y_DIMENSION - stonePixelY * StoneWranglerConstants.PIXEL_SIZE;
        }
    }

    private Mat generateVisualization(Mat src, Size flatSize) {
        return addPoseText(
                StoneWranglerUtils.verticalFlip(
                    StoneWranglerUtils.add(
                            src,
                            StoneWranglerUtils.warpPerspective(
                                    visualizePoseOnFlat(
                                            new Mat(flatSize, CvType.CV_8UC3, new Scalar(0, 0, 0))),
                                    src.size(),
                                    Calib3d.findHomography(calibrationWorldPoints, calibrationFramePoints)))));
    }

    private Mat visualizePoseOnFlat(Mat flatView) {
        StoneWranglerUtils.drawLine(flatView, stoneTheta, stoneRho, StoneWranglerConstants.BLUE_SCALAR, 1);
        StoneWranglerUtils.drawLine(flatView, 0, stonePixelX, StoneWranglerConstants.GREEN_SCALAR, 1);
        StoneWranglerUtils.drawLine(flatView, Math.PI * .5, stonePixelY, StoneWranglerConstants.GREEN_SCALAR, 1);
        return flatView;
    }

    private Mat addPoseText(Mat frame) {
        StoneWranglerUtils.addText(frame, "Stone orientation (radians): " + (stoneTheta - Math.PI / 2), 50);
        StoneWranglerUtils.addText(frame, "Stone X position (inches): " + (Math.round(stoneWorldX * 100) / 100.), 100);
        StoneWranglerUtils.addText(frame, "Stone Y position (inches): " + (Math.round(stoneWorldY * 100) / 100.), 150);
        return frame;
    }
}

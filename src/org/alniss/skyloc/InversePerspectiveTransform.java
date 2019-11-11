package org.alniss.skyloc;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;

import java.io.File;

import static org.alniss.skyloc.MiscUtils.dataFile;
import static org.alniss.skyloc.MiscUtils.loadMat;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class InversePerspectiveTransform {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static Mat bookTransform() {
        Mat im_src = loadMat(dataFile("book2.jpg"));
        Mat im_dst = loadMat(dataFile("book1.jpg"));
        MatOfPoint2f pts_src = createMatOfPoints2f(141, 131, 480, 159, 493, 630, 64, 601);
        MatOfPoint2f pts_dst = createMatOfPoints2f(318, 256, 534, 372, 316, 670, 73, 473);
        return inversePerspectiveTransform(im_src, im_dst, pts_src, pts_dst);
    }

    static Mat testTransform() {
        Mat im_src = loadMat(dataFile("test_frame_001.jpg"));
        Mat im_dst = new Mat(600, 600, CvType.CV_8UC3);

        MatOfPoint2f pts_src = createMatOfPoints2f(493, 719 - 322, 740, 719 - 342, 852, 719 - 153, 537, 719 - 113);
        MatOfPoint2f pts_dst = createMatOfPoints2f(200, 200, 285, 200, 285, 315, 200, 315);
        return inversePerspectiveTransform(im_src, im_dst, pts_src, pts_dst);
    }

    static Mat inversePerspectiveTransform(Mat im_src, Mat im_dst, MatOfPoint2f pts_src, MatOfPoint2f pts_dst) {
        Mat h = Calib3d.findHomography(pts_src, pts_dst);
        Mat im_out = new Mat();
        warpPerspective(im_src, im_out, h, im_dst.size());
        return im_out;
    }

    static MatOfPoint2f createMatOfPoints2f(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        MatOfPoint2f result = new MatOfPoint2f();
        result.fromArray(new org.opencv.core.Point(x1, y1), new org.opencv.core.Point(x2, y2),
                new org.opencv.core.Point(x3, y3), new org.opencv.core.Point(x4, y4));
        return result;
    }
}

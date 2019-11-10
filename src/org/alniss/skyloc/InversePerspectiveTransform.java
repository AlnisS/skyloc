package org.alniss.skyloc;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.File;

import static org.alniss.skyloc.MiscUtils.loadMat;
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
        Mat im_out = new Mat();
        warpPerspective(im_src, im_out, h, im_dst.size());
        return im_out;
    }
}

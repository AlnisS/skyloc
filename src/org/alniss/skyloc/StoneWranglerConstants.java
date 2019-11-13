package org.alniss.skyloc;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

class StoneWranglerConstants {
    static double  // frame space calibration points
            CALX1F = 629,
            CALY1F = 413,
            CALX2F = 935,
            CALY2F = 412,
            CALX3F = 1067,
            CALY3F = 228,
            CALX4F = 635,
            CALY4F = 228;
    static double  // world space calibration points
            CALX1W = 0,
            CALY1W = 11 * 3,
            CALX2W = 8.5,
            CALY2W = 11 * 3,
            CALX3W = 8.5,
            CALY3W = 11 * 2,
            CALX4W = 0,
            CALY4W = 11 * 2;
    static double
            PIXEL_SIZE = 0.1;  // world size of bird's eye view pixel (e.g. .1 inch -> 1 px == .1)
    static double
            AREA_X_DIMENSION = 45,  // surveyed area's dimensions in world space
            AREA_Y_DIMENSION = 45;
    static Size
            GAUSSIAN_DENOISE_K = new Size(3, 3);
    static Scalar
            STONE_HSV_LOWER = new Scalar(.04 * 179, .7 * 255, 0.1* 255),
            STONE_HSV_UPPER = new Scalar(.14 * 179, 1. * 255, 1. * 255);
    static double
            HOUGH_LINES_RHO_STEP = 1,
            HOUGH_LINES_THETA_STEP = Math.PI / 180;
    static int
            HOUGH_LINES_THRESHHOLD = 10;
    static Scalar
            RED_SCALAR   = new Scalar(0, 0, 255),
            GREEN_SCALAR = new Scalar(0, 255, 0),
            BLUE_SCALAR  = new Scalar(255, 0, 0);
    static double
            CLOSE_ENOUGH_THETA = Math.PI * 15 / 180,
            CLOSE_ENOUGH_RHO = 40;

    static MatOfPoint2f getCalF() {
        return StoneWranglerUtils.createMatOfPoint2f(CALX1F, CALY1F, CALX2F, CALY2F, CALX3F, CALY3F, CALX4F, CALY4F);
    }

    static MatOfPoint2f getCalW() {
        double hxdpx = AREA_X_DIMENSION / 2 / PIXEL_SIZE;
        double ydpx  = AREA_Y_DIMENSION / PIXEL_SIZE;

        double  calx1w = CALX1W / PIXEL_SIZE + hxdpx,
                calx2w = CALX2W / PIXEL_SIZE + hxdpx,
                calx3w = CALX3W / PIXEL_SIZE + hxdpx,
                calx4w = CALX4W / PIXEL_SIZE + hxdpx;
        double  caly1w = ydpx - CALY1W / PIXEL_SIZE,
                caly2w = ydpx - CALY2W / PIXEL_SIZE,
                caly3w = ydpx - CALY3W / PIXEL_SIZE,
                caly4w = ydpx - CALY4W / PIXEL_SIZE;
        return StoneWranglerUtils.createMatOfPoint2f(calx1w, caly1w, calx2w, caly2w, calx3w, caly3w, calx4w, caly4w);
    }
}

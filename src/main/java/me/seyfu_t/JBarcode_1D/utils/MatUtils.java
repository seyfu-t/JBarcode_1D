package me.seyfu_t.JBarcode_1D.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MatUtils {

    public static Mat calcIntegralImage(Mat src) {
        Mat integralImage = new Mat();
        Imgproc.integral(src, integralImage);
        return integralImage;
    }

    public static Rect boxDetection(Mat src, Point cp) {
        Rect result = new Rect(0, 0, 0, 0);
        Size imgSize = src.size();

        // lfet
        for (int w = (int) cp.x; w >= 0; w--) {
            if (src.get((int) cp.y, w)[0] < 128) {
                result.x = w;
                break;
            }
        }

        // right
        for (int w = (int) cp.x; w < imgSize.width; w++) {
            if (src.get((int) cp.y, w)[0] < 128) {
                result.width = w - result.x;
                break;
            }
        }

        // up
        for (int h = (int) cp.y; h >= 0; h--) {
            if (src.get(h, (int) cp.x)[0] < 128) {
                result.y = h;
                break;
            }
        }

        // down
        for (int h = (int) cp.y; h < imgSize.height; h++) {
            if (src.get(h, (int) cp.x)[0] < 128) {
                result.height = h - result.y;
                break;
            }
        }

        return result;
    }

    public static Point findMaxPointWithSmooth(Mat src, Mat smoothMap, int winSize) {
        Size imgSize = src.size();
        Point maxPoint = new Point(0, 0);

        int nSize = winSize * winSize;
        int cSize = (int) (winSize / 2) + 1;

        int maxHeight = (int) imgSize.height - cSize;
        int maxWidth = (int) imgSize.width - cSize;
        double meanMax = 0.0f;

        for (int h = 0; h < imgSize.height; h++) {
            int tempTop = h - cSize;
            int ntop = (tempTop > maxHeight) ? maxHeight : tempTop;
            int tempBottom = h + cSize;
            int nbottom = (int) ((tempBottom >= imgSize.height - 1) ? imgSize.height - 1 : tempBottom);

            for (int w = 0; w < imgSize.width; w++) {
                int tempLeft = w - cSize;
                int nleft = (tempLeft > maxWidth) ? maxWidth : tempLeft;
                int tempRight = w + cSize;
                int nright = (int) ((tempRight >= imgSize.width - 1) ? imgSize.width - 1 : tempRight);

                // local mean
                double n1 = (nleft > 0 && ntop > 0) ? src.get(ntop, nleft - 1)[0] : 0;
                double n2 = (nleft > 0) ? src.get(nbottom, nleft - 1)[0] : 0;
                double n3 = (ntop > 0) ? src.get(ntop, nright)[0] : 0;

                double sum = src.get(nbottom, nright)[0] - n3 - n2 + n1;
                double mean = sum / nSize;

                if (mean > meanMax) {
                    meanMax = mean;
                    maxPoint.x = w;
                    maxPoint.y = h;
                }

                smoothMap.put(h, w, (mean > 255) ? 255 : mean);
            }
        }

        return maxPoint;
    }

}

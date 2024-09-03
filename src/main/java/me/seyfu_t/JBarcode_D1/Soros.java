package me.seyfu_t.JBarcode_D1;

import java.util.Optional;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Soros {
    double gmask[][] = { { 0.0071, 0.0071, 0.0143, 0.0143, 0.0143, 0.0071, 0.0071 },
            { 0.0071, 0.0143, 0.0143, 0.0286, 0.0143, 0.0143, 0.0071 },
            { 0.0143, 0.0143, 0.0286, 0.0571, 0.0286, 0.0143, 0.0143 },
            { 0.0143, 0.0286, 0.0571, 0.1143, 0.0571, 0.0281, 0.0143 },
            { 0.0143, 0.0143, 0.0286, 0.0571, 0.0286, 0.0143, 0.0143 },
            { 0.0071, 0.0143, 0.0143, 0.0286, 0.0143, 0.0143, 0.0071 },
            { 0.0071, 0.0071, 0.0143, 0.0143, 0.0143, 0.0071, 0.0071 } };

    public Optional<Rect> process(Mat graySrc, boolean is1D, int winSize) {
        Mat saliencyMap = saliencyMapByAndoMatrix(graySrc, is1D);

        Mat integralMap = calcIntegralImage(saliencyMap);
        
        Mat smoothMap = new Mat(saliencyMap.size(), CvType.CV_8UC1);
        Point maxPoint = findMaxPointWithSmooth(integralMap, smoothMap, winSize);

        Mat bMap = new Mat(smoothMap.size(), CvType.CV_8UC1);
        Imgproc.threshold(smoothMap, bMap, 50, 255, Imgproc.THRESH_OTSU);

        Rect result = boxDetection(saliencyMap, maxPoint);
        return Optional.of(result);
    }

    private Mat calcIntegralImage(Mat src) {
        Mat integralImage = new Mat();
        Imgproc.integral(src, integralImage);
        return integralImage;
    }

    private Mat saliencyMapByAndoMatrix(Mat src, boolean is1D) {
        Size imgSize = src.size();
        Mat result = new Mat(imgSize, CvType.CV_8UC1);

        int size = (int) (imgSize.width * imgSize.height);
        double[] Ixx = new double[size], Ixy = new double[size], Iyy = new double[size], Cxx = new double[size],
                Cxy = new double[size], Cyy = new double[size];

        // edge by sobel
        for (int h = 1; h < imgSize.height - 1; h++) {
            for (int w = 1; w < imgSize.width - 1; w++) {
                double dx = src.get(h - 1, w - 1)[0] + 2.0f * src.get(h, w - 1)[0] + src.get(h + 1, w - 1)[0]
                        - src.get(h - 1, w + 1)[0] - 2.0f * src.get(h, w + 1)[0] - src.get(h + 1, w + 1)[0];

                double dy = src.get(h - 1, w - 1)[0] + 2.0f * src.get(h - 1, w)[0] + src.get(h - 1, w + 1)[0] -
                        src.get(h + 1, w - 1)[0] - 2.0f * src.get(h + 1, w)[0] - src.get(h + 1, w + 1)[0];

                Ixx[(int) (h * imgSize.width + w)] = dx * dx;
                Ixy[(int) (h * imgSize.width + w)] = dx * dy;
                Iyy[(int) (h * imgSize.width + w)] = dy * dy;
            }
        }

        // apply gaussian window function
        for (int h = 1; h < imgSize.height - 1; h++) {
            for (int w = 1; w < imgSize.width - 1; w++) {
                double C1 = 0;
                double C2 = 0;
                double C3 = 0;

                for (int m = 0; m < 7; m++) {
                    int s = h + m - 4;
                    if (s < 0)
                        continue;
                    for (int n = 0; n < 7; n++) {
                        int k = w + n - 4;
                        if (k < 0)
                            continue;
                        C1 += Ixx[(int) (s * imgSize.width + k)] * gmask[m][n];
                        C2 += Ixy[(int) (s * imgSize.width + k)] * gmask[m][n];
                        C3 += Iyy[(int) (s * imgSize.width + k)] * gmask[m][n];
                    }
                }

                Cxx[(int) (h * imgSize.width + w)] = C1;
                Cxy[(int) (h * imgSize.width + w)] = C2;
                Cyy[(int) (h * imgSize.width + w)] = C3;
            }
        }

        // edge or corner map
        for (int h = 1; h < imgSize.height - 1; h++) {
            for (int w = 1; w < imgSize.width - 1; w++) {
                double Txx = Cxx[(int) (h * imgSize.width + w)];
                double Txy = Cxy[(int) (h * imgSize.width + w)];
                double Tyy = Cyy[(int) (h * imgSize.width + w)];

                double m = 0;
                if (is1D) {
                    m = ((Txx - Tyy) * (Txx - Tyy) + 4 * (Txy * Txy)) / ((Txx + Tyy) * (Txx + Tyy) + 10000);
                } else {
                    m = (4 * (Txx * Tyy - (Txy * Txy))) / ((Txx + Tyy) * (Txx + Tyy) + 10000);
                }

                m *= 255.0;

                result.put(h, w, (m > 255) ? 255 : m);
            }
        }

        return result;
    }

    private Point findMaxPointWithSmooth(Mat src, Mat smoothMap, int winSize) {
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

    private Rect boxDetection(Mat src, Point cp) {
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
}

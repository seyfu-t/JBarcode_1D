package me.seyfu_t.JBarcode_1D.algorithms;

import java.util.Optional;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import me.seyfu_t.JBarcode_1D.utils.MatUtils;

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

        Mat integralMap = MatUtils.calcIntegralImage(saliencyMap);

        Mat smoothMap = new Mat(saliencyMap.size(), CvType.CV_8UC1);
        Point maxPoint = MatUtils.findMaxPointWithSmooth(integralMap, smoothMap, winSize);

        Mat binaryMap = new Mat(smoothMap.size(), CvType.CV_8UC1);
        Imgproc.threshold(smoothMap, binaryMap, 50, 255, Imgproc.THRESH_OTSU);

        Rect result = MatUtils.boxDetection(saliencyMap, maxPoint);
        return Optional.of(result);
    }

    private Mat saliencyMapByAndoMatrix(Mat src, boolean is1D) {
        Size imgSize = src.size();
        Mat result = new Mat(imgSize, CvType.CV_8UC1);

        int width = (int) imgSize.width;
        int height = (int) imgSize.height;
        int size = width * height;

        double[] Ixx = new double[size];
        double[] Ixy = new double[size];
        double[] Iyy = new double[size];
        double[] Cxx = new double[size];
        double[] Cxy = new double[size];
        double[] Cyy = new double[size];

        // edge by sobel
        for (int h = 1; h < height - 1; h++) {
            for (int w = 1; w < width - 1; w++) {
                int index = h * width + w;

                double dx = src.get(h - 1, w - 1)[0] + 2.0 * src.get(h, w - 1)[0] + src.get(h + 1, w - 1)[0]
                        - src.get(h - 1, w + 1)[0] - 2.0 * src.get(h, w + 1)[0] - src.get(h + 1, w + 1)[0];

                double dy = src.get(h - 1, w - 1)[0] + 2.0 * src.get(h - 1, w)[0] + src.get(h - 1, w + 1)[0]
                        - src.get(h + 1, w - 1)[0] - 2.0 * src.get(h + 1, w)[0] - src.get(h + 1, w + 1)[0];

                Ixx[index] = dx * dx;
                Ixy[index] = dx * dy;
                Iyy[index] = dy * dy;
            }
        }

        // apply gaussian window function
        for (int h = 1; h < height - 1; h++) {
            for (int w = 1; w < width - 1; w++) {
                int index = h * width + w;
                double C1 = 0;
                double C2 = 0;
                double C3 = 0;

                for (int m = 0; m < 7; m++) {
                    int s = h + m - 4;
                    if (s < 0 || s >= height)
                        continue;
                    for (int n = 0; n < 7; n++) {
                        int k = w + n - 4;
                        if (k < 0 || k >= width)
                            continue;
                        int innerIndex = s * width + k;

                        C1 += Ixx[innerIndex] * gmask[m][n];
                        C2 += Ixy[innerIndex] * gmask[m][n];
                        C3 += Iyy[innerIndex] * gmask[m][n];
                    }
                }

                Cxx[index] = C1;
                Cxy[index] = C2;
                Cyy[index] = C3;
            }
        }

        // edge or corner map
        for (int h = 1; h < height - 1; h++) {
            for (int w = 1; w < width - 1; w++) {
                int index = h * width + w;

                double Txx = Cxx[index];
                double Txy = Cxy[index];
                double Tyy = Cyy[index];

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

}

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

}

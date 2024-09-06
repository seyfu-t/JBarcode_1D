package me.seyfu_t.JBarcode_1D.algorithms;

import java.util.Optional;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import me.seyfu_t.JBarcode_1D.utils.MatUtils;

public class Gallo {

    public Optional<Rect> process(Mat graySrc, int winSize) {
        Mat gradient = calcGradient(graySrc);

        Mat integralImage = MatUtils.calcIntegralImage(gradient);

        Mat smoothMap = new Mat(gradient.size(), CvType.CV_8UC1);
        Point maxPoint = MatUtils.findMaxPointWithSmooth(integralImage, smoothMap, winSize);

        Mat binaryMap = new Mat(gradient.size(), CvType.CV_8UC1);
        Imgproc.threshold(smoothMap, binaryMap, 50, 255, Imgproc.THRESH_OTSU);

        Rect result = MatUtils.boxDetection(binaryMap, maxPoint);
        return Optional.of(result);
    }

    private Mat calcGradient(Mat src) {
        Size imgSize = src.size();
        Mat result = new Mat(imgSize, CvType.CV_8UC1);

        for (int h = 1; h < imgSize.height - 1; h++) {
            for (int w = 1; w < imgSize.width - 1; w++) {
                int dx = (int) (src.get(h - 1, w - 1)[0] + 2 * src.get(h, w - 1)[0] + src.get(h + 1, w - 1)[0] -
                        src.get(h - 1, w + 1)[0] - 2 * src.get(h, w + 1)[0] - src.get(h + 1, w + 1)[0]);

                result.put(h, w, Math.abs(dx));
            }
        }

        return result;
    }

}

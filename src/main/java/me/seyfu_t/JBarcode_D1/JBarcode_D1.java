package me.seyfu_t.JBarcode_D1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import me.seyfu_t.JBarcode_D1.algorithms.Gallo;
import me.seyfu_t.JBarcode_D1.algorithms.Soros;
import me.seyfu_t.JBarcode_D1.algorithms.Yun;
import me.seyfu_t.JBarcode_D1.cli.CLIHandling;
import me.seyfu_t.JBarcode_D1.cli.CLIOptions;
import me.seyfu_t.JBarcode_D1.modell.YunCandidate;
import nu.pattern.OpenCV;

public class JBarcode_D1 {

    static {
        OpenCV.loadLocally();
    }

    // private static final Logger log = Logger.getLogger(Main.class.getName());
    public static final String PROGRAM_NAME = JBarcode_D1.class.getPackageName();

    // I don't know what this constant really does
    private static final int WIN_SIZE = 20; 

    private static final Logger log = Logger.getLogger(JBarcode_D1.class.getName());

    public static void main(String[] args) {

        CLIOptions cliOptions = CLIHandling.parseArguments(args);

        String fileName = cliOptions.getFilePath();

        File imgFile = new File(fileName);
        if(!imgFile.exists()){
            log.log(Level.SEVERE, "File does not seem to exist.");
            System.exit(1);
        }

        Mat frame = Imgcodecs.imread(fileName);
        if (frame.empty()) {
            log.log(Level.SEVERE, "Frame is empty. Does your file have content?");
            System.exit(1);
        }

        // Convert to grayscale
        Mat frameGray = matToGrayscaleMat(frame);

        
        Optional<Rect> galloRect = calcRectFromMatWithGallo(frameGray);
        Optional<Rect> sorosRect = calcRectFromMatWithSoros(frameGray);
        List<YunCandidate> yunCandidatesList = calcYunCandidatesFromMat(frameGray);

        
        if (cliOptions.getPreviewStatus()) { // show the actual image with rectangles drawn
            Mat resultFrame = frame.clone();

            // draw image
            galloRect.ifPresent(rect -> drawRectangle(resultFrame, rect, new Scalar(0, 255, 0)));
            sorosRect.ifPresent(rect -> drawRectangle(resultFrame, rect, new Scalar(255, 0, 0)));
            for (YunCandidate candidate : yunCandidatesList) {
                if (candidate.isBarcode())
                    drawRectangle(resultFrame, candidate.getRoi(), new Scalar(0, 255, 255));
            }

            // show image
            HighGui.imshow("Frame", resultFrame);
            HighGui.waitKey(0);
            HighGui.destroyAllWindows();
        }

    }

    public static List<YunCandidate> calcYunCandidatesFromFile(File file) {
        Optional<Mat> mat = fileToGrayscaleMat(file);
        if (mat.isEmpty())
            return new ArrayList<YunCandidate>();

        Mat gray = matToGrayscaleMat(mat.get());
        return calcYunCandidatesFromMat(gray);
    }

    public static List<YunCandidate> calcYunCandidatesFromMat(Mat grayMat) {
        return new Yun().process(grayMat);
    }

    public static Optional<Rect> calcRectFromFileWithSoros(File file) {
        Optional<Mat> mat = fileToGrayscaleMat(file);
        if (mat.isEmpty())
            return Optional.empty();

        Mat gray = matToGrayscaleMat(mat.get());
        return calcRectFromMatWithSoros(gray);
    }

    public static Optional<Rect> calcRectFromMatWithSoros(Mat grayMat) {
        return new Soros().process(grayMat, true, WIN_SIZE);
    }

    public static Optional<Rect> calcRectFromFileWithGallo(File file) {
        Optional<Mat> mat = fileToGrayscaleMat(file);
        if (mat.isEmpty())
            return Optional.empty();

        Mat gray = matToGrayscaleMat(mat.get());
        return calcRectFromMatWithGallo(gray);
    }

    public static Optional<Rect> calcRectFromMatWithGallo(Mat grayMat) {
        return new Gallo().process(grayMat, WIN_SIZE);
    }

    public static Optional<Mat> fileToGrayscaleMat(File file) {
        Mat frame = Imgcodecs.imread(file.getAbsolutePath());
        if (frame.empty())
            return Optional.empty();

        return Optional.of(matToGrayscaleMat(frame));
    }

    public static Mat matToGrayscaleMat(Mat src) {
        Mat frameGray = new Mat();
        Imgproc.cvtColor(src, frameGray, Imgproc.COLOR_BGR2GRAY);
        return frameGray;
    }

    private static void drawRectangle(Mat image, Rect rect, Scalar color) {
        Imgproc.rectangle(image, rect, color, 2);
    }
}

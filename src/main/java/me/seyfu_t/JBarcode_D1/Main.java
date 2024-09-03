package me.seyfu_t.JBarcode_D1;

import java.util.List;
import java.util.Optional;
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

public class Main {

    static {
        OpenCV.loadLocally();
    }

    private static final Logger log = Logger.getLogger(Main.class.getName());
    public static final String PROGRAM_NAME = Main.class.getPackageName();

    public static void main(String[] args) {

        CLIOptions cliOptions = CLIHandling.parseArguments(args);

        String fileName = cliOptions.getFilePath();
        // log.info("File: "+fileName);

        // Create instances of the processors
        Gallo gallo = new Gallo();
        Soros soros = new Soros();
        Yun yun = new Yun();

        // Read the image
        Mat frame = Imgcodecs.imread(fileName);
        if (frame.empty()) {
            System.err.println("Error! Could not read image.");
            return;
        }

        // Convert to grayscale
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        // Process the image with each processor
        Mat resultFrame = frame.clone();

        Optional<Rect> galloRect = gallo.process(frameGray, 20);
        Optional<Rect> sorosRect = soros.process(frameGray, true, 20);
        List<YunCandidate> yunCandidatesList = yun.process(frameGray);

        if (cliOptions.getPreviewStatus()) { // show the actual image with rectangles drawn
            // draw image
            galloRect.ifPresent(rect -> drawRectangle(resultFrame, rect, new Scalar(0, 255, 0)));
            sorosRect.ifPresent(rect -> drawRectangle(resultFrame, rect, new Scalar(255, 0, 0)));
            for(YunCandidate candidate : yunCandidatesList){
                if (candidate.isBarcode())
                    drawRectangle(resultFrame, candidate.getRoi(), new Scalar(0, 255, 255));
            }

            // show image
            HighGui.imshow("Frame", resultFrame);
            HighGui.waitKey(0);
            HighGui.destroyAllWindows();
        }

    }
    
    private static void drawRectangle(Mat image, Rect rect, Scalar color) {
        Imgproc.rectangle(image, rect, color, 2);
    }
}

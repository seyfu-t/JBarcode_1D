package me.seyfu_t.JBarcode_D1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import me.seyfu_t.JBarcode_D1.modell.YunCandidate;
import nu.pattern.OpenCV;

public class Main {

    static {
        OpenCV.loadLocally();
    }
    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Command-line argument parsing
        Map<String, String> arguments = parseArguments(args);

        if (arguments.containsKey("help") || !arguments.containsKey("file")) {
            printHelpMessage();
            return;
        }

        String fileName = arguments.get("file");
        log.info(fileName);

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

        if (arguments.containsKey("show")) { // show the actual image with rectangles drawn
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

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) {
                    arguments.put(parts[0], parts[1]);
                } else {
                    arguments.put(parts[0], "");
                }
            }
        }
        return arguments;
    }

    private static void printHelpMessage() {
        System.out.println("Usage: java Main --file=<file_path>");
        System.out.println("Options:");
        System.out.println("  --help         Print this help message");
        System.out.println("  --file=<path>  Path to the image file (.bmp, .jpg, .png)");
    }

    private static void drawRectangle(Mat image, Rect rect, Scalar color) {
        Imgproc.rectangle(image, rect, color, 2);
    }
}

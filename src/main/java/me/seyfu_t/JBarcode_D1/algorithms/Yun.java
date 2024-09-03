package me.seyfu_t.JBarcode_D1.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import me.seyfu_t.JBarcode_D1.modell.YunCandidate;
import me.seyfu_t.JBarcode_D1.modell.YunLabel;
import me.seyfu_t.JBarcode_D1.modell.YunOrientation;
import me.seyfu_t.JBarcode_D1.modell.YunParams;
import me.seyfu_t.JBarcode_D1.utils.MatUtils;

public class Yun {

    private YunParams params;

    public static final int NUM_ANG = 18;

    public Yun() {
        // Default parameter values
        this.params = new YunParams(30, 25, 30, 15, 0.3);
    }

    public List<YunCandidate> process(Mat graySrc) {
        List<YunCandidate> result;

        List<YunOrientation> vMap = new ArrayList<YunOrientation>();

        Mat mMap = new Mat(graySrc.size(), CvType.CV_8UC1);
        Mat orientationMap = calcOrientation(graySrc, mMap, vMap);

        Mat saliencyMap = calcSaliency(orientationMap, vMap, params.getLocalBlockSize());

        Mat integralMap = MatUtils.calcIntegralImage(saliencyMap);
        Mat smoothMap = calcSmooth(integralMap, params.getWinSize());

        Mat binaryMap = new Mat();
        Imgproc.threshold(smoothMap, binaryMap, 50, 255, Imgproc.THRESH_OTSU);

        List<YunLabel> blob = ccl(binaryMap, orientationMap, vMap);

        result = calcCandidate(blob, mMap, orientationMap);

        return result;
    }

    private Mat calcOrientation(Mat src, Mat mMap, List<YunOrientation> vMap) {
        Size imgSize = src.size();
        int extAng = 2 * NUM_ANG;

        Mat orientationMap = new Mat(imgSize, CvType.CV_8UC1);

        for (int i = 0; i < NUM_ANG; i++)
            vMap.add(new YunOrientation());

        for (int h = 1; h < imgSize.height - 1; h++) {
            for (int w = 1; w < imgSize.width - 1; w++) {

                double dx = src.get(h - 1, w - 1)[0] + 2.0 * src.get(h, w - 1)[0] + src.get(h + 1, w - 1)[0] -
                        src.get(h - 1, w + 1)[0] - 2.0 * src.get(h, w + 1)[0] - src.get(h + 1, w + 1)[0];

                double dy = src.get(h - 1, w - 1)[0] + 2.0 * src.get(h - 1, w)[0] + src.get(h - 1, w + 1)[0] -
                        src.get(h + 1, w - 1)[0] - 2.0 * src.get(h + 1, w)[0] - src.get(h + 1, w + 1)[0];

                int intensity = (int) Math.sqrt(dx * dx + dy * dy);
                mMap.put(h, w, intensity > 255 ? 255 : intensity);

                if (intensity > params.getMagT()) {
                    double degree = Math.toDegrees(Math.atan2(dy, dx)) + 180.0; // 0~360;

                    // bin
                    int bin = (int) (extAng * (degree / 360.0));
                    if (bin < 0)
                        bin = 0;
                    else if (bin >= extAng)
                        bin = extAng - 1;

                    // integration
                    if (bin > 17)
                        bin -= 18;

                    // plus integration (not include paper)
                    if (bin > 16 || bin < 2)
                        bin = 0;
                    else if (bin < 5)
                        bin = 3;
                    else if (bin < 8)
                        bin = 6;
                    else if (bin < 11)
                        bin = 9;
                    else if (bin < 14)
                        bin = 12;
                    else
                        bin = 15;

                    // save
                    orientationMap.put(h, w, bin);
                    YunOrientation cand = vMap.get(bin);
                    cand.setCnt(cand.getCnt() + 1);
                } else {
                    orientationMap.put(h, w, 255);
                }
            }
        }

        // check orientation
        for (int i = 0; i < NUM_ANG; i++) {
            if (vMap.get(i).getCnt() > 6000)
                vMap.get(i).setStrong(true);
            else
                vMap.get(i).setStrong(false);
        }

        return orientationMap;
    }

    private Mat calcSaliency(Mat src, List<YunOrientation> vMap, int localBlockSize) {
        Size imgSize = src.size();

        Mat saliencyMap = new Mat(imgSize, CvType.CV_8UC1);
        saliencyMap.setTo(new Scalar(0));

        int nMax = localBlockSize * localBlockSize * NUM_ANG;
        int cBlock = (int) (localBlockSize / 2 + 1);

        for (int h = cBlock; h < imgSize.height - cBlock; h += localBlockSize) {
            for (int w = cBlock; w < imgSize.width - cBlock; w += localBlockSize) {

                // step 1 local block histogram (orientation)
                int[] LocalHisto = new int[NUM_ANG];
                for (int y = h - cBlock; y <= h + cBlock; y++) {
                    if (y < 0 || y >= imgSize.height)
                        continue;
                    for (int x = w - cBlock; x <= w + cBlock; x++) {
                        if (x < 0 || x >= imgSize.width)
                            continue;

                        int bin = (int) src.get(y, x)[0];
                        if (bin >= NUM_ANG)
                            continue;

                        LocalHisto[bin]++;
                    }
                }

                // step 2 find max values
                int max_val = 0;
                for (int i = 0; i < NUM_ANG; i++) {
                    if (LocalHisto[i] > max_val)
                        max_val = LocalHisto[i];
                }

                // step 3 entropy
                double pim = 0;
                for (int i = 0; i < NUM_ANG; i++) {
                    pim += Math.abs(LocalHisto[i] - max_val);
                }

                // step 4 check max value
                if (max_val == 0)
                    continue;

                // step 5 normalization
                double npim = pim / nMax;
                int ramp_npim = (int) ((npim * 255) > 255 ? 255 : (npim * 255));

                if (npim < 0.6)
                    ramp_npim = 0;

                // step 6 set block
                for (int y = h - cBlock; y <= h + cBlock; y++) {
                    if (y < 0 || y >= imgSize.height)
                        continue;
                    for (int x = w - cBlock; x <= w + cBlock; x++) {
                        if (x < 0 || x >= imgSize.width)
                            continue;
                        saliencyMap.put(y, x, ramp_npim);
                    }
                }
            }
        }

        return saliencyMap;
    }

    private Mat calcSmooth(Mat src, int winSize) {
        Size imgSize = src.size();

        int nSize = winSize * winSize;
        int cSize = (int) (winSize / 2 + 1);
        int maxHeight = (int) imgSize.height - cSize;
        int maxWidth = (int) imgSize.width - cSize;

        Mat smoothMap = new Mat(imgSize, CvType.CV_8UC1);
        for (int h = 0; h < imgSize.height; h++) {
            int temp_top = h - cSize;
            int ntop = (temp_top > maxHeight) ? maxHeight : temp_top;
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

                smoothMap.put(h, w, (mean > 255) ? 255 : mean);
            }
        }
        return smoothMap;
    }

    private static int push(int[] stackx, int[] stacky, int arr_size, int vx, int vy, int[] top) {
        if (top[0] >= arr_size)
            return -1;
        top[0]++;
        stackx[top[0]] = vx;
        stacky[top[0]] = vy;
        return 1;
    }

    private static int pop(int[] stackx, int[] stacky, int arr_size, int[] vx, int[] vy, int[] top) {
        if (top[0] == 0)
            return -1;
        vx[0] = stackx[top[0]];
        vy[0] = stacky[top[0]];
        top[0]--;
        return 1;
    }

    public static List<YunLabel> ccl(Mat src, Mat oMap, List<YunOrientation> Vmap) {
        List<YunLabel> result = new ArrayList<>();
        Size imgSize = src.size();
        Mat mask = new Mat(imgSize, CvType.CV_8UC1);
        mask.setTo(new Scalar(0));

        int[] stackx = new int[(int) (imgSize.width * imgSize.height)];
        int[] stacky = new int[(int) (imgSize.width * imgSize.height)];
        Arrays.fill(stackx, 0);
        Arrays.fill(stacky, 0);
        int tsize = (int) (imgSize.width * imgSize.height);
        int[] r = new int[1], c = new int[1], top = new int[1], label_id = { 0 };
        Rect rect = new Rect();

        for (int h = 1; h < imgSize.height - 2; h++) {
            for (int w = 1; w < imgSize.width - 2; w++) {
                if (mask.get(h, w)[0] != 0 || src.get(h, w)[0] < 128)
                    continue;

                r[0] = h;
                c[0] = w;
                rect = new Rect(w, h, 0, 0);
                top[0] = 0;
                label_id[0]++;
                if (label_id[0] > 255)
                    label_id[0] = 1;
                int[] hist = new int[NUM_ANG];

                while (true) {
                    boolean pushed = false;
                    for (int m = r[0] - 1; m <= r[0] + 1; m++) {
                        for (int n = c[0] - 1; n <= c[0] + 1; n++) {
                            if (m < 0 || m >= imgSize.height || n < 0 || n >= imgSize.width)
                                continue;
                            if (mask.get(m, n)[0] != 0 || src.get(m, n)[0] < 128)
                                continue;
                            int bin = (int) oMap.get(m, n)[0];
                            if (bin >= NUM_ANG)
                                continue;
                            mask.put(m, n, label_id[0]);
                            hist[bin]++;
                            if (push(stackx, stacky, tsize, m, n, top) == -1)
                                continue;
                            r[0] = m;
                            c[0] = n;
                            rect.x = Math.min(rect.x, c[0]);
                            rect.y = Math.min(rect.y, r[0]);
                            rect.width = Math.max(rect.width, c[0]);
                            rect.height = Math.max(rect.height, r[0]);
                            pushed = true;
                            break;
                        }
                        if (pushed)
                            break;
                    }
                    if (!pushed) {
                        if (pop(stackx, stacky, tsize, r, c, top) == -1) {
                            int width = rect.width - rect.x + 1;
                            int height = rect.height - rect.y + 1;
                            if (width > 15 && height > 15) {
                                YunLabel val = new YunLabel();
                                val.setRoi(new Rect(rect.x, rect.y, width, height));
                                int max_val = 0;
                                int ori = 255;
                                for (int i = 0; i < NUM_ANG; i++) {
                                    if (max_val < hist[i]) {
                                        max_val = hist[i];
                                        ori = i;
                                    }
                                }
                                val.setMaxOrientation(ori);
                                if (Vmap.get(ori).isStrong()) {
                                    result.add(val);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<YunCandidate> calcCandidate(List<YunLabel> labels, Mat mMap, Mat oMap) {
        List<YunCandidate> result = new ArrayList<>();

        for (YunLabel label : labels) {
            YunCandidate tmp = subCandidate(label, mMap, oMap);
            if (tmp.isBarcode()) {
                YunCandidate newTmp = calcRegionCheck(tmp, mMap.size());

                if (result.isEmpty()) {
                    result.add(newTmp);
                } else {
                    boolean isSave = true;

                    Point st = new Point(newTmp.getRoi().x, newTmp.getRoi().y);
                    Point et = new Point(newTmp.getRoi().x + newTmp.getRoi().width,
                            newTmp.getRoi().y + newTmp.getRoi().height);

                    for (YunCandidate existingCandidate : result) {
                        Rect existingRoi = existingCandidate.getRoi();
                        Point rst = new Point(existingRoi.x, existingRoi.y);
                        Point ret = new Point(existingRoi.x + existingRoi.width, existingRoi.y + existingRoi.height);

                        if (checkOverlap(st, et, rst, ret)) {
                            adjustRoi(existingCandidate, st, et);
                            isSave = false;
                            break;
                        }
                    }

                    if (isSave)
                        result.add(newTmp);
                }
            }
        }

        return result;
    }

    private boolean checkOverlap(Point st, Point et, Point rst, Point ret) {
        return (et.x >= rst.x && et.x <= ret.x && st.y >= rst.y && st.y <= ret.y) ||
                (st.x >= rst.x && st.x <= ret.x && st.y >= rst.y && st.y <= ret.y) ||
                (et.x >= rst.x && et.x <= ret.x && et.y >= rst.y && et.y <= ret.y) ||
                (st.x >= rst.x && st.x <= ret.x && et.y >= rst.y && et.y <= ret.y) ||
                (rst.x >= st.x && rst.x <= et.x && rst.y >= st.y && rst.y <= et.y) ||
                (ret.x >= st.x && ret.x <= et.x && rst.y >= st.y && rst.y <= et.y) ||
                (rst.x >= st.x && rst.x <= et.x && ret.y >= st.y && ret.y <= et.y) ||
                (ret.x >= st.x && ret.x <= et.x && ret.y >= st.y && ret.y <= et.y);
    }

    private void adjustRoi(YunCandidate existingCandidate, Point st, Point et) {
        Rect roi = existingCandidate.getRoi();
        if (st.x <= roi.x)
            roi.x = (int) st.x;
        if (et.x >= roi.width)
            roi.width = (int) et.x;
        else
            roi.width = roi.width + roi.x;
        if (st.y <= roi.y)
            roi.y = (int) st.y;
        if (et.y >= roi.height)
            roi.height = (int) et.y;
        else
            roi.height = roi.height + roi.y;

        roi.width -= roi.x;
        roi.height -= roi.y;
        existingCandidate.setRoi(roi);
    }

    private YunCandidate subCandidate(YunLabel val, Mat mMap, Mat oMap) {
        YunCandidate result = new YunCandidate();

        Rect roi = val.getRoi();
        Size imgSize = mMap.size();

        // Center point
        Point cPt = new Point(roi.x + roi.width / 2.0, roi.y + roi.height / 2.0);

        // Direction check
        double theta = (Math.PI / NUM_ANG) * val.getMaxOrientation();

        // Step
        Point step = new Point(Math.cos(theta), Math.sin(theta));

        Rect limitArea = new Rect(10, 10, (int) imgSize.width - 20, (int) imgSize.height - 20);
        Rect imgRect = limitArea.clone();

        int nEdge = 0;
        result.setRoi(roi);
        result.setOrientation(val.getMaxOrientation());

        for (int dir = 0; dir < 2; dir++) {
            int dist = 0;
            Point curPt = new Point(cPt.x, cPt.y);
            Point lastEdge = new Point(curPt.x, curPt.y);

            if (dir == 1)
                step = new Point(-step.x, -step.y);

            while (imgRect.contains(curPt)) {
                curPt = new Point(curPt.x + step.x, curPt.y + step.y);

                if (mMap.get((int) curPt.y, (int) curPt.x)[0] > params.getMagT()) {
                    if (oMap.get((int) curPt.y, (int) curPt.x)[0] == val.getMaxOrientation()) {
                        lastEdge = new Point(curPt.x, curPt.y);
                        dist = 0;
                        nEdge++;
                    } else if (nEdge > 0) {
                        dist++;
                        nEdge--;
                    }
                } else if (nEdge > 0) {
                    dist++;
                }

                if (dist > 7) {
                    if (dir == 1) {
                        result.setLastPt(lastEdge);
                    } else {
                        result.setFirstPt(lastEdge);
                    }
                    break;
                }
            }

            if (dir == 0 && result.getFirstPt() == null) {
                result.setFirstPt(lastEdge);
            } else if (dir == 1 && result.getLastPt() == null) {
                result.setLastPt(lastEdge);
            }
        }

        int edgeDensity = (int) Math.sqrt(Math.pow(result.getFirstPt().x - result.getLastPt().x, 2) +
                Math.pow(result.getFirstPt().y - result.getLastPt().y, 2));

        if (nEdge > Math.max(params.getMinEdgeT(), (int) (edgeDensity * params.getMinDensityEdgeT()))) {
            result.setBarcode(true);
        } else {
            result.setBarcode(false);
        }

        return result;
    }

    private YunCandidate calcRegionCheck(YunCandidate val, Size imgSize) {
        YunCandidate newVal = val;

        int margin = 1;

        Point st = new Point(val.getRoi().x, val.getRoi().y);
        Point et = new Point(val.getRoi().x + val.getRoi().width, val.getRoi().y + val.getRoi().height);

        // Start point adjustments
        newVal.getRoi().y = (int) (Math.max(0,
                Math.min(Math.min(st.y, val.getLastPt().y) - margin, Math.min(st.y, val.getFirstPt().y) - margin)));
        newVal.getRoi().x = (int) (Math.max(0,
                Math.min(Math.min(st.x, val.getLastPt().x) - margin, Math.min(st.x, val.getFirstPt().x) - margin)));

        // End point adjustments
        newVal.getRoi().height = (int) (Math.min(imgSize.height - 1,
                Math.max(Math.max(et.y, val.getLastPt().y) + margin, Math.max(et.y, val.getFirstPt().y) + margin)));
        newVal.getRoi().width = (int) (Math.min(imgSize.width - 1,
                Math.max(Math.max(et.x, val.getLastPt().x) + margin, Math.max(et.x, val.getFirstPt().x) + margin)));

        newVal.getRoi().width -= newVal.getRoi().x;
        newVal.getRoi().height -= newVal.getRoi().y;

        return newVal;
    }

}

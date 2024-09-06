package me.seyfu_t.JBarcode_1D.modell;

import org.opencv.core.Point;
import org.opencv.core.Rect;

public class YunCandidate {
    private Point lastPt, firstPt;
    private Rect roi;
    private int orientation;
    private boolean isBarcode;

    public Point getFirstPt() {
        return firstPt;
    }

    public Point getLastPt() {
        return lastPt;
    }

    public int getOrientation() {
        return orientation;
    }

    public Rect getRoi() {
        return roi;
    }

    public boolean isBarcode() {
        return isBarcode;
    }

    public void setBarcode(boolean isBarcode) {
        this.isBarcode = isBarcode;
    }

    public void setFirstPt(Point firstPt) {
        this.firstPt = firstPt;
    }

    public void setLastPt(Point lastPt) {
        this.lastPt = lastPt;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setRoi(Rect roi) {
        this.roi = roi;
    }
}

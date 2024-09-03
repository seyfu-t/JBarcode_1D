package me.seyfu_t.JBarcode_D1.modell;

import org.opencv.core.Rect;

public class YunLabel {
    private Rect roi;
    private int maxOrientation;

    public int getMaxOrientation() {
        return maxOrientation;
    }
    public Rect getRoi() {
        return roi;
    }
    public void setMaxOrientation(int maxOrientation) {
        this.maxOrientation = maxOrientation;
    }
    public void setRoi(Rect roi) {
        this.roi = roi;
    }
}

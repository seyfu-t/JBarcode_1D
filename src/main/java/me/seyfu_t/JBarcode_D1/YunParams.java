package me.seyfu_t.JBarcode_D1;

public class YunParams {
    private int magT;
    private int winSize;
    private int minEdgeT;
    private int localBlockSize;
    private double minDensityEdgeT;

    public YunParams(int magT, int winSize, int minEdgeT, int localBlockSize, double minDensityEdgeT) {
        this.magT = magT;
        this.winSize = winSize;
        this.minEdgeT = minEdgeT;
        this.localBlockSize = localBlockSize;
        this.minDensityEdgeT = minDensityEdgeT;
    }

    public int getLocalBlockSize() {
        return localBlockSize;
    }

    public int getMagT() {
        return magT;
    }

    public double getMinDensityEdgeT() {
        return minDensityEdgeT;
    }

    public int getMinEdgeT() {
        return minEdgeT;
    }

    public int getWinSize() {
        return winSize;
    }

    public void setLocalBlockSize(int localBlockSize) {
        this.localBlockSize = localBlockSize;
    }

    public void setMagT(int magT) {
        this.magT = magT;
    }

    public void setMinDensityEdgeT(double minDensityEdgeT) {
        this.minDensityEdgeT = minDensityEdgeT;
    }

    public void setMinEdgeT(int minEdgeT) {
        this.minEdgeT = minEdgeT;
    }

    public void setWinSize(int winSize) {
        this.winSize = winSize;
    }

}

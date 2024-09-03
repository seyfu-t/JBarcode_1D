package me.seyfu_t.JBarcode_D1.cli;

public class CLIOptions {
    private String filePath;
    private boolean preview;
    private boolean help;

    public String getFilePath() {
        return filePath;
    }

    public boolean getPreviewStatus() {
        return preview;
    }

    public boolean getHelpStatus() {
        return help;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPreviewStatus(boolean preview) {
        this.preview = preview;
    }

}

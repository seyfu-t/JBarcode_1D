package me.seyfu_t.JBarcode_D1.cli;

public enum CLIArguments {
    Preview("p", "preview", "Opens up a window showing a preview of the detection"),
    File("f", "file", "Path to the image file"),
    Help("h","help","See this help page")
    ;

    private final String shortArg;
    private final String longArg;
    private final String description;

    CLIArguments(String shortArg, String longArg, String description) {
        this.shortArg = shortArg;
        this.longArg = longArg;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getLongArg() {
        return longArg;
    }

    public String getShortArg() {
        return shortArg;
    }
}

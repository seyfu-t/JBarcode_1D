package me.seyfu_t.JBarcode_1D.cli;

public enum CLIArguments {
    PREVIEW("p", "preview", "Opens up a window showing a preview of the detection"),
    FILE("f", "file", "Path to the image file"),
    HELP("h","help","See this help page"),
    OUTPUT_FORMAT("o","output","Format in which the data shall be output. [csv/json/text]"),
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

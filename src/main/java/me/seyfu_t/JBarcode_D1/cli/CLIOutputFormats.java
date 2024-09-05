package me.seyfu_t.JBarcode_D1.cli;

public enum CLIOutputFormats {
    CSV("csv"),
    JSON("json"),
    TEXT("text");

    private final String name;

    CLIOutputFormats(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static boolean isValidFormat(String format) {
        for (CLIOutputFormats outputFormat : CLIOutputFormats.values())
            if (outputFormat.getName().equalsIgnoreCase(format))
                return true;
        return false;
    }
}

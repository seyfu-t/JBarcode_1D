package me.seyfu_t.JBarcode_D1.cli;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import me.seyfu_t.JBarcode_D1.JBarcode_D1;

public class CLIHandling {

    private static final Logger log = Logger.getLogger(CLIHandling.class.getName());

    public static CLIOptions parseArguments(String[] args) {
        Options opts = new Options();

        CLIArguments previewArg = CLIArguments.Preview;
        CLIArguments filePathArg = CLIArguments.File;
        CLIArguments helpArg = CLIArguments.Help;

        opts.addOption(previewArg.getShortArg(), previewArg.getLongArg(), false, previewArg.getDescription());
        opts.addOption(filePathArg.getShortArg(), filePathArg.getLongArg(), true, filePathArg.getDescription());
        opts.addOption(helpArg.getShortArg(), helpArg.getLongArg(), false, helpArg.getDescription());

        CommandLineParser parser = new DefaultParser();
        CLIOptions cliOptions = new CLIOptions();

        try {
            CommandLine cmdLine = parser.parse(opts, args);

            if(cmdLine.hasOption(helpArg.getShortArg())){
                new HelpFormatter().printHelp(JBarcode_D1.PROGRAM_NAME, opts);
                System.exit(0);
            }

            if (!cmdLine.hasOption(filePathArg.getShortArg())) {
                log.log(Level.SEVERE, "No file was provided. Use --file <file path>");
                System.exit(1);
            }

            String filePath = cmdLine.getOptionValue(filePathArg.getLongArg());
            File file = new File(filePath);
            if (!file.exists()) {
                log.log(Level.SEVERE, "File could not be found. Make sure the file exists. Maybe a permission issue?");
                System.exit(1);
            }

            cliOptions.setFilePath(filePath);
            cliOptions.setPreviewStatus(cmdLine.hasOption(previewArg.getShortArg()));

        } catch (ParseException e) {
            log.log(Level.SEVERE, "Parsing cli arguments failed. Error Message: " + e.getMessage());
            System.exit(1);
        }

        return cliOptions;
    }

}

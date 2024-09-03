# JBarcode_D1 (WIP)

### A Java Reimplementation and Extension of [iyyun/Barcode_1D](https://github.com/iyyun/Barcode_1D)

This project is a Java reimplementation and extension of [@iyyun](https://github.com/iyyun)'s [Barcode_1D](https://github.com/iyyun/Barcode_1D). It aims to replicate the original functionality as closely as possible while also providing additional features and a more user-friendly interface. The project can be used as a standalone CLI tool or integrated as a Maven library in other projects.

## This repo is a work-in-progress and so is this readme file

## Features

- **Gallo Processor**: Detects specific regions in an image.
- **Soros Processor**: Identifies salient regions in an image.
- **Yun Processor**: Locates potential barcodes within an image.

## Requirements

- Java 22 (Java 8 probably just works fine)
- OpenCV 4.x or higher

## Installation

### Maven

To include JBarcode_D1 in your Maven project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>me.seyfu_t</groupId>
    <artifactId>JBarcode_D1</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Command Line Interface (CLI)

You can also use JBarcode_D1 directly from the command line by running the JAR file.

## Usage

### CLI Usage

After building the project, you can run it via the command line using the following command:

```sh
java -jar target/JBarcode_D1-1.0-SNAPSHOT-jar-with-dependencies.jar --file <image_path> [--preview]
```

### Command Line Options

- `--file <image_path>` (or `-f <image_path>`): Specifies the path to the image file to be processed. This option is **required**.
- `--preview` (or `-p`): Displays the image with detected rectangles drawn on it. This option is **optional**.
- `--help` (or `-h`): Displays the help message, listing all available options.


## Contribution

Contributions are welcome! If you find any issues or have suggestions for improvements, feel free to open an issue or submit a pull request.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for more details.

# JBarcode_1D (WIP)

### A Java Reimplementation and Extension of [iyyun/Barcode_1D](https://github.com/iyyun/Barcode_1D)

This project is a Java reimplementation and extension of [@iyyun](https://github.com/iyyun)'s [Barcode_1D](https://github.com/iyyun/Barcode_1D). It aims to replicate the original functionality as closely as possible while also providing additional features and a more user-friendly interface. The project can be used as a standalone CLI tool or integrated as a Maven library in other projects.

## This repo is a work-in-progress and so is this readme file

## Supported file formats

Any filetype that is supported by `Imgcodecs.imread(String filepath)` is supported.

According to the docs of OpenCV 4.9's maven package these are:

- Windows bitmaps - \*.bmp, \*.dib (always supported)
- JPEG files - \*.jpeg, \*.jpg, \*.jpe (see the *Note* section)
- JPEG 2000 files - \*.jp2 (see the *Note* section)
- Portable Network Graphics - \*.png (see the *Note* section)
- WebP - \*.webp (see the *Note* section)
- AVIF - \*.avif (see the *Note* section)
- Portable image format - \*.pbm, \*.pgm, \*.ppm \*.pxm, \*.pnm (always supported)
- PFM files - \*.pfm (see the *Note* section)
- Sun rasters - \*.sr, \*.ras (always supported)
- TIFF files - \*.tiff, \*.tif (see the *Note* section)
- OpenEXR Image files - \*.exr (see the *Note* section)
- Radiance HDR - \*.hdr, \*.pic (always supported)
- Raster and Vector geospatial data supported by GDAL (see the *Note* section)

#### Note:

- The function determines the type of an image by the content, not by the file extension.

- In the case of color images, the decoded images will have the channels stored in B G R order.

- When using IMREAD_GRAYSCALE, the codec's internal grayscale conversion will be used, if available. Results may differ to the output of cvtColor()

- On Microsoft Windows\* OS and MacOSX\*, the codecs shipped with an OpenCV image (libjpeg, libpng, libtiff, and libjasper) are used by default. So, OpenCV can always read JPEGs, PNGs, and TIFFs. On MacOSX, there is also an option to use native MacOSX image readers. But beware that currently these native image loaders give images with different pixel values because of the color management embedded into MacOSX.

- On Linux\*, BSD flavors and other Unix-like open-source operating systems, OpenCV looks for codecs supplied with an OS image. Install the relevant packages (do not forget the development files, for example, "libjpeg-dev", in Debian\* and Ubuntu\*) to get the codec support or turn on the OPENCV_BUILD_3RDPARTY_LIBS flag in CMake.

- In the case you set *WITH_GDAL* flag to true in CMake and REF: IMREAD_LOAD_GDAL to load the image, then the [GDAL](http://www.gdal.org) driver will be used in order to decode the image, supporting the following formats: [Raster](http://www.gdal.org/formats_list.html), [Vector](http://www.gdal.org/ogr_formats.html).

- If EXIF information is embedded in the image file, the EXIF orientation will be taken into account and thus the image will be rotated accordingly except if the flags REF: IMREAD_IGNORE_ORIENTATION or REF: IMREAD_UNCHANGED are passed.

- Use the IMREAD_UNCHANGED flag to keep the floating point values from PFM image.

- By default number of pixels must be less than 2^30. Limit can be set using system variable OPENCV_IO_MAX_IMAGE_PIXELS

## Requirements

- Java 22 (Java 8 probably just works fine)
- OpenCV 4.x or higher

## Installation
WIP


### Command Line Interface (CLI)

You can also use JBarcode_1D directly from the command line by running the JAR file.

## Usage

### CLI Usage

After building the project, you can run it via the command line using the following command:

```sh
java -jar target/JBarcode_1D-1.0-SNAPSHOT-jar-with-dependencies.jar --file <image_path> [--preview]
```

### Command Line Options

- `--file <image_path>` (or `-f <image_path>`): Specifies the path to the image file to be processed. This option is **required**.
- `--preview` (or `-p`): Displays the image with detected rectangles drawn on it. This option is **optional**.
- `--help` (or `-h`): Displays the help message, listing all available options.


## Contribution

Contributions are welcome! If you find any issues or have suggestions for improvements, feel free to open an issue or submit a pull request.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for more details.

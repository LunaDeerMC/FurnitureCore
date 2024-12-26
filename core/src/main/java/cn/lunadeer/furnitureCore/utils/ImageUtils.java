package cn.lunadeer.furnitureCore.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    /**
     * Load an image from a file.
     *
     * @param imageFile The file to load the image from.
     * @return The loaded image.
     * @throws IOException If an error occurs while reading the image.
     */
    public static BufferedImage loadImage(File imageFile) throws IOException {
        return ImageIO.read(imageFile);
    }

    /**
     * Load an image from a file.
     *
     * @param imagePath The path to the image file.
     * @return The loaded image.
     * @throws IOException If an error occurs while reading the image.
     */
    public static BufferedImage loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        return ImageIO.read(imageFile);
    }

    /**
     * Save an image to a file.
     *
     * @param image      The image to save.
     * @param outputFile The file to save the image to.
     * @param format     The format of the image.
     * @throws IOException If an error occurs while writing the image.
     */
    public static void saveImage(BufferedImage image, File outputFile, String format) throws IOException {
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new IOException("Failed to create parent directory: %s".formatted(outputFile.getParentFile()));
        }
        ImageIO.write(image, format, outputFile);
    }

    /**
     * Save an image to a file.
     *
     * @param image      The image to save.
     * @param outputPath The path to save the image to.
     * @param format     The format of the image.
     * @throws IOException If an error occurs while writing the image.
     */
    public static void saveImage(BufferedImage image, String outputPath, String format) throws IOException {
        File outputFile = new File(outputPath);
        saveImage(image, outputFile, format);
    }

}

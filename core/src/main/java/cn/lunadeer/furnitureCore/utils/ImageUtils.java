package cn.lunadeer.furnitureCore.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static BufferedImage loadImage(File imageFile) throws IOException {
        return ImageIO.read(imageFile);
    }

    public static BufferedImage loadImage(String imagePath) throws IOException {
        File imageFile = new File(imagePath);
        return ImageIO.read(imageFile);
    }

    public static void saveImage(BufferedImage image, File outputFile, String format) throws IOException {
        ImageIO.write(image, format, outputFile);
    }

    public static void saveImage(BufferedImage image, String outputPath, String format) throws IOException {
        File outputFile = new File(outputPath);
        ImageIO.write(image, format, outputFile);
    }

}

package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {
    public static BufferedImage resize(BufferedImage image, int width, int height){
        Image temp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    public static double colorDistance(Color color1, Color color2){
        int r = color1.getRed() - color2.getRed();
        int g = color1.getGreen() - color2.getGreen();
        int b = color1.getBlue() - color2.getBlue();
        return Math.sqrt(r*r + g*g + b*b);
    }
}

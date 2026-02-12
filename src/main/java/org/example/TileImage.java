package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
public class TileImage {

    private BufferedImage image;
    private Color averageColor;
    private int usageCount = 0;

    public TileImage(BufferedImage image) {
        this.image = image;
        this.averageColor = computeAverageColor(image);
    }

    public BufferedImage getImage() {
        return image;
    }

    public Color getAverageColor() {
        return averageColor;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void incrementUsage() {
        usageCount++;
    }

    private Color computeAverageColor(BufferedImage img) {
        long r = 0, g = 0, b = 0;
        int width = img.getWidth();
        int height = img.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = new Color(img.getRGB(x, y));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }

        int total = width * height;
        return new Color((int)(r / total), (int)(g / total), (int)(b / total));
    }
}

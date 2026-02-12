package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class MosaicGenerator {

    private static final int TILE_SIZE = 60;
    private static final int TARGET_HEIGHT = 5000;
    private static final int MAX_USAGE = 30;
    private static final float OVERLAY_OPACITY = 0.17f;

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        // ----------------------------
        // Load & Resize Main Image
        // ----------------------------
        BufferedImage originalMain = ImageIO.read(new File("mainImage.png"));

        if (originalMain == null) {
            throw new RuntimeException("Main image not found!");
        }

        double scale = (double) TARGET_HEIGHT / originalMain.getHeight();
        int targetWidth = (int) (originalMain.getWidth() * scale);

        BufferedImage mainImage =
                ImageUtils.resize(originalMain, targetWidth, TARGET_HEIGHT);

        System.out.println("Main image resized to: " +
                targetWidth + " x " + TARGET_HEIGHT);

        int width = mainImage.getWidth();
        int height = mainImage.getHeight();

        // ----------------------------
        // Load Tiles
        // ----------------------------
        List<TileImage> tiles = loadTiles("src/main/java/org/example/tiles");

        BufferedImage mosaicImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = mosaicImage.createGraphics();

        // ----------------------------
        // Prepare Block List (Randomized)
        // ----------------------------
        List<Point> blocks = new ArrayList<>();

        for (int y = 0; y < height; y += TILE_SIZE) {
            for (int x = 0; x < width; x += TILE_SIZE) {
                blocks.add(new Point(x, y));
            }
        }

        Collections.shuffle(blocks);

        int totalBlocks = blocks.size();
        int processed = 0;
        int lastPercent = -1;

        System.out.println("Generating mosaic...");

        for (Point p : blocks) {

            int x = p.x;
            int y = p.y;

            BufferedImage subImage = mainImage.getSubimage(
                    x,
                    y,
                    Math.min(TILE_SIZE, width - x),
                    Math.min(TILE_SIZE, height - y)
            );

            Color avgColor = getAverageColor(subImage);

            TileImage bestMatch = findBestMatch(avgColor, tiles);

            if (bestMatch != null) {
                g2d.drawImage(bestMatch.getImage(), x, y, null);
            }

            processed++;
            int percent = (int) ((processed * 100.0) / totalBlocks);

            if (percent != lastPercent) {
                System.out.print("\rProcessing: " + percent + "% (" +
                        processed + "/" + totalBlocks + ")");
                lastPercent = percent;
            }
        }

        g2d.dispose();

        // ----------------------------
        // Overlay Original Image
        // ----------------------------
        Graphics2D overlay = mosaicImage.createGraphics();
        overlay.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        OVERLAY_OPACITY
                )
        );
        overlay.drawImage(mainImage, 0, 0, null);
        overlay.dispose();

        ImageIO.write(mosaicImage, "png", new File("output.png"));

        long endTime = System.currentTimeMillis();

        System.out.println("\nMosaic Completed!");
        System.out.println("Time Taken: " + (endTime - startTime) + " ms");
    }

    // -------------------------------------------------
    // Tile Loader (with resizing + progress)
    // -------------------------------------------------
    private static List<TileImage> loadTiles(String folderPath) throws Exception {

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Tiles folder not found: " +
                    folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            throw new RuntimeException("No images in tiles folder.");
        }

        List<TileImage> tiles = new ArrayList<>();

        int total = files.length;
        int processed = 0;
        int lastPercent = -1;

        System.out.println("Loading tiles...");

        for (File file : files) {

            BufferedImage img = ImageIO.read(file);

            if (img != null) {
                BufferedImage resized =
                        ImageUtils.resize(img, TILE_SIZE, TILE_SIZE);
                tiles.add(new TileImage(resized));
            }

            processed++;
            int percent = (int) ((processed * 100.0) / total);

            if (percent != lastPercent) {
                System.out.print("\rTiles Loaded: " + percent + "% (" +
                        processed + "/" + total + ")");
                lastPercent = percent;
            }
        }

        System.out.println("\nTiles Loaded Successfully!");

        return tiles;
    }

    // -------------------------------------------------
    // Matching Logic (Brightness + Usage Penalty)
    // -------------------------------------------------
    private static TileImage findBestMatch(Color target,
                                           List<TileImage> tiles) {

        TileImage bestMatch = null;
        double bestScore = Double.MAX_VALUE;

        double targetBrightness = getBrightness(target);

        // First pass: respect MAX_USAGE
        for (TileImage tile : tiles) {

            if (tile.getUsageCount() >= MAX_USAGE) continue;

            double score = computeScore(target, tile, targetBrightness);

            if (score < bestScore) {
                bestScore = score;
                bestMatch = tile;
            }
        }

        // If all tiles exceeded MAX_USAGE, fallback
        if (bestMatch == null) {
            for (TileImage tile : tiles) {

                double score = computeScore(target, tile, targetBrightness);

                if (score < bestScore) {
                    bestScore = score;
                    bestMatch = tile;
                }
            }
        }

        if (bestMatch != null) {
            bestMatch.incrementUsage();
        }

        return bestMatch;
    }

    private static double computeScore(Color target,
                                       TileImage tile,
                                       double targetBrightness) {

        double colorDistance =
                ImageUtils.colorDistance(target, tile.getAverageColor());

        double brightnessDifference =
                Math.abs(targetBrightness -
                        getBrightness(tile.getAverageColor()));

        return colorDistance
                + (tile.getUsageCount() * 3.5)
                + (brightnessDifference * 3.0);
    }


    private static double getBrightness(Color c) {
        return 0.299 * c.getRed()
                + 0.587 * c.getGreen()
                + 0.114 * c.getBlue();
    }

    private static Color getAverageColor(BufferedImage img) {
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

        return new Color(
                (int)(r / total),
                (int)(g / total),
                (int)(b / total)
        );
    }
}

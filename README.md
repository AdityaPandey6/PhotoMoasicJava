# PhotoMosaicJava

A high-performance Java-based Photo Mosaic Generator that creates a mosaic from a target image using a collection of tile images.

## Features

- **Automated Resizing**: Automatically scales the main image to a target height while maintaining aspect ratio.
- **Intelligent Tile Matching**: Uses color distance and brightness matching to find the best tile for each segment of the image.
- **Usage Limiting**: Prevents excessive repetition of the same tile image by implementing a usage cap (`MAX_USAGE`).
- **Original Overlay**: Applies a subtle overlay of the original image to enhance detail and color accuracy in the final mosaic.
- **Progress Tracking**: Real-time console progress bar for both tile loading and mosaic generation.
- **Randomized Processing**: Processes image blocks in random order to ensure even tile distribution.

## Project Structure

- `MosaicGenerator.java`: The main entry point containing the core logic for loading images, matching tiles, and assembling the mosaic.
- `ImageUtils.java`: Utility class for image resizing and calculating color distances.
- `TileImage.java`: Data model for tile images, storing their average color and tracking usage frequency.

## Configuration

You can customize the generation by modifying constants in `MosaicGenerator.java`:

- `TILE_SIZE`: The dimensions of each tile (default: 60px).
- `TARGET_HEIGHT`: The height of the output mosaic (default: 5000px).
- `MAX_USAGE`: Maximum number of times a single tile can be reused (default: 30).
- `OVERLAY_OPACITY`: Opacity of the original image overlay (default: 0.17f).

## Prerequisites

- Java Development Kit (JDK) 8 or higher.
- Maven (for dependency management).

## Getting Started

1.  **Prepare your images**:
    - Place your main image as `mainImage.png` in the project root.
    - Place your tile images in `src/main/java/org/example/tiles`.
2.  **Build the project**:
    ```bash
    mvn clean compile
    ```
3.  **Run the generator**:
    ```bash
    mvn exec:java -Dexec.mainClass="org.example.MosaicGenerator"
    ```
4.  **Find your output**: The result will be saved as `output.png` in the project root.

## Technical Details

### Color Matching Algorithm
The generator uses a scoring system to select tiles:
`Score = ColorDistance + (UsageCount * 3.5) + (BrightnessDifference * 3.0)`
The tile with the lowest score is selected, prioritizing color match while penalizing over-used tiles and significant brightness deviations.

### Performance
The generator uses `Graphics2D` for efficient image assembly and processes tiles in memory for speed.

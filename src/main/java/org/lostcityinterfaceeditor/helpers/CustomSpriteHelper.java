package org.lostcityinterfaceeditor.helpers;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CustomSpriteHelper {
    public ArrayList<WritableImage> sprites = new ArrayList<>();

    public CustomSpriteHelper(String imagePngFile) throws IOException {
        loadImageData(imagePngFile);
    }

    private void loadImageData(String imagePngFile) throws IOException {
        File spriteFile = new File(imagePngFile);
        Image image = new Image(spriteFile.toURI().toString());
        String spriteName = spriteFile.getName();

        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            spriteName = spriteName.substring(0, dotIndex);
        }

        String baseDir = spriteFile.getParent();
        File metaDir = new File(baseDir, "meta");
        File imageDataFile = new File(metaDir, spriteName + ".opt");
        int imageWidth = (int)image.getWidth();
        ArrayList<WritableImage> sprites = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(imageDataFile))) {
            String line;
            int count = 0;
            int blockWidth = 0;
            int blockHeight = 0;
            int rowCount = 1;
            reader.mark(100);
            String firstLine = reader.readLine();
            String secondLine = reader.readLine();
            if(secondLine != null) {
                String[] dimensions = firstLine.split("x");
                blockWidth = Integer.parseInt(dimensions[0]);
                blockHeight = Integer.parseInt(dimensions[1]);
                rowCount = imageWidth / blockWidth;
            }
            reader.reset();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    int initialXOffset = Integer.parseInt(parts[0]);
                    int initialYOffset = Integer.parseInt(parts[1]);
                    int width = Integer.parseInt(parts[2]);
                    int height = Integer.parseInt(parts[3]);

                    int xOffset = initialXOffset + blockWidth * (count % rowCount);
                    int yOffset = initialYOffset + blockHeight * (count / rowCount);

                    WritableImage spriteImage = extractSprite(image, xOffset, yOffset, width, height, initialXOffset, initialYOffset);
                    sprites.add(spriteImage);
                    count++;
                }
            }
        }
        this.sprites = sprites;
    }

    private WritableImage extractSprite(Image sourceImage, int xOffset, int yOffset, int width, int height, int initialXOffset, int initialYOffset) {
        WritableImage spriteImage = new WritableImage(width + initialXOffset, height + initialYOffset);
        PixelWriter pixelWriter = spriteImage.getPixelWriter();
        PixelReader pixelReader = sourceImage.getPixelReader();

        Color magenta = Color.MAGENTA;
        double tolerance = 0.001;

        for (int y = 0; y < height + initialYOffset; y++) {
            for (int x = 0; x < width + initialXOffset; x++) {
                pixelWriter.setColor(x, y, Color.TRANSPARENT);
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceX = xOffset + x;
                int sourceY = yOffset + y;

                if (sourceX < sourceImage.getWidth() && sourceY < sourceImage.getHeight()) {
                    Color pixelColor = pixelReader.getColor(sourceX, sourceY);

                    if (Math.abs(pixelColor.getRed() - magenta.getRed()) < tolerance &&
                            Math.abs(pixelColor.getGreen() - magenta.getGreen()) < tolerance &&
                            Math.abs(pixelColor.getBlue() - magenta.getBlue()) < tolerance) {
                    } else {
                        pixelWriter.setColor(x + initialXOffset, y + initialYOffset, pixelColor);
                    }
                }
            }
        }
        return spriteImage;
    }

    public void saveToFile(String outputPath) throws IOException {
        File originalFile = new File(outputPath);
        Image originalImage = new Image(originalFile.toURI().toString());
        int originalWidth = (int) originalImage.getWidth();
        int originalHeight = (int) originalImage.getHeight();

        WritableImage resultImage = new WritableImage(originalWidth, originalHeight);
        PixelWriter writer = resultImage.getPixelWriter();

        for (int y = 0; y < originalHeight; y++) {
            for (int x = 0; x < originalWidth; x++) {
                writer.setColor(x, y, Color.MAGENTA);
            }
        }

        String spriteName = originalFile.getName();
        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            spriteName = spriteName.substring(0, dotIndex);
        }

        String baseDir = originalFile.getParent();
        File metaDir = new File(baseDir, "meta");
        File imageDataFile = new File(metaDir, spriteName + ".opt");

        ArrayList<int[]> spritePositions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(imageDataFile))) {
            String line;
            int blockWidth = 0;
            int blockHeight = 0;
            int rowCount = 1;

            reader.mark(100);
            String firstLine = reader.readLine();
            String secondLine = reader.readLine();
            if (secondLine != null) {
                String[] dimensions = firstLine.split("x");
                blockWidth = Integer.parseInt(dimensions[0]);
                blockHeight = Integer.parseInt(dimensions[1]);
                rowCount = originalWidth / blockWidth;
            }
            reader.reset();

            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    int initialXOffset = Integer.parseInt(parts[0]);
                    int initialYOffset = Integer.parseInt(parts[1]);
                    int width = Integer.parseInt(parts[2]);
                    int height = Integer.parseInt(parts[3]);

                    int xOffset = initialXOffset + blockWidth * (count % rowCount);
                    int yOffset = initialYOffset + blockHeight * (count / rowCount);

                    spritePositions.add(new int[]{xOffset, yOffset, width, height, initialXOffset, initialYOffset});
                    count++;
                }
            }
        }

        for (int i = 0; i < sprites.size() && i < spritePositions.size(); i++) {
            WritableImage sprite = sprites.get(i);
            int[] position = spritePositions.get(i);

            int xOffset = position[0];
            int yOffset = position[1];
            int width = position[2];
            int height = position[3];
            int initialXOffset = position[4];
            int initialYOffset = position[5];

            PixelReader spriteReader = sprite.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int spriteX = x + initialXOffset;
                    int spriteY = y + initialYOffset;

                    if (spriteX < sprite.getWidth() && spriteY < sprite.getHeight()) {
                        Color pixelColor = spriteReader.getColor(spriteX, spriteY);

                        if (pixelColor.getOpacity() > 0.01) {
                            writer.setColor(xOffset + x, yOffset + y, pixelColor);
                        }
                    }
                }
            }
        }

        BufferedImage bufferedImage = new BufferedImage(
                (int) resultImage.getWidth(),
                (int) resultImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < resultImage.getHeight(); y++) {
            for (int x = 0; x < resultImage.getWidth(); x++) {
                Color color = resultImage.getPixelReader().getColor(x, y);

                int rgb = ((int) (color.getRed() * 255) << 16) |
                        ((int) (color.getGreen() * 255) << 8) |
                        ((int) (color.getBlue() * 255));

                bufferedImage.setRGB(x, y, rgb);
            }
        }

        try {
            ImageIO.write(bufferedImage, "png", originalFile);
            updatePaletteFile(outputPath);
        } catch (IOException e) {
            throw new IOException("Failed to save image to " + outputPath, e);
        }
    }

    private Set<Color> collectUniqueColors() {
        Set<Color> uniqueColors = new HashSet<>();
        uniqueColors.add(Color.MAGENTA);

        for (WritableImage sprite : sprites) {
            PixelReader reader = sprite.getPixelReader();
            for (int y = 0; y < sprite.getHeight(); y++) {
                for (int x = 0; x < sprite.getWidth(); x++) {
                    Color color = reader.getColor(x, y);
                    if (color.getOpacity() > 0.01) {
                        Color normalizedColor = Color.rgb(
                                (int)(color.getRed() * 255),
                                (int)(color.getGreen() * 255),
                                (int)(color.getBlue() * 255)
                        );
                        uniqueColors.add(normalizedColor);
                    }
                }
            }
        }
        return uniqueColors;
    }

    private void updatePaletteFile(String outputPath) throws IOException {
        File originalFile = new File(outputPath);
        String spriteName = originalFile.getName();
        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            spriteName = spriteName.substring(0, dotIndex);
        }

        String baseDir = originalFile.getParent();
        File metaDir = new File(baseDir, "meta");
        if (!metaDir.exists()) {
            metaDir.mkdirs();
        }
        File paletteFile = new File(metaDir, spriteName + ".pal.png");

        Set<Color> uniqueColors = collectUniqueColors();

        BufferedImage paletteImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                paletteImage.setRGB(x, y, 16711935);
            }
        }

        int colorIndex = 0;
        for (Color color : uniqueColors) {
            if (colorIndex >= 256) break;

            int x = colorIndex % 16;
            int y = colorIndex / 16;

            int rgb = ((int)(color.getRed() * 255) << 16) |
                    ((int)(color.getGreen() * 255) << 8) |
                    ((int)(color.getBlue() * 255));

            paletteImage.setRGB(x, y, rgb);
            colorIndex++;
        }

        try {
            ImageIO.write(paletteImage, "png", paletteFile);
        } catch (IOException e) {
            throw new IOException("Failed to save palette file to " + paletteFile.getPath(), e);
        }
    }
}
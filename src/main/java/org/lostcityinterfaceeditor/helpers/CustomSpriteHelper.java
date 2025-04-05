package org.lostcityinterfaceeditor.helpers;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
}
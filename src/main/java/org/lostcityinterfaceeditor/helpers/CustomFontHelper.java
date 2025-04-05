package org.lostcityinterfaceeditor.helpers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CustomFontHelper {
    private final byte[][] charMask = new byte[94][];
    private final int[] charMaskWidth = new int[94];
    private final int[] charMaskHeight = new int[94];
    private final int[] charOffsetX = new int[94];
    private final int[] charOffsetY = new int[94];
    private final int[] charAdvance = new int[95];
    private final int[] drawWidth = new int[256];
    public final Map<Character, WritableImage> charImages;
    private Image fontImage;
    private int gridSize;
    private int charsPerRow;
    public int height;
    private static final int[] CHAR_LOOKUP = new int[256];

    static {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

        for (int i = 0; i < 256; i++) {
            int c = charset.indexOf((char)i);
            if (c == -1) {
                c = 74;
            }
            CHAR_LOOKUP[i] = c;
        }
    }

    public CustomFontHelper(String imagePngFile) throws IOException {
        charImages = new HashMap<>();

        File fontFile = new File(imagePngFile);
        fontImage = new Image(fontFile.toURI().toString());

        String fontName = fontFile.getName();
        int dotIndex = fontName.lastIndexOf('.');
        if (dotIndex > 0) {
            fontName = fontName.substring(0, dotIndex);
        }

        String baseDir = fontFile.getParent();
        File metaDir = new File(baseDir, "meta");
        File fontOptFile = new File(metaDir, fontName + ".opt");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("fonts/" + fontName + ".dat");
        byte[] datFile;
        try (inputStream) {
            datFile = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + fontName, e);
        }
        loadFontData(fontOptFile.getPath(), datFile);

        charAdvance[94] = charAdvance[8];

        for (int c = 0; c < 256; c++) {
            drawWidth[c] = charAdvance[CHAR_LOOKUP[c]];
        }

        createCharacterImagesFromFontImage();
    }

    private void loadFontData(String fontDataFile, byte[] dat) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fontDataFile))) {
            String firstLine = reader.readLine();
            if (firstLine == null) throw new IOException("Font data file is empty or missing dimensions: " + fontDataFile);
            String[] dimensions = firstLine.split("x");
            if (dimensions.length != 2) throw new IOException("Invalid dimensions format in font data file: " + firstLine);
            gridSize = Integer.parseInt(dimensions[0]);
            charsPerRow = (int)fontImage.getWidth() / gridSize;

            String line;
            int charIndex = 0;
            int datPos = 2;
            height = 0;

            while ((line = reader.readLine()) != null && charIndex < 94) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    charOffsetY[charIndex] = Integer.parseInt(parts[1]);
                    int w = charMaskWidth[charIndex] = Integer.parseInt(parts[2]);
                    int h = charMaskHeight[charIndex] = Integer.parseInt(parts[3]);
                    int type = parts[4].equals("column") ? 0 : 1;

                    int len = w * h;
                    if (datPos + len > dat.length) {
                        throw new IOException("Not enough data in .dat file for character index " + charIndex);
                    }
                    charMask[charIndex] = new byte[len];

                    if (type == 0) {
                        for (int j = 0; j < len; j++) {
                            charMask[charIndex][j] = dat[datPos++];
                        }
                    } else if (type == 1) {
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                if (x + y * w < len) {
                                    charMask[charIndex][x + y * w] = dat[datPos++];
                                } else {
                                    System.err.println("Warning: Index out of bounds during row-wise read for char " + charIndex);
                                }
                            }
                        }
                    } else {
                        System.err.println("Warning: Unknown character data type " + type + " for char index " + charIndex);
                        datPos += len;
                    }
                    if (h > height) {
                        height = h;
                    }
                    charOffsetX[charIndex] = 1;
                    charAdvance[charIndex] = w + 2;
                    if (h >= 7) {
                        int spaceLeft = 0;
                        for (int j = h / 7; j < h; j++) {
                            if (j*w >= 0 && j*w < charMask[charIndex].length) {
                                spaceLeft += charMask[charIndex][j * w];
                            }
                        }
                        if (spaceLeft <= h / 7) {
                            charAdvance[charIndex]--;
                            charOffsetX[charIndex] = 0;
                        }

                        int spaceRight = 0;
                        for (int j = h / 7; j < h; j++) {
                            int idx = w + j * w - 1;
                            if (idx >= 0 && idx < charMask[charIndex].length) {
                                spaceRight += charMask[charIndex][idx];
                            }
                        }
                        if (spaceRight <= h / 7) {
                            charAdvance[charIndex]--;
                        }
                    } else if (w == 0 || h == 0) {
                        charAdvance[charIndex] = (w > 0) ? w : 4;
                        charOffsetX[charIndex] = 0;
                    }


                    charIndex++;
                } else if (!line.trim().isEmpty()) {
                    System.err.println("Warning: Skipping malformed line in font data: " + line);
                }
            }
            if (charIndex < 94) {
                System.err.println("Warning: Font data file only contained " + charIndex + " characters, expected 94.");
            }
        }
    }

    private void createCharacterImagesFromFontImage() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";

        for (int i = 0; i < charset.length(); i++) {
            char c = charset.charAt(i);
            int index = CHAR_LOOKUP[c];
            if (index < charMask.length) {
                int gridRow = i / charsPerRow;
                int gridCol = i % charsPerRow;

                int xOffset = gridCol * gridSize;
                int yOffset = gridRow * gridSize;

                int width = charMaskWidth[index];
                int height = charMaskHeight[index];

                WritableImage charImage = extractCharacter(fontImage, xOffset, yOffset, width, height, index);
                charImages.put(c, charImage);
            }
        }
    }

    private WritableImage extractCharacter(Image sourceImage, int xOffset, int yOffset, int width, int height, int charIndex) {
        WritableImage charImage = new WritableImage(width, height);
        PixelWriter pixelWriter = charImage.getPixelWriter();
        PixelReader pixelReader = sourceImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceX = xOffset + x;
                int sourceY = yOffset + y;

                if (sourceX < sourceImage.getWidth() && sourceY < sourceImage.getHeight()) {
                    Color pixelColor = pixelReader.getColor(sourceX, sourceY);

                    if (charMask[charIndex][x + y * width] != 0) {
                        pixelWriter.setColor(x, y, pixelColor);
                    } else {
                        pixelWriter.setColor(x, y, Color.TRANSPARENT);
                    }
                } else {
                    pixelWriter.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }

        return charImage;
    }

    public int getTextWidth(String str) {
        if (str == null) {
            return 0;
        }
        int size = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '@' && i + 4 < str.length() && str.charAt(i + 4) == '@') {
                i += 4;
            } else {
                size += charAdvance[CHAR_LOOKUP[str.charAt(i)]];
            }
        }
        return size;
    }

    public void drawTextWithTags(GraphicsContext gc, String text, double x, double y, Color defaultColor, boolean shadowed) {
        if (text == null) {
            return;
        }

        double offY = y - height;
        double currentX = x;
        Color currentColor = defaultColor;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '@' && i + 4 < text.length() && text.charAt(i + 4) == '@') {
                String colorTag = text.substring(i + 1, i + 4);
                currentColor = evaluateColorTag(colorTag);
                i += 4;
                continue;
            }

            char c = text.charAt(i);
            int index = CHAR_LOOKUP[c];

            if (index < 95) {
                WritableImage image = charImages.get(c);
                if (image != null && c != ' ') {
                    if (shadowed) {
                        WritableImage shadowImage = createColoredCharacter(image, Color.BLACK);
                        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
                        gc.drawImage(shadowImage,
                                currentX + charOffsetX[index] + 1,
                                offY + charOffsetY[index] + 1,
                                image.getWidth(), image.getHeight());
                    }

                    WritableImage coloredImage = createColoredCharacter(image, currentColor);
                    gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
                    gc.drawImage(coloredImage,
                            currentX + charOffsetX[index],
                            offY + charOffsetY[index],
                            image.getWidth(), image.getHeight());
                }

                currentX += charAdvance[index];
            }
        }
    }

    private Color evaluateColorTag(String tag) {
        switch (tag) {
            case "red":
                return Color.web("#ff0000");
            case "gre":
                return Color.web("#00ff00");
            case "blu":
                return Color.web("#0000ff");
            case "yel":
                return Color.web("#ffff00");
            case "cya":
                return Color.web("#00ffff");
            case "mag":
                return Color.web("#ff00ff");
            case "whi":
                return Color.web("#ffffff");
            case "bla":
                return Color.web("#000000");
            case "lre":
                return Color.web("#ff9040");
            case "dre":
                return Color.web("#800000");
            case "dbl":
                return Color.web("#000080");
            case "or1":
                return Color.web("#ffb000");
            case "or2":
                return Color.web("#ff7000");
            case "or3":
                return Color.web("#ff3000");
            case "gr1":
                return Color.web("#c0ff00");
            case "gr2":
                return Color.web("#80ff00");
            case "gr3":
                return Color.web("#40ff00");
            default:
                return Color.web("#000000");
        }
    }

    private WritableImage createColoredCharacter(Image source, Color color) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage coloredImage = new WritableImage(width, height);

        PixelReader reader = source.getPixelReader();
        PixelWriter writer = coloredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = reader.getColor(x, y);

                if (pixelColor.getOpacity() > 0) {
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            pixelColor.getOpacity()
                    );
                    writer.setColor(x, y, newColor);
                } else {
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        return coloredImage;
    }
}
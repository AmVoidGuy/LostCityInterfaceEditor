package org.lostcityinterfaceeditor.baseCode;

import org.lostcityinterfaceeditor.LostCityInterfaceEditor;
import org.lostcityinterfaceeditor.fileUtils.OptFileTransformer;
import org.lostcityinterfaceeditor.loaders.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pix8 extends Pix2D {

    public byte[] pixels;
    public int[] palette = null;
    public int width;
    public int height;
    public int cropX;
    public int cropY;
    public int cropW;
    private int cropH;

    public Pix8(String name) {
        Map<String, OptFileTransformer.TextureOptions> textureOptionsMap = TextureLoader.textureOptsMap;
        OptFileTransformer.TextureOptions textureOptions = textureOptionsMap.get(name);
        BufferedImage image = null;
        String sourcePath = LostCityInterfaceEditor.serverDirectoryPath + "/textures/";
        String imagePath = Paths.get(sourcePath, name + ".png").toString();
        try {
            InputStream inputStream = new FileInputStream(imagePath);
            image = ImageIO.read(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
            return;
        }

        if (image == null) {
            System.err.println("Error loading image: " + imagePath + " (image is null after reading)");
            return;
        }

        this.cropW = image.getWidth();
        this.cropH = image.getHeight();

        this.palette = generatePalette(image);

        this.cropX = textureOptions.cropX();
        this.cropY = textureOptions.cropY();
        this.width = textureOptions.width();
        this.height = textureOptions.height();

        int pixelOrder = textureOptions.pixelOrder();
        int len = this.width * this.height;
        this.pixels = new byte[len];
        loadPixelData(image, pixelOrder);
    }

    private int[] generatePalette(BufferedImage image) {
        List<Integer> colorList = new ArrayList<>();
        colorList.add(0);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0xFFFFFF;
                if (rgb == 0xFF00FF) {
                    continue;
                }
                if (!colorList.contains(rgb)) {
                    colorList.add(rgb);
                }
            }
        }

        int[] palette = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); i++) {
            palette[i] = colorList.get(i);
        }
        return palette;
    }

    private void loadPixelData(BufferedImage image, int pixelOrder) {
        int len = this.width * this.height;

        if (pixelOrder == 0) {
            for (int i = 0; i < len; i++) {
                int x = i % this.width;
                int y = i / this.width;

                int imageX = cropX + x;
                int imageY = cropY + y;
                if (imageX < 0 || imageX >= image.getWidth() || imageY < 0 || imageY >= image.getHeight()) {
                    pixels[i] = 0;
                    continue;
                }

                int rgb = image.getRGB(imageX, imageY) & 0xFFFFFF;
                byte index = findPaletteIndex(rgb);
                pixels[i] = index;
            }
        } else if (pixelOrder == 1) {
            for (int x = 0; x < this.width; x++) {
                for (int y = 0; y < this.height; y++) {
                    int imageX = cropX + x;
                    int imageY = cropY + y;
                    if (imageX < 0 || imageX >= image.getWidth() || imageY < 0 || imageY >= image.getHeight()) {
                        pixels[x + y * this.width] = 0;
                        continue;
                    }

                    int rgb = image.getRGB(imageX, imageY) & 0xFFFFFF;
                    byte index = findPaletteIndex(rgb);
                    pixels[x + y * this.width] = index;
                }
            }
        }
    }

    private byte findPaletteIndex(int rgb) {
        for (byte i = 0; i < palette.length; i++) {
            if (palette[i] == rgb) {
                return i;
            }
        }
        return 0;
    }

    public void crop() {
        if (this.width == this.cropW && this.height == this.cropH) {
            return;
        }

        byte[] pixels = new byte[this.cropW * this.cropH];
        int off = 0;
        for ( int y = 0; y < this.height; y++) {
            for ( int x = 0; x < this.width; x++) {
                pixels[x + this.cropX + (y + this.cropY) * this.cropW] = this.pixels[off++];
            }
        }

        this.pixels = pixels;
        this.width = this.cropW;
        this.height = this.cropH;
        this.cropX = 0;
        this.cropY = 0;
    }
}

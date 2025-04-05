package org.lostcityinterfaceeditor.managers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lostcityinterfaceeditor.helpers.CustomFontHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private final Map<String, CustomFontHelper> fonts = new HashMap<>();

    public void loadFont(String fontImageFile) throws IOException {
        File fontFile = new File(fontImageFile);
        String fontName = fontFile.getName();
        int dotIndex = fontName.lastIndexOf('.');
        if (dotIndex > 0) {
            fontName = fontName.substring(0, dotIndex);
        }

        CustomFontHelper font = new CustomFontHelper(fontImageFile);
        fonts.put(fontName, font);
    }

    public CustomFontHelper getFont(String fontName) {
        return fonts.get(fontName);
    }

    public void drawTaggableText(GraphicsContext gc, String fontName, String text,
                                 double x, double y, int rgb, boolean shadow) {
        CustomFontHelper font = getFont(fontName);
        Color color = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);

        if (font != null) {
            font.drawTextWithTags(gc, text, x, y, color, shadow);
        } else {
            System.err.println("Font not found: " + fontName);
        }
    }
}
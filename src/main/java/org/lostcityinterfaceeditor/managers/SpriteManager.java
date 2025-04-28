package org.lostcityinterfaceeditor.managers;

import javafx.scene.image.WritableImage;
import org.lostcityinterfaceeditor.helpers.CustomSpriteHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteManager {
    private final Map<String, CustomSpriteHelper> sprites = new HashMap<>();
    private final Map<String, String> spriteSourcePaths = new HashMap<>();
    private final List<String> spriteDirectories = new ArrayList<>();

    public SpriteManager() {
        spriteDirectories.add("sprites");
        spriteDirectories.add("title");
    }

    public void loadSprites(String spriteImageFile) throws IOException {
        File spriteFile = new File(spriteImageFile);
        String spriteName = spriteFile.getName();

        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            spriteName = spriteName.substring(0, dotIndex);
        }
        CustomSpriteHelper customSpriteHelper = new CustomSpriteHelper(spriteImageFile);
        sprites.put(spriteName, customSpriteHelper);
        spriteSourcePaths.put(spriteName, spriteFile.getAbsolutePath());
    }

    public CustomSpriteHelper getSprites(String spriteName) {
        return sprites.get(spriteName);
    }

    public WritableImage getSprite(String name, int index) {
        CustomSpriteHelper helper = getSprites(name);
        if (helper != null && index >= 0 && index < helper.sprites.size()) {
            return helper.sprites.get(index);
        }
        return null;
    }

    public List<String> getAllSpriteNames() {
        return new ArrayList<>(sprites.keySet());
    }

    public void saveSprite(String spriteName, int spriteIndex, WritableImage sprite) throws IOException {
        CustomSpriteHelper spriteHelper = sprites.get(spriteName);
        if (spriteHelper != null && spriteIndex >= 0 && spriteIndex < spriteHelper.sprites.size()) {
            spriteHelper.sprites.set(spriteIndex, sprite);

            String originalFilePath = spriteSourcePaths.get(spriteName);
            if (originalFilePath != null) {
                File originalFile = new File(originalFilePath);
                String baseDir = originalFile.getParent();
                File metaDir = new File(baseDir, "meta");
                File paletteFile = new File(metaDir, spriteName + ".pal.png");
                boolean shouldCreatePalette = paletteFile.exists();
                spriteHelper.setShouldCreatePalette(shouldCreatePalette);
                spriteHelper.saveToFile(originalFilePath);
                return;
            }
        }
        throw new IOException("Failed to save sprite: " + spriteName + " at index " + spriteIndex);
    }
}
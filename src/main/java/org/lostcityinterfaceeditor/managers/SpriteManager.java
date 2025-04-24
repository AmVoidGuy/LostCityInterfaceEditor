package org.lostcityinterfaceeditor.managers;

import javafx.scene.image.WritableImage;
import org.lostcityinterfaceeditor.LostCityInterfaceEditor;
import org.lostcityinterfaceeditor.helpers.CustomSpriteHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteManager {
    private final Map<String, CustomSpriteHelper> sprites = new HashMap<>();

    public void loadSprites(String spriteImageFile) throws IOException {
        File spriteFile = new File(spriteImageFile);
        String spriteName = spriteFile.getName();

        int dotIndex = spriteName.lastIndexOf('.');
        if (dotIndex > 0) {
            spriteName = spriteName.substring(0, dotIndex);
        }
        CustomSpriteHelper customSpriteHelper = new CustomSpriteHelper(spriteImageFile);
        sprites.put(spriteName, customSpriteHelper);
    }

    public CustomSpriteHelper getSprites(String spriteName) {
        return sprites.get(spriteName);
    }

    public WritableImage getSprite(String name, int index) {
        return getSprites(name).sprites.get(index);
    }

    public List<String> getAllSpriteNames() {
        return new ArrayList<>(sprites.keySet());
    }

    public void saveSprite(String spriteName, int spriteIndex, WritableImage sprite) throws IOException {
        CustomSpriteHelper spriteHelper = sprites.get(spriteName);
        if (spriteHelper != null && spriteIndex >= 0 && spriteIndex < spriteHelper.sprites.size()) {
            spriteHelper.sprites.set(spriteIndex, sprite);

            String originalFilePath = findOriginalFilePath(spriteName);
            if (originalFilePath != null) {
                spriteHelper.saveToFile(originalFilePath);
                return;
            }
        }
        throw new IOException("Failed to save sprite: " + spriteName + " at index " + spriteIndex);
    }

    private String findOriginalFilePath(String spriteName) {
        File baseDir = new File(LostCityInterfaceEditor.serverDirectoryPath, "sprites");
        File spriteFile = new File(baseDir, spriteName + ".png");
        if (spriteFile.exists()) {
            return spriteFile.getAbsolutePath();
        }
        return null;
    }
}
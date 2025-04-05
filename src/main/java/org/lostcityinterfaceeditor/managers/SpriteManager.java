package org.lostcityinterfaceeditor.managers;

import javafx.scene.image.WritableImage;
import org.lostcityinterfaceeditor.helpers.CustomSpriteHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
}
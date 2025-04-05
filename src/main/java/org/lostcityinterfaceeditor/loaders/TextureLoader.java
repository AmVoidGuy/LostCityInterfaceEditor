package org.lostcityinterfaceeditor.loaders;

import org.lostcityinterfaceeditor.fileUtils.OptFileTransformer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TextureLoader {
    public static Map<String, OptFileTransformer.TextureOptions> textureOptsMap;
    public static Map<Integer, String> textureMap = new HashMap<>();

    public static void loadTextures(String serverSrcDirectory) {
        textureOptsMap = OptFileTransformer.loadTextureOptions(serverSrcDirectory + "/textures/meta/");
        textureMap = parsePackFile(serverSrcDirectory + "/pack/texture.pack");
    }

    private static Map<Integer, String> parsePackFile(String packFilePath) {
        Map<Integer, String> packMap = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(packFilePath);
             Scanner scanner = new Scanner(inputStream)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        packMap.put(id, name);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid pack file line: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid pack file line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading pack file: " + e.getMessage());
        }

        return packMap;
    }

}

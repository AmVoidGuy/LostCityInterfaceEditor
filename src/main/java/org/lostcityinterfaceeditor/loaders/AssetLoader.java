package org.lostcityinterfaceeditor.loaders;

import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.lostcityinterfaceeditor.baseCode.Model;
import org.lostcityinterfaceeditor.baseCode.Pix3D;
import org.lostcityinterfaceeditor.managers.FontManager;
import org.lostcityinterfaceeditor.managers.SpriteManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AssetLoader {

    private final String serverDirectoryPath;
    private final FontManager fontManager;
    private final SpriteManager spriteManager;
    private final Map<String, Model> loadedModels;

    private boolean loaded = false;

    public AssetLoader(String serverDirectoryPath) {
        if (serverDirectoryPath == null || serverDirectoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Server directory path cannot be null or empty.");
        }
        File dir = new File(serverDirectoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("Warning: Provided server directory path does not exist or is not a directory: " + serverDirectoryPath);
        }

        this.serverDirectoryPath = serverDirectoryPath;
        this.fontManager = new FontManager();
        this.spriteManager = new SpriteManager();
        this.loadedModels = new HashMap<>();
    }

    public void loadAll() throws IOException {
        if (loaded) {
            System.out.println("Assets already loaded.");
            return;
        }
        System.out.println("Starting asset loading from: " + serverDirectoryPath);
        try {
            loadTexturesAnd3D();
            loadModels();
            loadSprites();
            loadFonts();
            loaded = true;
            System.out.println("Asset loading successfully completed.");
        } catch (IOException e) {
            loaded = false;
            System.err.println("------------------------------------------");
            System.err.println("Error during loading:");
            System.err.println("Path: " + serverDirectoryPath);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Application might not function correctly.");
            System.err.println("------------------------------------------");
            throw e;
        } catch (Exception e) {
            loaded = false;
            System.err.println("------------------------------------------");
            System.err.println("UNEXPECTED ERROR DURING ASSET LOADING:");
            System.err.println("Path: " + serverDirectoryPath);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("------------------------------------------");
            throw new IOException("An unexpected error occurred during asset loading.", e);
        }
    }
    public static String chooseServerDirectory(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Directory Selection");
        alert.setHeaderText("Select Server Data Source Directory");
        alert.setContentText("Please select the root directory containing your server's 'models', 'sprites', 'fonts', etc. folders (e.g., '../Server/data/src/').");
        alert.showAndWait();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Server Data Source Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            return selectedDirectory.getAbsolutePath();
        } else {
            return null;
        }
    }

    private void loadTexturesAnd3D() throws IOException {
        try {
            TextureLoader.loadTextures(serverDirectoryPath);
            Pix3D.loadTextures();
            Pix3D.setBrightness(0.8);
            Pix3D.initPool(20);
        } catch (Exception e) {
            throw new IOException("Failed to initialize textures or 3D system: " + e.getMessage(), e);
        }
    }

    private void loadModels() throws IOException {
        System.out.println("Loading models...");
        Path modelsDirectory = Paths.get(serverDirectoryPath, "models");

        if (!Files.isDirectory(modelsDirectory)) {
            throw new IOException("Required 'models' directory not found at: " + modelsDirectory);
        }

        Map<String, Model> successfullyLoaded = new HashMap<>();
        try (Stream<Path> paths = Files.walk(modelsDirectory)) {
            paths.filter(path -> path.toString().toLowerCase().endsWith(".ob2") && Files.isRegularFile(path))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
                        try {
                            successfullyLoaded.put(fileNameNoExt, Model.convert(path));
                        } catch (Exception e) {
                            System.err.println("Warning: Failed to load or convert model '" + fileNameNoExt + "': " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new IOException("Error reading models directory: " + modelsDirectory, e);
        }

        this.loadedModels.putAll(successfullyLoaded);
        System.out.println("Finished loading models. Successfully loaded: " + this.loadedModels.size());
    }

    private void loadSprites() throws IOException {
        System.out.println("Loading sprites...");
        Path spritesDirectoryPath = Paths.get(serverDirectoryPath, "sprites");

        if (!Files.isDirectory(spritesDirectoryPath)) {
            throw new IOException("Required 'sprites' directory not found at: " + spritesDirectoryPath);
        }

        File spritesDirectory = spritesDirectoryPath.toFile();
        File[] spriteFiles = spritesDirectory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") && new File(dir, name).isFile());

        int loadedCount = 0;
        if (spriteFiles != null) {
            for (File spriteFile : spriteFiles) {
                try {
                    spriteManager.loadSprites(spriteFile.getAbsolutePath());
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("Warning: Failed to load sprite sheet '" + spriteFile.getName() + "': " + e.getMessage());
                }
            }
            System.out.println("Finished loading sprites. Processed " + loadedCount + " sheets.");
        } else {
            throw new IOException("Could not list files in sprites directory: " + spritesDirectoryPath);
        }
    }

    private void loadFonts() throws IOException {
        System.out.println("Loading fonts...");
        Path fontsDirectoryPath = Paths.get(serverDirectoryPath, "fonts");

        if (!Files.isDirectory(fontsDirectoryPath)) {
            throw new IOException("Required 'fonts' directory not found at: " + fontsDirectoryPath);
        }

        File fontsDirectory = fontsDirectoryPath.toFile();
        File[] fontFiles = fontsDirectory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") && new File(dir, name).isFile());

        int loadedCount = 0;
        if (fontFiles != null) {
            for (File fontFile : fontFiles) {
                try {
                    fontManager.loadFont(fontFile.getAbsolutePath());
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("Warning: Failed to load font '" + fontFile.getName() + "': " + e.getMessage());
                }
            }
            System.out.println("Finished loading fonts. Loaded " + loadedCount + " font definitions.");
        } else {
            throw new IOException("Could not list files in fonts directory: " + fontsDirectoryPath);
        }
    }
    public FontManager getFontManager() {
        if (!loaded) throw new IllegalStateException("Assets have not been loaded yet. Call loadAll() first.");
        return fontManager;
    }

    public SpriteManager getSpriteManager() {
        if (!loaded) throw new IllegalStateException("Assets have not been loaded yet. Call loadAll() first.");
        return spriteManager;
    }

    public Map<String, Model> getLoadedModels() {
        if (!loaded) throw new IllegalStateException("Assets have not been loaded yet. Call loadAll() first.");
        return loadedModels;
    }

    public Model getModel(String name) {
        if (!loaded) throw new IllegalStateException("Assets have not been loaded yet. Call loadAll() first.");
        return loadedModels.get(name);
    }

    public String getServerDirectoryPath() {
        return serverDirectoryPath;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
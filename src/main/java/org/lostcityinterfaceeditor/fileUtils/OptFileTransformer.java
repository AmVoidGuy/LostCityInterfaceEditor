package org.lostcityinterfaceeditor.fileUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptFileTransformer {
    public static Map<String, TextureOptions> loadTextureOptions(String textureOptDirectory) {
        Map<String, TextureOptions> textureOptions = new HashMap<>();

        try {
            List<String> filePaths = getFilesInDirectory(textureOptDirectory);
            if (filePaths == null) {
                System.err.println("No files found in directory: " + textureOptDirectory);
                return textureOptions;
            }

            filePaths.stream()
                    .filter(filePath -> filePath.toLowerCase().endsWith(".opt"))
                    .forEach(filePath -> {
                        String name = getFileNameWithoutExtension(filePath);
                        try {
                            TextureOptions options = parseTextureOptions(filePath);
                            if (options != null) {
                                textureOptions.put(name, options);
                            }
                        } catch (IOException e) {
                            System.err.println("Error processing texture options file: " + filePath);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error accessing texture options directory: " + textureOptDirectory);
            e.printStackTrace();
        }

        return textureOptions;
    }

    private static TextureOptions parseTextureOptions(String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream, "Input stream cannot be null")))) {

            String dataLine = reader.readLine();
            if (dataLine != null) {
                String[] parts = dataLine.split(",");
                if (parts.length == 5) {
                    try {
                        int cropX = Integer.parseInt(parts[0].trim());
                        int cropY = Integer.parseInt(parts[1].trim());
                        int width = Integer.parseInt(parts[2].trim());
                        int height = Integer.parseInt(parts[3].trim());
                        String pixelOrderStr = parts[4].trim().toLowerCase();

                        int pixelOrder = 0;
                        if (pixelOrderStr.equals("column")) {
                            pixelOrder = 1;
                        }

                        return new TextureOptions(cropX, cropY, width, height, pixelOrder);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing data in file: " + filePath + " - Invalid number format.");
                        throw new IOException("Invalid number format in file: " + filePath, e);
                    }
                } else {
                    System.err.println("Error in file: " + filePath + " - Incorrect number of values (expected 5, got " + parts.length + ")");
                    throw new IOException("Incorrect number of values in file: " + filePath);
                }
            } else {
                System.err.println("Warning: Empty file: " + filePath);
                return null;
            }
        }
    }

    private static List<String> getFilesInDirectory(String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            System.err.println("Directory not found or is not a directory: " + directoryPath);
            return null;
        }

        try (Stream<Path> stream = Files.list(directory)) {
            return stream.map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    private static String getFileNameWithoutExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public record TextureOptions(int cropX, int cropY, int width, int height, int pixelOrder) {
    }
}
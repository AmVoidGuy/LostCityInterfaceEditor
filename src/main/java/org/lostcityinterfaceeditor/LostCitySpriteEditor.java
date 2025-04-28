package org.lostcityinterfaceeditor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lostcityinterfaceeditor.helpers.CustomSpriteHelper;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.managers.SpriteManager;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LostCitySpriteEditor {
    private static AssetLoader assetLoader;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 32;
    private static final int MIN_BRUSH_SIZE = 1;
    private static final int MAX_BRUSH_SIZE = 10;
    private static boolean colorPickerMode = false;
    private static Map<String, Map<Integer, WritableImage>> originalSprites = new HashMap<>();
    private static boolean hasUnsavedChanges = false;
    private static final Color PREVIEW_OUTLINE_COLOR = Color.YELLOW;

    static void openSpriteEditor(AssetLoader loader) {
        assetLoader = loader;
        List<String> spriteNames = assetLoader.getSpriteManager().getAllSpriteNames();

        spriteNames.sort(Comparator.naturalOrder());

        if (spriteNames.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Sprites",
                    "No sprites available", "Please load sprites first.");
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select Sprite");
        dialog.setHeaderText("Choose a sprite to edit");

        ButtonType selectButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(spriteNames);
        listView.getSelectionModel().selectFirst();

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(spriteName -> {
            CustomSpriteHelper spriteHelper = assetLoader.getSpriteManager().getSprites(spriteName);
            if (spriteHelper != null && !spriteHelper.sprites.isEmpty()) {
                openPixelEditor(spriteName);
            }
        });
    }

    private static void drawScaledSprite(GraphicsContext gc, WritableImage sprite, int maxWidth, int maxHeight, int zoom) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);

        for (int x = 0; x <= maxWidth; x++) {
            double xPos = x * zoom;
            gc.strokeLine(xPos, 0, xPos, maxHeight * zoom);
        }
        for (int y = 0; y <= maxHeight; y++) {
            double yPos = y * zoom;
            gc.strokeLine(0, yPos, maxWidth * zoom, yPos);
        }

        if (sprite != null) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                for (int y = 0; y < sprite.getHeight(); y++) {
                    Color color = sprite.getPixelReader().getColor(x, y);

                    int pixelX = (int)(x * zoom);
                    int pixelY = (int)(y * zoom);
                    int pixelWidth = (int)Math.ceil(zoom);
                    int pixelHeight = (int)Math.ceil(zoom);

                    pixelWidth = Math.max(1, pixelWidth);
                    pixelHeight = Math.max(1, pixelHeight);

                    if (color.getOpacity() > 0) {
                        gc.setFill(color);
                        gc.fillRect(pixelX, pixelY, pixelWidth, pixelHeight);
                    }
                }
            }
        }
    }

    private static boolean applyBrushToPixel(GraphicsContext mainGc, WritableImage sprite,
                                             int pixelX, int pixelY, Color newColor, int zoom) {
        if (pixelX < 0 || pixelX >= sprite.getWidth() ||
                pixelY < 0 || pixelY >= sprite.getHeight()) {
            return false;
        }

        Color currentColor = sprite.getPixelReader().getColor(pixelX, pixelY);

        if (!currentColor.equals(newColor)) {
            boolean isFullyTransparent = newColor.getOpacity() == 0.0;

            if (isFullyTransparent) {
                sprite.getPixelWriter().setArgb(pixelX, pixelY, 0);
            } else {
                sprite.getPixelWriter().setColor(pixelX, pixelY, newColor);
            }

            int drawX = (int)(pixelX * zoom);
            int drawY = (int)(pixelY * zoom);
            int drawWidth = (int)Math.ceil(zoom);
            int drawHeight = (int)Math.ceil(zoom);

            drawWidth = Math.max(1, drawWidth);
            drawHeight = Math.max(1, drawHeight);

            if (isFullyTransparent) {
                mainGc.clearRect(drawX, drawY, drawWidth, drawHeight);

                mainGc.setStroke(Color.LIGHTGRAY);
                mainGc.setLineWidth(1.0);
                mainGc.strokeLine(drawX, drawY, drawX + drawWidth, drawY);
                mainGc.strokeLine(drawX, drawY, drawX, drawY + drawHeight);
            } else {
                mainGc.setFill(newColor);
                mainGc.fillRect(drawX, drawY, drawWidth, drawHeight);
            }

            return true;
        }

        return false;
    }

    private static void saveSprite(String spriteName, int spriteIndex, WritableImage sprite) {
        try {
            assetLoader.getSpriteManager().saveSprite(spriteName, spriteIndex, sprite);
            storeOriginalSprite(spriteName, spriteIndex, sprite);
            hasUnsavedChanges = false;
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Sprite saved", "The sprite has been updated successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to save sprite", e.getMessage());
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static ButtonType showSaveConfirmDialog(int oldIndex) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes to sprite index " + oldIndex);
        alert.setContentText("Would you like to save your changes?");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        return alert.showAndWait().orElse(cancelButton);
    }

    private static WritableImage createBackupSprite(WritableImage originalSprite) {
        if (originalSprite == null) {
            return null;
        }

        int width = (int) originalSprite.getWidth();
        int height = (int) originalSprite.getHeight();
        WritableImage backup = new WritableImage(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = originalSprite.getPixelReader().getColor(x, y);
                backup.getPixelWriter().setColor(x, y, color);
            }
        }

        return backup;
    }

    private static void storeOriginalSprite(String spriteName, int index, WritableImage sprite) {
        originalSprites.computeIfAbsent(spriteName, k -> new HashMap<>())
                .putIfAbsent(index, createBackupSprite(sprite));
    }

    private static void restoreOriginalSprite(String spriteName, int index, SpriteManager spriteManager) {
        if (originalSprites.containsKey(spriteName) && originalSprites.get(spriteName).containsKey(index)) {
            WritableImage original = originalSprites.get(spriteName).get(index);
            WritableImage current = spriteManager.getSprite(spriteName, index);

            if (original != null && current != null &&
                    original.getWidth() == current.getWidth() && original.getHeight() == current.getHeight())
            {
                int width = (int) original.getWidth();
                int height = (int) original.getHeight();

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        Color color = original.getPixelReader().getColor(x, y);
                        current.getPixelWriter().setColor(x, y, color);
                    }
                }
                hasUnsavedChanges = false;
            } else {
                System.err.println("Warning: Could not restore original sprite for " + spriteName + "[" + index + "] due to mismatch or null sprite.");
            }
        }
    }

    private static int calculateInitialZoom(int width, int height) {
        int maxDimension = Math.max(width, height);

        if (maxDimension <= 16) {
            return 25;
        } else if (maxDimension <= 32) {
            return 20;
        } else if (maxDimension <= 64) {
            return 10;
        } else {
            return 1;
        }
    }

    private static void openPixelEditor(String spriteName) {
        SpriteManager spriteManager = assetLoader.getSpriteManager();
        CustomSpriteHelper spriteHelper = spriteManager.getSprites(spriteName);

        if (spriteHelper == null || spriteHelper.sprites.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Sprite not found", "Could not load the selected sprite.");
            return;
        }
        int totalSprites = spriteHelper.sprites.size();
        Stage editorStage = new Stage();
        editorStage.setTitle("Sprite Editor - " + spriteName);

        int maxWidth = 0;
        int maxHeight = 0;
        for (WritableImage img : spriteHelper.sprites) {
            if (img != null) {
                maxWidth = Math.max(maxWidth, (int)img.getWidth());
                maxHeight = Math.max(maxHeight, (int)img.getHeight());
            }
        }
        if (maxWidth == 0 || maxHeight == 0) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Invalid Sprite Dimensions", "Could not determine sprite dimensions.");
            return;
        }


        originalSprites.remove(spriteName);
        hasUnsavedChanges = false;

        int initialZoom = calculateInitialZoom(maxWidth, maxHeight);
        final int[] currentZoom = {initialZoom};
        final int[] currentSpriteIndex = {0};
        final int[] requestedSpriteIndex = {0};

        WritableImage initialSprite = spriteManager.getSprite(spriteName, currentSpriteIndex[0]);
        if (initialSprite == null) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Sprite Not Loaded", "Could not load initial sprite at index 0.");
            return;
        }
        storeOriginalSprite(spriteName, currentSpriteIndex[0], initialSprite);

        StackPane canvasPane = new StackPane();
        canvasPane.setAlignment(Pos.TOP_LEFT);

        Canvas mainCanvas = createCanvas(maxWidth, maxHeight, currentZoom[0]);
        GraphicsContext mainGc = mainCanvas.getGraphicsContext2D();

        Canvas previewCanvas = createCanvas(maxWidth, maxHeight, currentZoom[0]);
        GraphicsContext previewGc = previewCanvas.getGraphicsContext2D();

        canvasPane.getChildren().addAll(mainCanvas, previewCanvas);

        final ColorPicker colorPicker = new ColorPicker(Color.BLACK);

        Label indexValueLabel = new Label("0 / " + (totalSprites - 1));

        Button prevButton = new Button("◀ Previous");
        Button nextButton = new Button("Next ▶");

        Label zoomLabel = new Label("Zoom:");
        Slider zoomSlider = new Slider(MIN_ZOOM, MAX_ZOOM, currentZoom[0]);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setMajorTickUnit(5);
        zoomSlider.setBlockIncrement(1);
        Label zoomValueLabel = new Label(currentZoom[0] + "x");

        ToggleButton colorPickerTool = new ToggleButton("Color Picker Tool");
        colorPickerTool.setSelected(colorPickerMode);

        int finalMaxWidth = maxWidth;
        int finalMaxHeight = maxHeight;

        Label brushSizeLabel = new Label("Brush Size:");
        Slider brushSizeSlider = new Slider(MIN_BRUSH_SIZE, MAX_BRUSH_SIZE, 1);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setMajorTickUnit(1);
        brushSizeSlider.setMinorTickCount(0);
        brushSizeSlider.setBlockIncrement(1);
        brushSizeSlider.setSnapToTicks(true);

        Label brushValueLabel = new Label(String.valueOf((int)brushSizeSlider.getValue()));
        brushSizeSlider.valueProperty().addListener((obs, ov, nv) -> {
            brushValueLabel.setText(String.valueOf(nv.intValue()));
        });


        HBox brushSizeControls = new HBox(5, brushSizeLabel, brushSizeSlider, brushValueLabel);
        brushSizeControls.setAlignment(Pos.CENTER_LEFT);

        Runnable updateCanvas = () -> {
            int index = currentSpriteIndex[0];
            int zoom = currentZoom[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (sprite == null) {
                System.err.println("Error: Sprite is null for index " + index + " in updateCanvas");
                return;
            }

            if (!originalSprites.containsKey(spriteName) || !originalSprites.get(spriteName).containsKey(index)) {
                storeOriginalSprite(spriteName, index, sprite);
            }

            indexValueLabel.setText(currentSpriteIndex[0] + " / " + (totalSprites - 1));

            double canvasWidth = finalMaxWidth * zoom;
            double canvasHeight = finalMaxHeight * zoom;

            mainCanvas.setWidth(canvasWidth);
            mainCanvas.setHeight(canvasHeight);
            previewCanvas.setWidth(canvasWidth);
            previewCanvas.setHeight(canvasHeight);

            previewGc.clearRect(0, 0, canvasWidth, canvasHeight);
            drawScaledSprite(mainGc, sprite, finalMaxWidth, finalMaxHeight, zoom);

            setupCanvasEventHandlers(mainCanvas, mainGc, previewCanvas, previewGc, spriteName, spriteManager,
                    colorPicker, currentSpriteIndex, currentZoom, brushSizeSlider);

            String title = "Sprite Editor - " + spriteName + " [" + index + "] - Zoom: " + zoom + "x";
            if (hasUnsavedChanges) {
                title += " *";
            }
            editorStage.setTitle(title);
            zoomValueLabel.setText(zoom + "x");

            prevButton.setDisable(index == 0);
            nextButton.setDisable(index == totalSprites - 1);
        };

        Runnable changeIndex = () -> {
            int oldIndex = currentSpriteIndex[0];
            int newIndex = requestedSpriteIndex[0];

            prevButton.setDisable(true);
            nextButton.setDisable(true);

            if (oldIndex == newIndex) {
                prevButton.setDisable(oldIndex == 0);
                nextButton.setDisable(oldIndex == totalSprites - 1);
                return;
            }

            if (hasUnsavedChanges) {
                ButtonType result = showSaveConfirmDialog(oldIndex);

                if (result.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    requestedSpriteIndex[0] = oldIndex;
                    prevButton.setDisable(oldIndex == 0);
                    nextButton.setDisable(oldIndex == totalSprites - 1);
                    return;
                } else if (result.getText().equals("Save")) {
                    WritableImage spriteToSave = spriteManager.getSprite(spriteName, oldIndex);
                    if (spriteToSave != null) {
                        saveSprite(spriteName, oldIndex, spriteToSave);
                    } else {
                        System.err.println("Error: Cannot save null sprite at index " + oldIndex);
                        restoreOriginalSprite(spriteName, oldIndex, spriteManager);
                    }
                } else {
                    restoreOriginalSprite(spriteName, oldIndex, spriteManager);
                }
            }

            currentSpriteIndex[0] = newIndex;
            hasUnsavedChanges = false;
            updateCanvas.run();
        };

        prevButton.setOnAction(e -> {
            if (currentSpriteIndex[0] > 0) {
                requestedSpriteIndex[0] = currentSpriteIndex[0] - 1;
                changeIndex.run();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentSpriteIndex[0] < totalSprites - 1) {
                requestedSpriteIndex[0] = currentSpriteIndex[0] + 1;
                changeIndex.run();
            }
        });

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() != oldVal.intValue()) {
                currentZoom[0] = newVal.intValue();
                updateCanvas.run();
            }
        });

        colorPickerTool.selectedProperty().addListener((obs, oldVal, newVal) -> {
            colorPickerMode = newVal;
            previewGc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        });

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);
            if (sprite != null) {
                saveSprite(spriteName, index, sprite);
                updateCanvas.run();
            }
        });

        Button revertButton = new Button("Revert Changes");
        revertButton.setOnAction(e -> {
            int index = currentSpriteIndex[0];
            restoreOriginalSprite(spriteName, index, spriteManager);
            updateCanvas.run();
        });

        HBox zoomControls = new HBox(10, zoomLabel, zoomSlider, zoomValueLabel);
        zoomControls.setAlignment(Pos.CENTER_LEFT);

        HBox navigationControls = new HBox(5, prevButton, indexValueLabel, nextButton);
        navigationControls.setAlignment(Pos.CENTER);

        HBox spriteControls = new HBox(10, navigationControls, saveButton, revertButton);
        spriteControls.setAlignment(Pos.CENTER_LEFT);

        HBox bottomControls = new HBox(20, spriteControls, zoomControls);
        bottomControls.setPadding(new Insets(10));
        bottomControls.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(zoomControls, Priority.ALWAYS);

        VBox toolPanel = new VBox(10);
        toolPanel.setPadding(new Insets(10));
        toolPanel.getChildren().addAll(
                new Label("Color:"),
                colorPicker,
                new Separator(),
                colorPickerTool,
                new Separator(),
                brushSizeControls
        );

        ScrollPane scrollPane = new ScrollPane(canvasPane);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        toolPanel.setPrefWidth(200);
        toolPanel.setMinWidth(200);

        HBox contentLayout = new HBox(scrollPane, toolPanel);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(toolPanel, Priority.NEVER);

        VBox root = new VBox(contentLayout, bottomControls);
        VBox.setVgrow(contentLayout, Priority.ALWAYS);

        setupCanvasEventHandlers(mainCanvas, mainGc, previewCanvas, previewGc, spriteName, spriteManager,
                colorPicker, currentSpriteIndex, currentZoom, brushSizeSlider);

        Scene scene = new Scene(root, 950, 700);
        editorStage.setScene(scene);

        editorStage.setOnCloseRequest(e -> {
            if (hasUnsavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You may have unsaved changes.");
                alert.setContentText("Closing the editor might discard changes.\nWould you like to save the currently visible sprite (" + currentSpriteIndex[0] + ") before closing, or discard all changes?");

                ButtonType saveCurrentButton = new ButtonType("Save Current");
                ButtonType discardAllButton = new ButtonType("Discard All Changes");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(saveCurrentButton, discardAllButton, cancelButton);

                ButtonType result = alert.showAndWait().orElse(cancelButton);

                if (result == saveCurrentButton) {
                    int index = currentSpriteIndex[0];
                    WritableImage sprite = spriteManager.getSprite(spriteName, index);
                    if (sprite != null) {
                        saveSprite(spriteName, index, sprite);
                    }
                } else if (result == discardAllButton) {
                    if (originalSprites.containsKey(spriteName)) {
                        originalSprites.get(spriteName).forEach((idx, originalImg) -> {
                            WritableImage current = spriteManager.getSprite(spriteName, idx);
                            if (current != null && originalImg != null && !areImagesEqual(current, originalImg)) {
                                System.out.println("Discarding changes for index: " + idx);
                                restoreOriginalSprite(spriteName, idx, spriteManager);
                            }
                        });
                    }
                    hasUnsavedChanges = false;
                } else {
                    e.consume();
                }
            }
        });
        updateCanvas.run();
        editorStage.show();
    }

    private static boolean areImagesEqual(WritableImage img1, WritableImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false;
        }
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (!img1.getPixelReader().getColor(x, y).equals(img2.getPixelReader().getColor(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }


    private static Canvas createCanvas(int width, int height, int zoom) {
        double canvasWidth = Math.max(1.0, (double)width * zoom);
        double canvasHeight = Math.max(1.0, (double)height * zoom);
        return new Canvas(canvasWidth, canvasHeight);
    }
    private static void drawBrushPreview(GraphicsContext previewGc, WritableImage sprite, int zoom, int brushSize, int centerX, int centerY) {
        if (colorPickerMode || sprite == null || previewGc == null) {
            previewGc.clearRect(0, 0, previewGc.getCanvas().getWidth(), previewGc.getCanvas().getHeight());
            return;
        }

        previewGc.clearRect(0, 0, previewGc.getCanvas().getWidth(), previewGc.getCanvas().getHeight());

        int startX = centerX - (brushSize - 1) / 2;
        int startY = centerY - (brushSize - 1) / 2;

        previewGc.setStroke(PREVIEW_OUTLINE_COLOR);
        previewGc.setLineWidth(zoom > 4 ? 2.0 : 1.0);

        for (int x = 0; x < brushSize; x++) {
            for (int y = 0; y < brushSize; y++) {
                int pixelX = startX + x;
                int pixelY = startY + y;
                if (pixelX >= 0 && pixelX < sprite.getWidth() &&
                        pixelY >= 0 && pixelY < sprite.getHeight()) {
                    double drawX = pixelX * zoom + (previewGc.getLineWidth() / 2.0);
                    double drawY = pixelY * zoom + (previewGc.getLineWidth() / 2.0);
                    double drawW = zoom - previewGc.getLineWidth();
                    double drawH = zoom - previewGc.getLineWidth();

                    if (drawW > 0 && drawH > 0) {
                        previewGc.strokeRect(drawX, drawY, drawW, drawH);
                    } else {
                        previewGc.strokeLine(drawX, drawY, drawX, drawY);
                    }
                }
            }
        }
    }

    private static void setupCanvasEventHandlers(Canvas mainCanvas, GraphicsContext mainGc,
                                                 Canvas previewCanvas, GraphicsContext previewGc,
                                                 String spriteName, SpriteManager spriteManager,
                                                 ColorPicker colorPicker, int[] currentSpriteIndex,
                                                 int[] currentZoom, Slider brushSizeSlider) {
        previewCanvas.setOnMouseMoved(e -> {
            int zoom = currentZoom[0];
            int brushSize = (int) brushSizeSlider.getValue();
            int centerX = (int) (e.getX() / zoom);
            int centerY = (int) (e.getY() / zoom);
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);
            drawBrushPreview(previewGc, sprite, zoom, brushSize, centerX, centerY);

            e.consume();
        });

        previewCanvas.setOnMouseExited(e -> {
            previewGc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
            e.consume();
        });

        previewCanvas.setOnMouseDragged(e -> {
            int zoom = currentZoom[0];
            int brushSize = (int) brushSizeSlider.getValue();
            int centerX = (int) (e.getX() / zoom);
            int centerY = (int) (e.getY() / zoom);
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (sprite == null) return;

            drawBrushPreview(previewGc, sprite, zoom, brushSize, centerX, centerY);

            if (colorPickerMode) {
                if (centerX >= 0 && centerX < sprite.getWidth() && centerY >= 0 && centerY < sprite.getHeight()) {
                    Color pickedColor = sprite.getPixelReader().getColor(centerX, centerY);
                    colorPicker.setValue(pickedColor);
                }
            } else {
                Color newColor = colorPicker.getValue();
                boolean madeChanges = false;

                int startX = centerX - (brushSize - 1) / 2;
                int startY = centerY - (brushSize - 1) / 2;

                for (int x = 0; x < brushSize; x++) {
                    for (int y = 0; y < brushSize; y++) {
                        int pixelX = startX + x;
                        int pixelY = startY + y;

                        if (applyBrushToPixel(mainGc, sprite, pixelX, pixelY, newColor, zoom)) {
                            madeChanges = true;
                        }
                    }
                }

                if (madeChanges && !hasUnsavedChanges) {
                    hasUnsavedChanges = true;
                    Stage stage = (Stage) previewCanvas.getScene().getWindow();
                    if (stage != null && !stage.getTitle().endsWith(" *")) {
                        stage.setTitle(stage.getTitle() + " *");
                    }
                }
            }
            e.consume();
        });

        previewCanvas.setOnMouseClicked(e -> {
            int zoom = currentZoom[0];
            int brushSize = (int) brushSizeSlider.getValue();
            int centerX = (int) (e.getX() / zoom);
            int centerY = (int) (e.getY() / zoom);
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (sprite == null) return;

            drawBrushPreview(previewGc, sprite, zoom, brushSize, centerX, centerY);

            if (colorPickerMode) {
                if (centerX >= 0 && centerX < sprite.getWidth() && centerY >= 0 && centerY < sprite.getHeight()) {
                    Color pickedColor = sprite.getPixelReader().getColor(centerX, centerY);
                    colorPicker.setValue(pickedColor);
                }
            } else {
                Color newColor = colorPicker.getValue();
                boolean madeChanges = false;

                int startX = centerX - (brushSize - 1) / 2;
                int startY = centerY - (brushSize - 1) / 2;

                for (int x = 0; x < brushSize; x++) {
                    for (int y = 0; y < brushSize; y++) {
                        int pixelX = startX + x;
                        int pixelY = startY + y;

                        if (applyBrushToPixel(mainGc, sprite, pixelX, pixelY, newColor, zoom)) {
                            madeChanges = true;
                        }
                    }
                }

                if (madeChanges && !hasUnsavedChanges) {
                    hasUnsavedChanges = true;
                    Stage stage = (Stage) previewCanvas.getScene().getWindow();
                    if (stage != null && !stage.getTitle().endsWith(" *")) {
                        stage.setTitle(stage.getTitle() + " *");
                    }
                }
            }
            e.consume();
        });
    }
}
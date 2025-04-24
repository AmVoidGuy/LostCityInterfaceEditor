package org.lostcityinterfaceeditor;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
    private static boolean colorPickerMode = false;
    private static Map<String, Map<Integer, WritableImage>> originalSprites = new HashMap<>();
    private static boolean hasUnsavedChanges = false;

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
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setStroke(Color.LIGHTGRAY);

        for (int x = 0; x <= maxWidth; x++) {
            gc.strokeLine(x * zoom, 0, x * zoom, maxHeight * zoom);
        }
        for (int y = 0; y <= maxHeight; y++) {
            gc.strokeLine(0, y * zoom, maxWidth * zoom, y * zoom);
        }

        if (sprite != null) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                for (int y = 0; y < sprite.getHeight(); y++) {
                    Color color = sprite.getPixelReader().getColor(x, y);
                    gc.setFill(color);
                    gc.fillRect(x * zoom, y * zoom, zoom, zoom);
                }
            }
        }
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
        originalSprites.computeIfAbsent(spriteName, k -> new HashMap<>());
        originalSprites.get(spriteName).put(index, createBackupSprite(sprite));
    }

    private static void restoreOriginalSprite(String spriteName, int index, SpriteManager spriteManager) {
        if (originalSprites.containsKey(spriteName) && originalSprites.get(spriteName).containsKey(index)) {
            WritableImage original = originalSprites.get(spriteName).get(index);
            WritableImage current = spriteManager.getSprite(spriteName, index);

            int width = (int) original.getWidth();
            int height = (int) original.getHeight();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = original.getPixelReader().getColor(x, y);
                    current.getPixelWriter().setColor(x, y, color);
                }
            }
            hasUnsavedChanges = false;
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
            maxWidth = Math.max(maxWidth, (int)img.getWidth());
            maxHeight = Math.max(maxHeight, (int)img.getHeight());
        }

        originalSprites.remove(spriteName);
        hasUnsavedChanges = false;

        int initialZoom = calculateInitialZoom(maxWidth, maxHeight);
        final int[] currentZoom = {initialZoom};
        final int[] currentSpriteIndex = {0};
        final int[] requestedSpriteIndex = {0};

        WritableImage initialSprite = spriteManager.getSprite(spriteName, currentSpriteIndex[0]);
        storeOriginalSprite(spriteName, currentSpriteIndex[0], initialSprite);

        VBox canvasContainer = new VBox();
        canvasContainer.setAlignment(Pos.CENTER);

        Canvas canvas = createCanvas(maxWidth, maxHeight, currentZoom[0]);
        canvasContainer.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
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

        Runnable updateCanvas = () -> {
            int index = currentSpriteIndex[0];
            int zoom = currentZoom[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (!originalSprites.containsKey(spriteName) || !originalSprites.get(spriteName).containsKey(index)) {
                storeOriginalSprite(spriteName, index, sprite);
            }

            indexValueLabel.setText(currentSpriteIndex[0] + " / " + (totalSprites - 1));

            Canvas newCanvas = createCanvas(finalMaxWidth, finalMaxHeight, zoom);
            GraphicsContext newGc = newCanvas.getGraphicsContext2D();

            setupCanvasEventHandlers(newCanvas, newGc, spriteName, spriteManager, colorPicker, currentSpriteIndex, currentZoom);

            canvasContainer.getChildren().clear();
            canvasContainer.getChildren().add(newCanvas);

            drawScaledSprite(newGc, sprite, finalMaxWidth, finalMaxHeight, zoom);

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
                    WritableImage sprite = spriteManager.getSprite(spriteName, oldIndex);
                    saveSprite(spriteName, oldIndex, sprite);
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
            currentZoom[0] = newVal.intValue();
            updateCanvas.run();
        });

        colorPickerTool.selectedProperty().addListener((obs, oldVal, newVal) -> {
            colorPickerMode = newVal;
        });

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);
            saveSprite(spriteName, index, sprite);
            updateCanvas.run();
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
        spriteControls.setAlignment(Pos.CENTER);

        HBox controls = new HBox(20, spriteControls, zoomControls);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        VBox toolPanel = new VBox(10);
        toolPanel.setPadding(new Insets(10));
        toolPanel.getChildren().addAll(
                new Label("Color:"),
                colorPicker,
                new Separator(),
                colorPickerTool
        );
        ScrollPane scrollPane = new ScrollPane(canvasContainer);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        toolPanel.setPrefWidth(200);
        toolPanel.setMinWidth(200);
        HBox contentLayout = new HBox(scrollPane, toolPanel);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        HBox.setHgrow(toolPanel, Priority.NEVER);

        VBox root = new VBox(contentLayout, controls);
        VBox.setVgrow(contentLayout, Priority.ALWAYS);

        setupCanvasEventHandlers(canvas, gc, spriteName, spriteManager, colorPicker, currentSpriteIndex, currentZoom);

        Scene scene = new Scene(root, 800, 600);
        editorStage.setScene(scene);

        editorStage.setOnCloseRequest(e -> {
            if (hasUnsavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes");
                alert.setContentText("Would you like to save your changes before closing?");

                ButtonType saveButton2 = new ButtonType("Save");
                ButtonType dontSaveButton = new ButtonType("Don't Save");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(saveButton2, dontSaveButton, cancelButton);

                ButtonType result = alert.showAndWait().orElse(cancelButton);

                if (result == saveButton2) {
                    int index = currentSpriteIndex[0];
                    WritableImage sprite = spriteManager.getSprite(spriteName, index);
                    saveSprite(spriteName, index, sprite);
                } else if (result == dontSaveButton) {
                    int index = currentSpriteIndex[0];
                    restoreOriginalSprite(spriteName, index, spriteManager);
                } else if (result == cancelButton) {
                    e.consume();
                }
            }
        });
        updateCanvas.run();
        editorStage.show();
    }

    private static Canvas createCanvas(int width, int height, int zoom) {
        return new Canvas(width * zoom, height * zoom);
    }

    private static void setupCanvasEventHandlers(Canvas canvas, GraphicsContext gc,
                                                 String spriteName, SpriteManager spriteManager,
                                                 ColorPicker colorPicker, int[] currentSpriteIndex, int[] currentZoom) {
        canvas.setOnMouseDragged(e -> {
            int zoom = currentZoom[0];
            int x = (int) (e.getX() / zoom);
            int y = (int) (e.getY() / zoom);
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (x >= 0 && x < sprite.getWidth() && y >= 0 && y < sprite.getHeight()) {
                if (colorPickerMode) {
                    Color pickedColor = sprite.getPixelReader().getColor(x, y);
                    colorPicker.setValue(pickedColor);
                } else {
                    Color currentColor = sprite.getPixelReader().getColor(x, y);
                    Color newColor = colorPicker.getValue();

                    if (!currentColor.equals(newColor)) {
                        sprite.getPixelWriter().setColor(x, y, newColor);
                        gc.setFill(newColor);
                        gc.fillRect(x * zoom, y * zoom, zoom, zoom);
                        hasUnsavedChanges = true;

                        Stage stage = (Stage) canvas.getScene().getWindow();
                        if (!stage.getTitle().endsWith(" *")) {
                            stage.setTitle(stage.getTitle() + " *");
                        }
                    }
                }
            }
            e.consume();
        });

        canvas.setOnMouseClicked(e -> {
            int zoom = currentZoom[0];
            int x = (int) (e.getX() / zoom);
            int y = (int) (e.getY() / zoom);
            int index = currentSpriteIndex[0];
            WritableImage sprite = spriteManager.getSprite(spriteName, index);

            if (x >= 0 && x < sprite.getWidth() && y >= 0 && y < sprite.getHeight()) {
                if (colorPickerMode) {
                    Color pickedColor = sprite.getPixelReader().getColor(x, y);
                    colorPicker.setValue(pickedColor);
                } else {
                    Color currentColor = sprite.getPixelReader().getColor(x, y);
                    Color newColor = colorPicker.getValue();

                    if (!currentColor.equals(newColor)) {
                        sprite.getPixelWriter().setColor(x, y, newColor);
                        gc.setFill(newColor);
                        gc.fillRect(x * zoom, y * zoom, zoom, zoom);
                        hasUnsavedChanges = true;

                        Stage stage = (Stage) canvas.getScene().getWindow();
                        if (!stage.getTitle().endsWith(" *")) {
                            stage.setTitle(stage.getTitle() + " *");
                        }
                    }
                }
            }
            e.consume();
        });

        canvas.setOnMousePressed(Event::consume);
    }
}
package org.lostcityinterfaceeditor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lostcityinterfaceeditor.baseCode.Model;
import org.lostcityinterfaceeditor.baseCode.Pix3D;
import org.lostcityinterfaceeditor.fileUtils.InterfaceFileParser;
import org.lostcityinterfaceeditor.fileUtils.InterfaceFileWriter;
import org.lostcityinterfaceeditor.helpers.CustomFontHelper;
import org.lostcityinterfaceeditor.loaders.AssetLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

public class LostCityInterfaceEditor extends Application {

    private Map<String, Double> originalViewOrderMap = new HashMap<>();
    private ImageView chatbackImageView;
    private ImageView mapbackImageView;
    private ImageView backbase1ImageView;
    private ImageView backbase2ImageView;
    private ImageView backhmid1ImageView;
    private ImageView backleft1ImageView;
    private ImageView backleft2ImageView;
    private ImageView backright1ImageView;
    private ImageView backright2ImageView;
    private ImageView backtop1ImageView;
    private ImageView backtop2ImageView;
    private ImageView backvmid1ImageView;
    private ImageView backvmid2ImageView;
    private ImageView backvmid3ImageView;
    private ImageView backhmid2ImageView;
    private ImageView compassImageView;
    private ImageView invbackImageView;
    private Pane areaInvback;
    private Pane areaMinimap;
    private Pane areaCompass;
    private Pane areaBackleft1;
    private Pane areaBackleft2;
    private Pane areaBackright1;
    private Pane areaBackright2;
    private Pane areaBacktop1;
    private Pane areaBacktop2;
    private Pane areaBackvmid1;
    private Pane areaBackvmid2;
    private Pane areaBackvmid3;
    private Pane areaBackhmid2;
    private Pane areaChatback;
    private Pane areaMapback;
    private Pane areaBackbase1;
    private Pane areaBackbase2;
    private Pane areaBackhmid1;
    private Pane areaViewport;
    private Pane areaSidebar;
    private Canvas tooltipCanvas;
    private Pane tooltipPane;
    private GraphicsContext gc;
    private AnchorPane root;
    private Canvas backgroundCanvas;
    private Scene scene;
    private List<InterfaceComponent> interfaceComponents;
    private String activeComponentName = null;
    private Map<String, EventHandler<MouseEvent>> originalClickHandlers = new HashMap<>();
    public static String serverDirectoryPath;
    private TreeView<String> componentTreeView;
    private VBox sidebarVBox;
    private VBox propertiesSidebarVBox;
    private Pane selectedComponentHighlight;
    private double dragStartX, dragStartY;
    private boolean isDragging = false;
    private Pane currentlyDraggablePane = null;
    private InterfaceComponent currentlyDraggableComponent = null;
    private String currentLoadedFile;
    private Map<String, TextRenderInfo> textRenderInfoMap = new HashMap<>();
    private AssetLoader assetLoader;
    private ScrollPane propertiesScrollPane;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            serverDirectoryPath = AssetLoader.chooseServerDirectory(stage);
            if (serverDirectoryPath == null) {
                System.err.println("No server directory selected. Exiting.");
                return;
            }

            assetLoader = new AssetLoader(serverDirectoryPath);
            assetLoader.loadAll();
            initializeUIComponents();
            initializeSidebar();
            initializePropertiesSidebar();
            initializeSelectionHighlight();
            stage.setTitle("Lost City Interface Editor");
            stage.setScene(scene);
            stage.show();
            populateComponentTreeView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializePropertiesSidebar() {
        propertiesSidebarVBox = new VBox();
        propertiesSidebarVBox.setPrefWidth(300);
        propertiesSidebarVBox.setPadding(new Insets(10));
        propertiesSidebarVBox.setStyle("-fx-background-color: #f0f0f0;");

        propertiesScrollPane = new ScrollPane();
        propertiesScrollPane.setContent(propertiesSidebarVBox);
        propertiesScrollPane.setFitToWidth(true);
        propertiesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propertiesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.getChildren().add(propertiesScrollPane);

        AnchorPane.setTopAnchor(propertiesScrollPane, 0.0);
        AnchorPane.setBottomAnchor(propertiesScrollPane, 0.0);
        if (sidebarVBox != null) {
            AnchorPane.setRightAnchor(propertiesScrollPane, sidebarVBox.getPrefWidth());
        } else {
            AnchorPane.setRightAnchor(propertiesScrollPane, 200.0);
            System.err.println("Warning: sidebarVBox was null during properties sidebar initialization.");
        }
    }

    private void populatePropertiesSidebar(InterfaceComponent component) {
        propertiesSidebarVBox.getChildren().clear();

        if (component == null) {
            Label noSelectionLabel = new Label("No component selected.");
            propertiesSidebarVBox.getChildren().add(noSelectionLabel);
            return;
        }

        addSectionHeader(propertiesSidebarVBox, "General Properties");

        addReadOnlyProperty(propertiesSidebarVBox, "Name", component.getName());
        addReadOnlyProperty(propertiesSidebarVBox, "Type", component.getType());
        addEditableProperty(propertiesSidebarVBox, "X", String.valueOf(component.getX()), value -> component.setX(Integer.parseInt(value)));
        addEditableProperty(propertiesSidebarVBox, "Y", String.valueOf(component.getY()), value -> component.setY(Integer.parseInt(value)));
        addEditableProperty(propertiesSidebarVBox, "Width", String.valueOf(component.getWidth()), value -> component.setWidth(Integer.parseInt(value)));
        addEditableProperty(propertiesSidebarVBox, "Height", String.valueOf(component.getHeight()), value -> component.setHeight(Integer.parseInt(value)));
        addEditableProperty(propertiesSidebarVBox, "Layer", component.getLayer(), value -> component.setLayer(value));
        addEditableProperty(propertiesSidebarVBox, "Overlayer", component.getOverlayer(), value -> component.setOverlayer(value));
        addEditableProperty(propertiesSidebarVBox, "Option", component.getOption(), value -> component.setOption(value));
        addEditableProperty(propertiesSidebarVBox, "Button Type", component.getButtonType(), value -> component.setButtonType(value));
        addEditableProperty(propertiesSidebarVBox, "Client Code", String.valueOf(component.getClientCode()),
                value -> component.setClientCode(Integer.parseInt(value)));

        if (component.getButtonType() != null && component.getButtonType().contains("target")) {
            addEditableProperty(propertiesSidebarVBox, "Action Verb", component.getActionVerb(), value -> component.setActionVerb(value));
            addEditableProperty(propertiesSidebarVBox, "Action Target", component.getActionTarget(), value -> component.setActionTarget(value));
            addEditableProperty(propertiesSidebarVBox, "Action", component.getAction(), value -> component.setAction(value));
        }

        String type = component.getType();
        if ("text".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Text Properties");
            addEditableProperty(propertiesSidebarVBox, "Text", component.getText(), value -> component.setText(value));
            addDropdownProperty(propertiesSidebarVBox, "Font", component.getFont(), List.of("p11", "p12", "b12", "q8"), component::setFont);
            addBooleanProperty(propertiesSidebarVBox, "Center", component.isCenter(), value -> component.setCenter(value));
            addBooleanProperty(propertiesSidebarVBox, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
            addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            addColorProperty(propertiesSidebarVBox, "Over Colour", component.getOverColour(), value -> component.setOverColour(value));
            addColorProperty(propertiesSidebarVBox, "Active Colour", component.getActiveColour(), value -> component.setActiveColour(value));
        } else if ("graphic".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Graphic Properties");
            addDropdownProperty(propertiesSidebarVBox, "Graphic Name", component.getGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setGraphicName);
            addEditableProperty(propertiesSidebarVBox, "Graphic Index", String.valueOf(component.getGraphicIndex()),
                    value -> component.setGraphicIndex(Integer.parseInt(value)));
            addDropdownProperty(propertiesSidebarVBox, "Active Graphic Name", component.getActiveGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setActiveGraphicName);
            addEditableProperty(propertiesSidebarVBox, "Active Graphic Index", String.valueOf(component.getActiveGraphicIndex()),
                    value -> component.setActiveGraphicIndex(Integer.parseInt(value)));
        } else if ("layer".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Layer Properties");
            addBooleanProperty(propertiesSidebarVBox, "Hide", component.isHide(), value -> component.setHide(value));
            addEditableProperty(propertiesSidebarVBox, "Scroll", String.valueOf(component.getScroll()), value -> component.setScroll(Integer.parseInt(value)));
        } else if ("rect".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Rectangle Properties");
            addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            addBooleanProperty(propertiesSidebarVBox, "Fill", component.isFill(), value -> component.setFill(value));
        } else if ("model".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Model Properties");
            addEditableProperty(propertiesSidebarVBox, "Model", component.getModel(), value -> component.setModel(value));
            addEditableProperty(propertiesSidebarVBox, "Active Model", component.getActiveModel(), value -> component.setActiveModel(value));
            addEditableProperty(propertiesSidebarVBox, "Active Animation", component.getActiveAnim(), value -> component.setActiveAnim(value));
            addEditableProperty(propertiesSidebarVBox, "Xan", String.valueOf(component.getXan()),
                    value -> component.setXan(Integer.parseInt(value)));
            addEditableProperty(propertiesSidebarVBox, "Yan", String.valueOf(component.getYan()),
                    value -> component.setYan(Integer.parseInt(value)));
            addEditableProperty(propertiesSidebarVBox, "Zoom", String.valueOf(component.getZoom()),
                    value -> component.setZoom(Integer.parseInt(value)));
        } else if ("inv".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Inventory Properties");
            addBooleanProperty(propertiesSidebarVBox, "Draggable", component.isDraggable(), value -> component.setDraggable(value));
            addBooleanProperty(propertiesSidebarVBox, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
            addBooleanProperty(propertiesSidebarVBox, "Usable", component.isUsable(), value -> component.setUsable(value));
            addEditableProperty(propertiesSidebarVBox, "Margin", component.getMargin(), value -> component.setMargin(value));
            List<String> invOptions = component.getInvOptions();
            if (invOptions != null) {
                for (int i = 0; i < invOptions.size(); i++) {
                    final int optionIndex = i;
                    String optionValue = invOptions.get(i);
                    String optionLabel = "Option " + (i + 1);
                    addEditableProperty(propertiesSidebarVBox, optionLabel, optionValue, value -> {
                        List<String> options = new ArrayList<>(component.getInvOptions());
                        options.set(optionIndex, value);
                        component.setInvOptions(options);
                    });
                }
            }
        } else if ("invtext".equals(type)) {
            addSectionHeader(propertiesSidebarVBox, "Inventory Text Properties");
            addDropdownProperty(propertiesSidebarVBox, "Font", component.getFont(), List.of("p11", "p12", "b12", "q8"), component::setFont);
            addBooleanProperty(propertiesSidebarVBox, "Center", component.isCenter(), value -> component.setCenter(value));
            addBooleanProperty(propertiesSidebarVBox, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
            addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            addEditableProperty(propertiesSidebarVBox, "Margin", component.getMargin(), value -> component.setMargin(value));
            addBooleanProperty(propertiesSidebarVBox, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
            List<String> invOptions = component.getInvOptions();
            if (invOptions != null) {
                for (int i = 0; i < invOptions.size(); i++) {
                    final int optionIndex = i;
                    String optionValue = invOptions.get(i);
                    String optionLabel = "Option " + (i + 1);
                    addEditableProperty(propertiesSidebarVBox, optionLabel, optionValue, value -> {
                        List<String> options = new ArrayList<>(component.getInvOptions());
                        options.set(optionIndex, value);
                        component.setInvOptions(options);
                    });
                }
            }
        }

        if (component.getScripts() != null && !component.getScripts().isEmpty()) {
            addSectionHeader(propertiesSidebarVBox, "Scripts");
            for (Map.Entry<Integer, InterfaceFileParser.Script> scriptEntry : component.getScripts().entrySet()) {
                final int scriptKey = scriptEntry.getKey();
                String scriptValue = scriptEntry.getValue() != null ? scriptEntry.getValue().toString() : "null";
                addReadOnlyProperty(propertiesSidebarVBox, "Script " + scriptKey, scriptValue);
            }
        }

        Button saveChangesButton = new Button("Apply Changes");
        saveChangesButton.setMaxWidth(Double.MAX_VALUE);
        saveChangesButton.setOnAction(e -> {
            final InterfaceComponent editedComponent = component;

            clearRenderedComponents();
            renderInterfaceComponents();
            populateComponentTreeView();
            selectComponentInTreeView(editedComponent.getName());
            highlightSelectedComponent(editedComponent);
            if (editedComponent != null) {
                Node componentNode = findComponentPane(editedComponent.getName());
                if (componentNode instanceof Pane) {
                    Pane newComponentPane = (Pane) componentNode;
                    makeComponentDraggable(editedComponent, newComponentPane);

                    currentlyDraggablePane = newComponentPane;
                    currentlyDraggableComponent = editedComponent;
                } else {
                    System.err.println("Warning: Could not find the new pane for " + editedComponent.getName() + " after applying changes.");
                    currentlyDraggablePane = null;
                    currentlyDraggableComponent = null;
                }
            }
        });

        VBox.setMargin(saveChangesButton, new Insets(10, 0, 0, 0));
        propertiesSidebarVBox.getChildren().add(saveChangesButton);
    }

    private void addReadOnlyProperty(VBox container, String name, String value) {
        HBox propertyRow = new HBox(5);
        propertyRow.setPadding(new Insets(2, 0, 2, 5));

        Label nameLabel = new Label(name + ":");
        nameLabel.setMinWidth(120);
        nameLabel.setMaxWidth(120);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label valueLabel = new Label(value != null ? value : "null");
        valueLabel.setWrapText(true);

        propertyRow.getChildren().addAll(nameLabel, valueLabel);
        container.getChildren().add(propertyRow);
    }

    private void addEditableProperty(VBox container, String name, String value, Consumer<String> setter) {
        HBox propertyRow = new HBox(5);
        propertyRow.setPadding(new Insets(2, 0, 2, 5));

        Label nameLabel = new Label(name + ":");
        nameLabel.setMinWidth(120);
        nameLabel.setMaxWidth(120);
        nameLabel.setStyle("-fx-font-weight: bold;");

        TextField valueField = new TextField(value != null ? value : "");
        valueField.setPrefWidth(150);

        valueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    setter.accept(valueField.getText());
                } catch (NumberFormatException e) {
                    valueField.setText(value != null ? value : "");
                    showAlert("Invalid input", "Please enter a valid number for " + name);
                }
            }
        });

        valueField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    setter.accept(valueField.getText());
                    valueField.getParent().requestFocus();
                } catch (NumberFormatException e) {
                    valueField.setText(value != null ? value : "");
                    showAlert("Invalid input", "Please enter a valid number for " + name);
                }
            }
        });

        propertyRow.getChildren().addAll(nameLabel, valueField);
        container.getChildren().add(propertyRow);
    }

    private void addDropdownProperty(VBox container, String name, String currentValue, List<String> options, Consumer<String> setter) {
        HBox propertyRow = new HBox(5);
        propertyRow.setPadding(new Insets(2, 0, 2, 5));

        Label nameLabel = new Label(name + ":");
        nameLabel.setMinWidth(120);
        nameLabel.setMaxWidth(120);
        nameLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> valueComboBox = new ComboBox<>(FXCollections.observableArrayList(options));
        valueComboBox.setPrefWidth(150);

        if (currentValue != null && options.contains(currentValue)) {
            valueComboBox.setValue(currentValue);
        } else if (!options.isEmpty()) {
            valueComboBox.setPromptText("Select " + name);
        }

        valueComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                setter.accept(newValue);
            }
        });

        propertyRow.getChildren().addAll(nameLabel, valueComboBox);
        container.getChildren().add(propertyRow);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addBooleanProperty(VBox container, String name, boolean value, Consumer<Boolean> setter) {
        HBox propertyRow = new HBox(5);
        propertyRow.setPadding(new Insets(2, 0, 2, 5));

        Label nameLabel = new Label(name + ":");
        nameLabel.setMinWidth(120);
        nameLabel.setMaxWidth(120);
        nameLabel.setStyle("-fx-font-weight: bold;");

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(value);

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            setter.accept(newValue);
        });

        propertyRow.getChildren().addAll(nameLabel, checkBox);
        container.getChildren().add(propertyRow);
    }

    private void addColorProperty(VBox container, String name, String value, Consumer<String> setter) {
        HBox propertyRow = new HBox(5);
        propertyRow.setPadding(new Insets(2, 0, 2, 5));

        Label nameLabel = new Label(name + ":");
        nameLabel.setMinWidth(120);
        nameLabel.setMaxWidth(120);
        nameLabel.setStyle("-fx-font-weight: bold;");

        ColorPicker colorPicker = new ColorPicker();

        if (value != null && !value.isEmpty()) {
            try {
                String colorValue = value;
                if (value.startsWith("0x")) {
                    colorValue = "#" + value.substring(2);
                } else if (value.matches("\\d+")) {
                    int decimalColor = Integer.parseInt(value);
                    colorValue = String.format("#%06X", decimalColor);
                }
                colorPicker.setValue(Color.web(colorValue));
            } catch (Exception e) {
                System.err.println("Could not parse color value: " + value);
            }
        }

        colorPicker.setOnAction(event -> {
            Color color = colorPicker.getValue();
            String hexColor = String.format("0x%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));
            setter.accept(hexColor);
        });

        propertyRow.getChildren().addAll(nameLabel, colorPicker);
        container.getChildren().add(propertyRow);
    }

    private void addSectionHeader(VBox container, String sectionName) {
        Label sectionLabel = new Label(sectionName);
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 2 0;");
        container.getChildren().add(sectionLabel);

        Separator separator = new Separator();
        separator.setPrefWidth(container.getPrefWidth());
        container.getChildren().add(separator);
    }

    private void initializeSidebar() {
        sidebarVBox = new VBox(10);
        sidebarVBox.setPrefWidth(200);
        sidebarVBox.setPadding(new Insets(10));
        sidebarVBox.setStyle("-fx-background-color: lightgray;");

        Label treeViewLabel = new Label("Interface Components");
        treeViewLabel.setStyle("-fx-font-weight: bold;");

        componentTreeView = new TreeView<>();
        componentTreeView.setRoot(new TreeItem<>("Interface Components"));
        componentTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String componentName = newValue.getValue().split(" ")[0];
                InterfaceComponent selectedComponent = null;
                for (InterfaceComponent component : interfaceComponents) {
                    if (component.getName().equals(componentName)) {
                        selectedComponent = component;
                        break;
                    }
                }
                populatePropertiesSidebar(selectedComponent);
            } else {
                populatePropertiesSidebar(null);
            }
        });

        Label selectionHintLabel = new Label("Select a component to view properties and enable dragging");
        selectionHintLabel.setWrapText(true);
        selectionHintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

        Button addComponentButton = new Button("Add New Component");
        addComponentButton.setMaxWidth(Double.MAX_VALUE);
        addComponentButton.setOnAction(e -> showAddComponentDialog());

        Button loadInterfaceButton = new Button("Load Interface");
        loadInterfaceButton.setMaxWidth(Double.MAX_VALUE);
        loadInterfaceButton.setOnAction(e -> loadInterfaceFile());

        Button saveInterfaceButton = new Button("Save Interface");
        saveInterfaceButton.setMaxWidth(Double.MAX_VALUE);
        saveInterfaceButton.setOnAction(e -> saveInterfaceFile());

        Button spriteEditorButton = new Button("Edit Sprite");
        spriteEditorButton.setMaxWidth(Double.MAX_VALUE);
        spriteEditorButton.setOnAction(event -> LostCitySpriteEditor.openSpriteEditor(assetLoader));

        VBox buttonBox = new VBox(5);
        buttonBox.getChildren().addAll(addComponentButton, loadInterfaceButton, saveInterfaceButton, spriteEditorButton);
        VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0));

        sidebarVBox.getChildren().addAll(treeViewLabel, componentTreeView, selectionHintLabel, buttonBox);
        root.getChildren().add(sidebarVBox);
        AnchorPane.setTopAnchor(sidebarVBox, 0.0);
        AnchorPane.setBottomAnchor(sidebarVBox, 0.0);
        AnchorPane.setRightAnchor(sidebarVBox, 0.0);
    }

    private void showAddComponentDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add New Component");
        dialog.setHeaderText("Select component type");
        ButtonType confirmButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        ComboBox<String> componentTypeCombo = new ComboBox<>();
        componentTypeCombo.getItems().addAll("text", "graphic", "layer", "rect", "model", "inv", "invtext");
        componentTypeCombo.setPromptText("Component Type");
        componentTypeCombo.getSelectionModel().selectFirst();
        TextField nameField = new TextField();
        nameField.setPromptText("Component Name");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Component Type:"), 0, 0);
        grid.add(componentTypeCombo, 1, 0);
        grid.add(new Label("Component Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                if (nameField.getText().isEmpty()) {
                    showAlert("Invalid Input", "Component name cannot be empty");
                    return null;
                }
                return componentTypeCombo.getValue() + ":" + nameField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(typeAndName -> {
            String[] parts = typeAndName.split(":");
            if (parts.length == 2) {
                String type = parts[0];
                String name = parts[1];
                createNewComponent(type, name);
            }
        });
    }

    private void createNewComponent(String type, String name) {
        for (InterfaceComponent component : interfaceComponents) {
            if (component.getName().equals(name)) {
                showAlert("Duplicate Name", "A component with this name already exists");
                return;
            }
        }

        InterfaceComponent newComponent = new InterfaceComponent(name, type, 250, 250, 32, 32, null, null, null, 0, null, null);

        switch (type) {
            case "text":
                newComponent.setText("New Text");
                newComponent.setColour("0x000000");
                newComponent.setFont("p12");
                break;
            case "rect":
                newComponent.setColour("0x000000");
                newComponent.setFill(true);
                break;
            case "graphic":
                newComponent.setGraphicName("miscgraphics");
                newComponent.setGraphicIndex(0);
                break;
            case "inv":
                newComponent.setDraggable(false);
                newComponent.setInteractable(false);
                newComponent.setUsable(false);
                newComponent.setMargin("0,0");
                break;
            case "invtext":
                newComponent.setDraggable(false);
                newComponent.setInteractable(false);
                newComponent.setMargin("0,0");
                newComponent.setColour("0x000000");
                newComponent.setFont("p12");
                newComponent.setCenter(false);
                newComponent.setShadowed(false);
                break;
            case "model":
                newComponent.setModel("model_55_idk_head");
                newComponent.setZoom(1200);
                newComponent.setXan(0);
                newComponent.setYan(200);
                break;
        }

        interfaceComponents.add(newComponent);
        clearRenderedComponents();
        renderInterfaceComponents();
        populateComponentTreeView();

        selectComponentInTreeView(name);
    }

    private void updateInterfacePackFilesIfNeeded(File savedInterfaceFile, List<InterfaceComponent> componentsToRegister) {
        if (serverDirectoryPath == null || serverDirectoryPath.isEmpty() || savedInterfaceFile == null || componentsToRegister == null) {
            System.out.println("Skipping pack file update: Missing server directory, saved file, or component list.");
            return;
        }

        Path packFilePath = Paths.get(serverDirectoryPath, "pack", "interface.pack");
        Path orderFilePath = Paths.get(serverDirectoryPath, "pack", "interface.order");
        Path interfaceDirectoryPath = savedInterfaceFile.getParentFile().toPath();
        Path expectedInterfaceBasePath = Paths.get(serverDirectoryPath);

        if (!interfaceDirectoryPath.startsWith(expectedInterfaceBasePath)) {
            System.out.println("Skipping pack file update: Saved file '" + savedInterfaceFile.getName() + "' is not within the expected server directory structure.");
            return;
        }

        String interfaceFileName = savedInterfaceFile.getName();
        if (interfaceFileName.toLowerCase().endsWith(".if")) {
            interfaceFileName = interfaceFileName.substring(0, interfaceFileName.length() - 3);
        }

        System.out.println("Checking and potentially updating pack files for interface: " + interfaceFileName);

        try {
            int nextNumber = 0;
            Set<String> existingEntries = new HashSet<>();
            if (Files.exists(packFilePath)) {
                List<String> packLines = Files.readAllLines(packFilePath);
                for (String line : packLines) {
                    line = line.trim();
                    if (line.isEmpty() || !line.contains("=")) continue;
                    try {
                        int number = Integer.parseInt(line.substring(0, line.indexOf('=')));
                        if (number >= nextNumber) {
                            nextNumber = number + 1;
                        }
                        String entry = line.substring(line.indexOf('=') + 1);
                        existingEntries.add(entry);
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                        System.err.println("Warning: Skipping malformed line in interface.pack: " + line);
                    }
                }
            } else {
                Files.createDirectories(packFilePath.getParent());
                Files.createFile(packFilePath);
            }

            if (!Files.exists(orderFilePath)) {
                Files.createDirectories(orderFilePath.getParent());
                Files.createFile(orderFilePath);
            }


            List<String> linesToAddPack = new ArrayList<>();
            List<String> linesToAddOrder = new ArrayList<>();
            boolean addedAny = false;

            for (InterfaceComponent component : componentsToRegister) {
                if (component == null || component.getName() == null || component.getName().trim().isEmpty()) {
                    continue;
                }

                String targetEntry = interfaceFileName + ":" + component.getName();

                if (!existingEntries.contains(targetEntry)) {
                    String newPackEntry = nextNumber + "=" + targetEntry;
                    linesToAddPack.add(newPackEntry);
                    linesToAddOrder.add(String.valueOf(nextNumber));

                    System.out.println("Adding to pack files: " + newPackEntry);
                    existingEntries.add(targetEntry);
                    nextNumber++;
                    addedAny = true;
                }
            }

            if (addedAny) {
                boolean packNeedsLeadingNewline = false;
                if (Files.exists(packFilePath) && Files.size(packFilePath) > 0) {
                    try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(packFilePath.toFile(), "r")) {
                        raf.seek(Files.size(packFilePath) - 1);
                        if (raf.readByte() != '\n') {
                            packNeedsLeadingNewline = true;
                        }
                    }
                } else if (!Files.exists(packFilePath) && !linesToAddPack.isEmpty()) {
                    packNeedsLeadingNewline = false;
                } else if (Files.exists(packFilePath) && Files.size(packFilePath) == 0 && !linesToAddPack.isEmpty()){
                    packNeedsLeadingNewline = false;
                }

                List<String> finalPackLines = new ArrayList<>();
                if (packNeedsLeadingNewline && !linesToAddPack.isEmpty()) {
                    finalPackLines.add("\n" + linesToAddPack.get(0));
                    finalPackLines.addAll(linesToAddPack.subList(1, linesToAddPack.size()));
                } else {
                    finalPackLines.addAll(linesToAddPack);
                }
                Files.write(packFilePath, finalPackLines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);

                boolean orderNeedsLeadingNewline = false;
                if (Files.exists(orderFilePath) && Files.size(orderFilePath) > 0) {
                    try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(orderFilePath.toFile(), "r")) {
                        raf.seek(Files.size(orderFilePath) - 1);
                        if (raf.readByte() != '\n') {
                            orderNeedsLeadingNewline = true;
                        }
                    }
                } else if (!Files.exists(orderFilePath) && !linesToAddOrder.isEmpty()) {
                    orderNeedsLeadingNewline = false;
                } else if (Files.exists(orderFilePath) && Files.size(orderFilePath) == 0 && !linesToAddOrder.isEmpty()){
                    orderNeedsLeadingNewline = false;
                }

                List<String> finalOrderLines = new ArrayList<>();
                if (orderNeedsLeadingNewline && !linesToAddOrder.isEmpty()) {
                    finalOrderLines.add("\n" + linesToAddOrder.get(0));
                    finalOrderLines.addAll(linesToAddOrder.subList(1, linesToAddOrder.size()));
                } else {
                    finalOrderLines.addAll(linesToAddOrder);
                }

                Files.write(orderFilePath, finalOrderLines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);


                System.out.println("Successfully updated interface pack files.");
            } else {
                System.out.println("No new component entries needed for pack files.");
            }
        } catch (IOException e) {
            System.err.println("Error updating interface pack files: " + e.getMessage());
            e.printStackTrace();
            showAlert("Pack File Error", "Could not update interface.pack/interface.order: " + e.getMessage());
        }
    }

    private void selectComponentInTreeView(String componentName) {
        TreeItem<String> root = componentTreeView.getRoot();
        selectComponentInTreeView(root, componentName);
    }

    private boolean selectComponentInTreeView(TreeItem<String> item, String componentName) {
        if (item.getValue().startsWith(componentName + " ")) {
            componentTreeView.getSelectionModel().select(item);
            return true;
        }

        for (TreeItem<String> child : item.getChildren()) {
            if (selectComponentInTreeView(child, componentName)) {
                return true;
            }
        }

        return false;
    }

    private void loadInterfaceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Interface File");

        if (serverDirectoryPath != null && !serverDirectoryPath.isEmpty()) {
            File initialDirectory = new File(serverDirectoryPath + "/scripts/");
            if (initialDirectory.exists() && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
        }

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Interface Files", "*.if"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                loadInterfaceComponents(selectedFile.getAbsolutePath());
                renderInterfaceComponents();
                populateComponentTreeView();
                currentLoadedFile = selectedFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Loading File");
                alert.setHeaderText(null);
                alert.setContentText("Error loading interface file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void saveInterfaceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Interface File");

        File initialDir = null;
        if (currentLoadedFile != null && !currentLoadedFile.isEmpty()) {
            File currentFile = new File(currentLoadedFile);
            initialDir = currentFile.getParentFile();
            fileChooser.setInitialFileName(currentFile.getName());
        } else if (serverDirectoryPath != null && !serverDirectoryPath.isEmpty()) {
            initialDir = new File(serverDirectoryPath);
        }
        if (initialDir != null && initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        if(fileChooser.getInitialFileName() == null) {
            fileChooser.setInitialFileName("untitled.if");
        }


        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Interface Files", "*.if"));

        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".if")) {
                    selectedFile = new File(filePath + ".if");
                }

                InterfaceFileWriter.writeInterfaceFile(interfaceComponents, selectedFile.getAbsolutePath());
                currentLoadedFile = selectedFile.getAbsolutePath();

                updateInterfacePackFilesIfNeeded(selectedFile, interfaceComponents);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File Saved");
                alert.setHeaderText(null);
                alert.setContentText("Interface file was successfully saved to:\n" + selectedFile.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Saving File");
                alert.setHeaderText(null);
                alert.setContentText("Error saving interface file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void populateComponentTreeView() {
        if (interfaceComponents == null || interfaceComponents.isEmpty()) {
            return;
        }

        Map<String, TreeItem<String>> itemMap = new HashMap<>();
        TreeItem<String> rootItem = new TreeItem<>("Interface Components");
        componentTreeView.setMaxHeight(300.0);
        itemMap.put("root", rootItem);

        for (InterfaceComponent component : interfaceComponents) {
            if (component == null) continue;
            TreeItem<String> newItem = new TreeItem<>(component.getName() + " (" + component.getType() + ")");
            itemMap.put(component.getName(), newItem);

            if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                TreeItem<String> parentItem = itemMap.get(component.getLayer());
                if (parentItem != null) {
                    parentItem.getChildren().add(newItem);
                } else {
                    rootItem.getChildren().add(newItem);
                    System.out.println("Parent not found for component: " + component.getName());
                }
            } else {
                rootItem.getChildren().add(newItem);
            }
        }

        componentTreeView.setRoot(rootItem);
        rootItem.setExpanded(true);

        componentTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if (currentlyDraggablePane != null) {
                removeDraggable(currentlyDraggablePane, currentlyDraggableComponent);
                currentlyDraggablePane = null;
                currentlyDraggableComponent = null;
            }

            if (newValue != null) {
                String componentName = newValue.getValue().split(" ")[0];
                InterfaceComponent selectedComponent = findComponentByName(componentName);
                Node componentNode = findComponentPane(componentName);

                if (selectedComponent != null && componentNode instanceof Pane) {
                    Pane componentPane = (Pane) componentNode;

                    populatePropertiesSidebar(selectedComponent);
                    highlightSelectedComponent(selectedComponent);

                    makeComponentDraggable(selectedComponent, componentPane);
                    currentlyDraggablePane = componentPane;
                    currentlyDraggableComponent = selectedComponent;

                } else {
                    System.err.println("Warning: Could not find component or pane for tree item: " + newValue.getValue());
                    populatePropertiesSidebar(null);
                    selectedComponentHighlight.setVisible(false);
                }
            } else {
                populatePropertiesSidebar(null);
                selectedComponentHighlight.setVisible(false);
            }
        });
    }

    private void highlightSelectedComponent(InterfaceComponent component) {
        if (component == null) {
            selectedComponentHighlight.setVisible(false);
            return;
        }

        Node componentNode = findComponentPane(component.getName());
        if (componentNode instanceof Pane) {
            Pane componentPane = (Pane) componentNode;

            Point2D sceneCoords = componentPane.localToScene(0, 0);
            Point2D rootCoords = root.sceneToLocal(sceneCoords);

            double x = rootCoords.getX();
            double y = rootCoords.getY();
            double width = component.getWidth();
            double height = component.getHeight();

            if ("graphic".equals(component.getType())) {

                if (width <= 0 || height <= 0) {
                    if (!componentPane.getChildren().isEmpty() && componentPane.getChildren().get(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) componentPane.getChildren().get(0);
                        if (imageView.getImage() != null) {
                            width = width <= 0 ? imageView.getImage().getWidth() : width;
                            height = height <= 0 ? imageView.getImage().getHeight() : height;
                        }
                    }
                }
            } else if ("text".equals(component.getType())) {

                if (!componentPane.getChildren().isEmpty() && componentPane.getChildren().get(0) instanceof Canvas) {
                    Canvas canvas = (Canvas) componentPane.getChildren().get(0);
                    width = canvas.getWidth();
                    height = canvas.getHeight();
                }
            }

            selectedComponentHighlight.setLayoutX(x);
            selectedComponentHighlight.setLayoutY(y);
            selectedComponentHighlight.setPrefWidth(width);
            selectedComponentHighlight.setPrefHeight(height);
            selectedComponentHighlight.setVisible(true);
        } else {
            selectedComponentHighlight.setVisible(false);
        }
    }

    private InterfaceComponent findComponentByName(String name) {
        if (interfaceComponents == null || name == null) {
            return null;
        }
        for (InterfaceComponent component : interfaceComponents) {
            if (component != null && name.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }

    private Node findComponentPane(String componentName) {
        for (Node node : root.getChildren()) {
            if (node.getId() != null &&
                    (node.getId().equals("layer_" + componentName) ||
                            node.getId().equals("graphic_" + componentName) ||
                            node.getId().equals("rect_" + componentName) ||
                            node.getId().equals("model_" + componentName) ||
                            node.getId().equals("inv_" + componentName) ||
                            node.getId().equals("invtext_" + componentName) ||
                            node.getId().equals("text_" + componentName))) {
                return node;
            }
        }

        for (Node node : root.getChildren()) {
            if (node instanceof Pane && node.getId() != null && node.getId().startsWith("layer_")) {
                Pane layerPane = (Pane) node;
                for (Node childNode : layerPane.getChildren()) {
                    if (childNode.getId() != null &&
                            (childNode.getId().equals("graphic_" + componentName) ||
                                    childNode.getId().equals("rect_" + componentName) ||
                                    childNode.getId().equals("model_" + componentName) ||
                                    node.getId().equals("inv_" + componentName) ||
                                    node.getId().equals("invtext_" + componentName) ||
                                    childNode.getId().equals("text_" + componentName))) {
                        return childNode;
                    }
                }
            }
        }
        for (Node node : areaViewport.getChildren()) {
            if (node.getId() != null &&
                    (node.getId().equals("layer_" + componentName) ||
                            node.getId().equals("graphic_" + componentName) ||
                            node.getId().equals("rect_" + componentName) ||
                            node.getId().equals("model_" + componentName) ||
                            node.getId().equals("inv_" + componentName) ||
                            node.getId().equals("invtext_" + componentName) ||
                            node.getId().equals("text_" + componentName))) {
                return node;
            }
        }

        for (Node node : areaViewport.getChildren()) {
            if (node instanceof Pane && node.getId() != null && node.getId().startsWith("layer_")) {
                Pane layerPane = (Pane) node;
                for (Node childNode : layerPane.getChildren()) {
                    if (childNode.getId() != null &&
                            (childNode.getId().equals("graphic_" + componentName) ||
                                    childNode.getId().equals("rect_" + componentName) ||
                                    childNode.getId().equals("model_" + componentName) ||
                                    node.getId().equals("inv_" + componentName) ||
                                    node.getId().equals("invtext_" + componentName) ||
                                    childNode.getId().equals("text_" + componentName))) {
                        return childNode;
                    }
                }
            }
        }

        return null;
    }

    private void makeComponentDraggable(InterfaceComponent component, Pane componentPane) {

        componentPane.setOnMousePressed(null);
        componentPane.setOnMouseDragged(null);
        componentPane.setOnMouseReleased(null);

        componentPane.setMouseTransparent(false);
        componentPane.setPickOnBounds(true);

        final InterfaceComponent parentComponent = component.getLayer() != null ?
                findComponentByName(component.getLayer()) : null;
        final Pane parentPane = parentComponent != null ?
                (Pane)findComponentPane(parentComponent.getName()) : null;

        if (parentPane != null) {
            parentPane.setPickOnBounds(false);
        }

        componentPane.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            isDragging = true;

            componentPane.toFront();
            selectedComponentHighlight.toFront();

            event.consume();
        });

        componentPane.setOnMouseDragged(event -> {
            if (isDragging) {
                double deltaX = event.getSceneX() - dragStartX;
                double deltaY = event.getSceneY() - dragStartY;

                double newX = component.getX() + deltaX;
                double newY = component.getY() + deltaY;

                if (componentPane.getParent() == areaViewport) {
                    newX = Math.max(0, Math.min(newX, areaViewport.getWidth() - component.getWidth()));
                    newY = Math.max(0, Math.min(newY, areaViewport.getHeight() - component.getHeight()));
                }

                component.setX((int) newX);
                component.setY((int) newY);

                componentPane.setLayoutX(newX);
                componentPane.setLayoutY(newY);

                if (component == currentlyDraggableComponent) {
                    highlightSelectedComponent(component);
                }

                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();

                if ("layer".equals(component.getType())) {
                    updateChildrenPositions(component.getName());
                }

                event.consume();
            }
        });

        componentPane.setOnMouseReleased(event -> {
            if (isDragging) {
                isDragging = false;
                event.consume();
                populatePropertiesSidebar(component);
            }
        });

        if (parentPane != null) {
            parentPane.setPickOnBounds(false);

            parentPane.setOnMouseExited(event -> {
                if (isDragging) {
                    isDragging = false;
                    event.consume();
                }
            });
        }

        else if (componentPane.getParent() == areaViewport) {
            areaViewport.setOnMouseExited(event -> {
                if (isDragging) {
                    isDragging = false;
                    event.consume();
                }
            });
        }
    }

    private void updateChildrenPositions(String layerName) {
        if (layerName == null || interfaceComponents == null) return;

        for (InterfaceComponent component : interfaceComponents) {
            if (component != null && layerName.equals(component.getLayer())) {
                Node childNode = findComponentPane(component.getName());
                if (childNode instanceof Pane) {
                    Pane childPane = (Pane) childNode;

                    TreeItem<String> selectedItem = componentTreeView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null && selectedItem.getValue().startsWith(component.getName() + " ")) {
                        highlightSelectedComponent(component);
                    }
                }
            }
        }
    }

    private void initializeSelectionHighlight() {
        selectedComponentHighlight = new Pane();
        selectedComponentHighlight.setStyle("-fx-border-color: #ff6600; -fx-border-width: 2; -fx-border-style: dashed;");
        selectedComponentHighlight.setMouseTransparent(true);
        selectedComponentHighlight.setVisible(false);
        root.getChildren().add(selectedComponentHighlight);
        selectedComponentHighlight.setViewOrder(-10);
    }

    private void loadInterfaceComponents(String filePath) throws IOException {
        if (interfaceComponents != null) {
            interfaceComponents.clear();
        }
        clearRenderedComponents();

        interfaceComponents = InterfaceFileParser.parseInterfaceFile(filePath);
    }

    private void clearRenderedComponents() {
        List<Node> nodesToRemove = new ArrayList<>();

        for (Node node : root.getChildren()) {
            if (node != sidebarVBox &&
                    node != propertiesSidebarVBox &&
                    node != areaViewport &&
                    node != areaSidebar &&
                    node != tooltipPane &&
                    node != backgroundCanvas) {

                if (node instanceof Pane) {
                    String id = node.getId();
                    if (id != null && (
                            id.startsWith("layer_") ||
                                    id.startsWith("graphic_") ||
                                    id.startsWith("rect_") ||
                                    id.startsWith("model_") ||
                                    id.startsWith("text_"))) {
                        nodesToRemove.add(node);
                    }
                }
            }
        }
        root.getChildren().removeAll(nodesToRemove);
        nodesToRemove.clear();

        for (Node node : areaViewport.getChildren()) {
            if (node != sidebarVBox &&
                    node != propertiesSidebarVBox &&
                    node != areaViewport &&
                    node != areaSidebar &&
                    node != tooltipPane &&
                    node != backgroundCanvas) {

                if (node instanceof Pane) {
                    String id = node.getId();
                    if (id != null && (
                            id.startsWith("layer_") ||
                                    id.startsWith("graphic_") ||
                                    id.startsWith("rect_") ||
                                    id.startsWith("model_") ||
                                    id.startsWith("text_"))) {
                        nodesToRemove.add(node);
                    }
                }
            }
        }
        areaViewport.getChildren().removeAll(nodesToRemove);

        activeComponentName = null;

        originalClickHandlers.clear();
        originalViewOrderMap.clear();
        textRenderInfoMap.clear();
    }

    private void renderInterfaceComponents() {
        hideTooltip();
        Map<String, InterfaceComponent> componentMap = new HashMap<>();
        Map<String, Pane> componentPaneMap = new HashMap<>();
        Map<String, List<String>> layerChildrenMap = new HashMap<>();
        Map<String, Boolean> layerVisibilityMap = new HashMap<>();
        root.setOnMouseClicked(event -> {
            if (activeComponentName != null && event.getTarget() == root) {
                Pane activePane = componentPaneMap.get(activeComponentName);
                InterfaceComponent activeComponent = componentMap.get(activeComponentName);

                if (activePane != null && activeComponent != null &&
                        activeComponent.getActiveGraphicName() != null) {
                    ImageView activeImageView = (ImageView) activePane.getChildren().get(0);
                    activeImageView.setImage(assetLoader.getSpriteManager().getSprite(activeComponent.getGraphicName(),
                            activeComponent.getGraphicIndex()));
                }

                activeComponentName = null;
            }
        });

        for (InterfaceComponent component : interfaceComponents) {
            if (component != null) {
                componentMap.put(component.getName(), component);

                if ("layer".equals(component.getType())) {
                    layerVisibilityMap.put(component.getName(), !component.isHide());
                    layerChildrenMap.put(component.getName(), new ArrayList<>());
                }

                if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                    layerChildrenMap.computeIfAbsent(component.getLayer(), k -> new ArrayList<>())
                            .add(component.getName());
                }
            }
        }

        for (InterfaceComponent component : interfaceComponents) {
            if (component == null)
                continue;
            InterfaceComponent parentComponent = null;
            double x = component.getX();
            double y = component.getY();
            if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                parentComponent = componentMap.get(component.getLayer());
                if (parentComponent != null) {
                    x += parentComponent.getX();
                    y += parentComponent.getY();
                } else {
                    System.err.println("Warning: Layer reference '" + component.getLayer() +
                            "' not found for component '" + component.getName() + "'");
                }
            }

            Pane componentPane = null;

            if ("layer".equals(component.getType())) {
                componentPane = new Pane();
                componentPane.setLayoutX(x);
                componentPane.setLayoutY(y);
                componentPane.setPrefWidth(component.getWidth());
                componentPane.setPrefHeight(component.getHeight());
                componentPane.setId("layer_" + component.getName());

                Rectangle clipRect = new Rectangle(component.getWidth(), component.getHeight());
                componentPane.setClip(clipRect);

                Rectangle eventCapture = new Rectangle(component.getWidth(), component.getHeight());
                eventCapture.setFill(Color.TRANSPARENT);
                componentPane.getChildren().add(eventCapture);

                boolean isVisible = layerVisibilityMap.getOrDefault(component.getName(), true);
                componentPane.setVisible(isVisible);
                componentPane.setPickOnBounds(true);
            }
            else if ("graphic".equals(component.getType())) {
                try {
                    componentPane = new Pane();
                    componentPane.setLayoutX(x);
                    componentPane.setLayoutY(y);
                    componentPane.setPrefWidth(component.getWidth() > 0 ? component.getWidth() : 50);
                    componentPane.setPrefHeight(component.getHeight() > 0 ? component.getHeight() : 50);
                    componentPane.setId("graphic_" + component.getName());

                    ImageView imageView = new ImageView();

                    if (component.getGraphicName() != null && !component.getGraphicName().isEmpty()) {
                        WritableImage sprite = assetLoader.getSpriteManager().getSprite(component.getGraphicName(), component.getGraphicIndex());
                        imageView.setImage(sprite);
                    }
                    componentPane.getChildren().add(imageView);

                    if (component.getActiveGraphicName() != null && !component.getActiveGraphicName().isEmpty()) {
                        final String componentName = component.getName();

                        final String activeGraphicName = component.getActiveGraphicName();
                        final int activeGraphicIndex = component.getActiveGraphicIndex();

                        double width = component.getWidth() > 0 ? component.getWidth() : (imageView.getImage() != null ? imageView.getImage().getWidth() : 50);
                        double height = component.getHeight() > 0 ? component.getHeight() : (imageView.getImage() != null ? imageView.getImage().getHeight() : 50);

                        Rectangle clickCaptureRect = new Rectangle(width, height);
                        clickCaptureRect.setFill(Color.TRANSPARENT);
                        componentPane.getChildren().add(clickCaptureRect);

                        EventHandler<MouseEvent> clickHandler = event -> {

                            if (activeComponentName != null && !componentName.equals(activeComponentName)) {
                                Pane prevActivePane = componentPaneMap.get(activeComponentName);
                                InterfaceComponent prevActiveComponent = componentMap.get(activeComponentName);

                                if (prevActivePane != null && prevActiveComponent != null &&
                                        prevActiveComponent.getActiveGraphicName() != null) {
                                    Node firstChild = prevActivePane.getChildren().get(0);
                                    if (firstChild instanceof ImageView) {
                                        ImageView prevImageView = (ImageView) firstChild;
                                        if (prevActiveComponent.getGraphicName() != null && !prevActiveComponent.getGraphicName().isEmpty()) {
                                            prevImageView.setImage(assetLoader.getSpriteManager().getSprite(prevActiveComponent.getGraphicName(),
                                                    prevActiveComponent.getGraphicIndex()));
                                        } else {
                                            prevImageView.setImage(null);
                                        }
                                    }
                                }
                            }

                            activeComponentName = componentName;

                            WritableImage activeImage = assetLoader.getSpriteManager().getSprite(activeGraphicName, activeGraphicIndex);
                            if (activeImage != null) {
                                imageView.setImage(activeImage);
                            } else {
                                System.err.println("Failed to get active sprite for: " + activeGraphicName);
                            }

                            event.consume();
                        };
                        originalClickHandlers.put(componentName, clickHandler);

                        imageView.setOnMouseClicked(clickHandler);
                        clickCaptureRect.setOnMouseClicked(clickHandler);

                        componentPane.setPickOnBounds(true);
                    }
                } catch (Exception e) {
                    System.err.println("Error creating component " + component.getName() + ": " + e.getMessage());
                }
            }
            else if ("rect".equals(component.getType())) {
                try {
                    componentPane = new Pane();
                    componentPane.setLayoutX(x);
                    componentPane.setLayoutY(y);
                    componentPane.setId("rect_" + component.getName());

                    Rectangle rectangle = new Rectangle(component.getWidth(), component.getHeight());

                    String colorStr = component.getColour();
                    if (colorStr != null && colorStr.startsWith("0x")) {
                        int colorValue = Integer.parseInt(colorStr.substring(2), 16);
                        int red = (colorValue >> 16) & 0xFF;
                        int green = (colorValue >> 8) & 0xFF;
                        int blue = colorValue & 0xFF;
                        Color color = Color.rgb(red, green, blue);
                        if (component.isFill()) {
                            rectangle.setFill(color);
                        } else {
                            rectangle.setStroke(color);
                            rectangle.setStrokeWidth(1);
                        }
                    } else {
                        if (component.isFill()) {
                            rectangle.setFill(Color.BLACK);
                        } else {
                            rectangle.setFill(Color.TRANSPARENT);
                        }
                    }

                    componentPane.getChildren().add(rectangle);
                } catch (Exception e) {
                    System.err.println("Error creating rect component " + component.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else if ("model".equals(component.getType())) {
                try {
                    componentPane = new Pane();
                    componentPane.setLayoutX(component.getX() + areaViewport.getLayoutX());
                    componentPane.setLayoutY(component.getY() + areaViewport.getLayoutY());
                    componentPane.setId("model_" + component.getName());

                    Model model = assetLoader.getModel(component.getModel());
                    if (model != null) {
                        if (model.faceColorA == null) {
                            model.calculateNormals(64, 768, -50, -10, -50, true);
                        }
                        int compWidth = component.getWidth();
                        int compHeight = component.getHeight();

                        if (compWidth <= 0 || compHeight <= 0) {
                            System.err.println("Skipping model component with invalid dimensions: " + component.getName());
                            continue;
                        }


                        int modelRadius = model.radius;
                        int bufferWidth = Math.max(compWidth, compWidth + modelRadius * 2);
                        int bufferHeight = Math.max(compHeight, compHeight + modelRadius * 2);

                        WritableImage image = new WritableImage(bufferWidth, bufferHeight);
                        PixelWriter pixelWriter = image.getPixelWriter();

                        int[] pixelBuffer = new int[bufferWidth * bufferHeight];

                        Pix3D.width2d = bufferWidth;
                        Pix3D.boundBottom = bufferHeight;
                        Pix3D.safeWidth = bufferWidth > 0 ? bufferWidth - 1 : 0;
                        Pix3D.data = pixelBuffer;
                        Pix3D.lineOffset = new int[bufferHeight];
                        for (int i = 0; i < bufferHeight; i++) {
                            Pix3D.lineOffset[i] = i * bufferWidth;
                        }

                        int originalCenterW = Pix3D.centerW3D;
                        int originalCenterH = Pix3D.centerH3D;

                        Pix3D.centerW3D = bufferWidth / 2;
                        Pix3D.centerH3D = bufferHeight / 2;

                        Arrays.fill(pixelBuffer, 0);

                        int eyeY = Pix3D.sinTable[component.getXan()] * component.getZoom() >> 16;
                        int eyeZ = Pix3D.cosTable[component.getXan()] * component.getZoom() >> 16;

                        model.drawSimple(0, component.getYan(), 0, component.getXan(), 0, eyeY, eyeZ);

                        Pix3D.centerW3D = originalCenterW;
                        Pix3D.centerH3D = originalCenterH;

                        for (int y2 = 0; y2 < bufferHeight; y2++) {
                            for (int x2 = 0; x2 < bufferWidth; x2++) {
                                int pixel = pixelBuffer[y2 * bufferWidth + x2];
                                if (pixel == 0) {
                                    pixelWriter.setArgb(x2, y2, 0x00000000);
                                } else {
                                    if ((pixel & 0xFF000000) == 0) {
                                        pixel = pixel | 0xFF000000;
                                    }
                                    pixelWriter.setArgb(x2, y2, pixel);
                                }
                            }
                        }

                        ImageView imageView = new ImageView(image);

                        imageView.setLayoutX((compWidth - bufferWidth) / 2);
                        imageView.setLayoutY((compHeight - bufferHeight) / 2);

                        componentPane.getChildren().add(imageView);
                    } else {
                        System.out.println("No model found for: " + component.getModel());
                    }
                } catch (Exception e) {
                    System.err.println("Error creating model component " + component.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else if ("text".equals(component.getType())) {
                try {
                    CustomFontHelper font = assetLoader.getFontManager().getFont(component.getFont());
                    if (font == null) {
                        System.err.println("Error: Font not found: " + component.getFont());
                        return;
                    }

                    componentPane = new Pane();
                    componentPane.setLayoutX(x);
                    componentPane.setLayoutY(y);
                    componentPane.setViewOrder(-1.0);
                    componentPane.setId("text_" + component.getName());

                    double containerWidth = component.getWidth();
                    String originalText = component.getText();
                    if (originalText == null) originalText = "";

                    double maxTextWidth = 0;
                    double totalHeight = 0;
                    ArrayList<String> lines = new ArrayList<>();
                    int lineHeight = font.height;

                    TextRenderInfo renderInfo = new TextRenderInfo();
                    renderInfo.font = font;
                    renderInfo.text = originalText;
                    renderInfo.shadowed = component.isShadowed();
                    renderInfo.centered = component.isCenter();
                    renderInfo.containerWidth = containerWidth;

                    String remainingText = originalText;
                    while (remainingText.length() > 0 || lines.isEmpty()) {
                        int newlineIndex = remainingText.indexOf("\\n");
                        String line;

                        if (newlineIndex != -1) {
                            line = remainingText.substring(0, newlineIndex);
                            remainingText = remainingText.substring(newlineIndex + 2);
                        } else {
                            line = remainingText;
                            remainingText = "";
                        }

                        if (originalText.isEmpty() && lines.isEmpty() && line.isEmpty()) {
                            lines.add("");
                        } else if (!originalText.isEmpty() || !line.isEmpty()){
                            lines.add(line);
                        }

                        int currentLineWidth = font.getTextWidth(line);
                        maxTextWidth = Math.max(maxTextWidth, currentLineWidth);
                        totalHeight += lineHeight;

                        if (originalText.isEmpty() && !lines.isEmpty()) break;

                        if (newlineIndex == -1 && remainingText.isEmpty()) break;

                    }
                    if (lines.isEmpty()) {
                        lines.add("");
                        totalHeight = lineHeight;
                    }

                    double canvasWidth = component.isCenter() ? containerWidth : maxTextWidth;
                    double canvasHeight = totalHeight;

                    if (canvasWidth <= 0) canvasWidth = 1;
                    if (canvasHeight <= 0) canvasHeight = lineHeight;

                    Canvas textCanvas = new Canvas(canvasWidth + 5, canvasHeight + 5);
                    GraphicsContext gc = textCanvas.getGraphicsContext2D();

                    Color color = Color.BLACK;
                    try {
                        String colorStr = component.getColour();
                        if (colorStr != null && colorStr.length() >= 2) {
                            if (colorStr.startsWith("0x")) {
                                colorStr = colorStr.substring(2);
                            }
                            if (colorStr.matches("[0-9a-fA-F]{6}")) {
                                int rgb = Integer.parseInt(colorStr, 16);
                                color = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
                            } else {
                                System.err.println("Warning: Invalid color format for " + component.getName() + ": " + component.getColour());
                            }
                        }
                    } catch (NumberFormatException nfe) {
                        System.err.println("Error parsing color for " + component.getName() + ": " + component.getColour() + " - " + nfe.getMessage());
                    } catch (Exception e) {
                        System.err.println("Error processing color for " + component.getName() + ": " + e.getMessage());
                    }
                    double currentLineY = 0;
                    for (String line : lines) {
                        double baselineY = currentLineY + lineHeight;

                        int lineStartX = 0;
                        if (component.isCenter()) {
                            int currentLineWidth = font.getTextWidth(line);
                            lineStartX = ((int)containerWidth - currentLineWidth) / 2 + 1;
                            if(lineStartX < 0) lineStartX = 0;
                        }

                        renderInfo.lines.add(line);
                        renderInfo.lineStartXPositions.add(lineStartX);
                        renderInfo.lineYPositions.add(baselineY);

                        font.drawTextWithTags(
                                gc,
                                line,
                                lineStartX,
                                baselineY,
                                color,
                                component.isShadowed()
                        );
                        currentLineY += lineHeight;
                    }

                    textRenderInfoMap.put(component.getName(), renderInfo);
                    componentPane.getChildren().add(textCanvas);

                    componentPane.setPrefWidth(canvasWidth);
                    componentPane.setPrefHeight(canvasHeight);

                } catch (Exception e) {
                    System.err.println("Error creating text component " + component.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else if ("inv".equals(component.getType())) {
                componentPane = new Pane();
                componentPane.setLayoutX(x);
                componentPane.setLayoutY(y);
                componentPane.setPrefWidth(component.getWidth());
                componentPane.setPrefHeight(component.getHeight());
                componentPane.setId("inv_" + component.getName());
            } else if ("invtext".equals(component.getType())) {
                componentPane = new Pane();
                componentPane.setLayoutX(x);
                componentPane.setLayoutY(y);
                componentPane.setPrefWidth(component.getWidth());
                componentPane.setPrefHeight(component.getHeight());
                componentPane.setId("invtext_" + component.getName());
            }
            if (componentPane != null && component.getScroll() > 0) {
                Pane scrollbarPane = new Pane();
                scrollbarPane.setLayoutX(x + component.getWidth());
                scrollbarPane.setLayoutY(y);
                scrollbarPane.setPrefWidth(16);
                scrollbarPane.setPrefHeight(component.getHeight());
                scrollbarPane.setId("scrollbar_" + component.getName());

                ImageView topButton = new ImageView(assetLoader.getSpriteManager().getSprite("scrollbar", 0));
                topButton.setFitWidth(16);
                topButton.setFitHeight(16);

                ImageView bottomButton = new ImageView(assetLoader.getSpriteManager().getSprite("scrollbar", 1));
                bottomButton.setFitWidth(16);
                bottomButton.setFitHeight(16);
                bottomButton.setLayoutY(component.getHeight() - 16);

                int gripSize = (component.getHeight() - 32) * component.getHeight() / component.getScroll();
                if (gripSize < 8) gripSize = 8;

                int gripY = (component.getHeight() - gripSize - 32) /
                        (Math.max(1, component.getScroll() - component.getHeight()));

                Rectangle track = new Rectangle(16, component.getHeight() - 32);
                track.setLayoutY(16);
                track.setFill(Color.web("0x23201b"));

                Rectangle grip = new Rectangle(16, gripSize);
                grip.setLayoutY(0);
                grip.setFill(Color.web("0x4d4233"));

                Line highlight1 = new Line(0, 0, 0, gripSize);
                highlight1.setStroke(Color.web("0x766654"));
                highlight1.setLayoutX(0);
                highlight1.setLayoutY(0);

                Line highlight2 = new Line(0, 0, 0, gripSize);
                highlight2.setStroke(Color.web("0x766654"));
                highlight2.setLayoutX(1);
                highlight2.setLayoutY(0);

                Line highlight3 = new Line(0, 0, 16, 0);
                highlight3.setStroke(Color.web("0x766654"));
                highlight3.setLayoutY(0);

                Line highlight4 = new Line(0, 0, 16, 0);
                highlight4.setStroke(Color.web("0x766654"));
                highlight4.setLayoutY(1);

                Line lowlight1 = new Line(0, 0, 0, gripSize);
                lowlight1.setStroke(Color.web("0x332d25"));
                lowlight1.setLayoutX(15);
                lowlight1.setLayoutY(0);

                Line lowlight2 = new Line(0, 0, 0, gripSize - 1);
                lowlight2.setStroke(Color.web("0x332d25"));
                lowlight2.setLayoutX(14);
                lowlight2.setLayoutY(1);

                Line lowlight3 = new Line(0, 0, 16, 0);
                lowlight3.setStroke(Color.web("0x332d25"));
                lowlight3.setLayoutY(gripSize - 1);

                Line lowlight4 = new Line(0, 0, 15, 0);
                lowlight4.setStroke(Color.web("0x332d25"));
                lowlight4.setLayoutX(1);
                lowlight4.setLayoutY(gripSize - 2);

                Pane gripPane = new Pane();
                gripPane.getChildren().addAll(
                        grip,
                        highlight1, highlight2, highlight3, highlight4,
                        lowlight1, lowlight2, lowlight3, lowlight4
                );
                gripPane.setLayoutY(16 + gripY);

                scrollbarPane.getChildren().addAll(track, topButton, bottomButton, gripPane);

                if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                    Pane parentPane = componentPaneMap.get(component.getLayer());
                    if (parentPane != null) {
                        parentPane.getChildren().add(scrollbarPane);
                    } else {
                        root.getChildren().add(scrollbarPane);
                    }
                } else {
                    root.getChildren().add(scrollbarPane);
                }
            }

            if (componentPane != null) {
                final String overlayerName = component.getOverlayer();

                String tooltipText = null;
                if (component.getOption() != null && !component.getOption().isEmpty()) {
                    tooltipText = component.getOption();
                } else if (component.getButtonType() != null && !component.getButtonType().isEmpty()) {
                    tooltipText = component.getButtonType();
                }
                final String finalTooltipText = tooltipText != null ? capitalizeFirstLetter(tooltipText) : null;

                final Canvas textCanvas = component.getType().equals("text") ?
                        (Canvas) componentPane.getChildren().get(0) : null;

                componentPane.setOnMouseEntered(event -> {
                    event.consume();
                    if (overlayerName != null && !overlayerName.isEmpty()) {
                        toggleLayerVisibility(overlayerName, true, componentMap, componentPaneMap,
                                layerVisibilityMap, layerChildrenMap);
                    }

                    if (finalTooltipText != null) {
                        showTooltip(finalTooltipText);
                    }

                    if (component.getType().equals("text") && component.getOverColour() != null && textCanvas != null) {
                        TextRenderInfo renderInfo = textRenderInfoMap.get(component.getName());
                        if (renderInfo != null) {
                            GraphicsContext hoverGc = textCanvas.getGraphicsContext2D();
                            hoverGc.clearRect(0, 0, textCanvas.getWidth(), textCanvas.getHeight());

                            Color hoverColor = Color.BLACK;
                            try {
                                String colorStr = component.getOverColour();
                                if (colorStr != null && colorStr.length() >= 2) {
                                    if (colorStr.startsWith("0x")) {
                                        colorStr = colorStr.substring(2);
                                    }
                                    if (colorStr.matches("[0-9a-fA-F]{6}")) {
                                        int rgb = Integer.parseInt(colorStr, 16);
                                        hoverColor = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing hover color: " + e.getMessage());
                            }

                            for (int i = 0; i < renderInfo.lines.size(); i++) {
                                renderInfo.font.drawTextWithTags(
                                        hoverGc,
                                        renderInfo.lines.get(i),
                                        renderInfo.lineStartXPositions.get(i),
                                        renderInfo.lineYPositions.get(i),
                                        hoverColor,
                                        renderInfo.shadowed
                                );
                            }
                        }
                    }

                    event.consume();
                });

                componentPane.setOnMouseExited(event -> {
                    event.consume();
                    if (overlayerName != null && !overlayerName.isEmpty()) {
                        InterfaceComponent overlayerComponent = componentMap.get(overlayerName);
                        if (overlayerComponent != null && "layer".equals(overlayerComponent.getType())) {
                            toggleLayerVisibility(overlayerName, !overlayerComponent.isHide(),
                                    componentMap, componentPaneMap, layerVisibilityMap, layerChildrenMap);
                        }
                    }

                    if (finalTooltipText != null) {
                        hideTooltip();
                    }

                    if (component.getType().equals("text") && component.getOverColour() != null && textCanvas != null) {
                        TextRenderInfo renderInfo = textRenderInfoMap.get(component.getName());
                        if (renderInfo != null) {
                            GraphicsContext exitGc = textCanvas.getGraphicsContext2D();
                            exitGc.clearRect(0, 0, textCanvas.getWidth(), textCanvas.getHeight());

                            Color originalColor = Color.BLACK;
                            try {
                                String colorStr = component.getColour();
                                if (colorStr != null && colorStr.length() >= 2) {
                                    if (colorStr.startsWith("0x")) {
                                        colorStr = colorStr.substring(2);
                                    }
                                    if (colorStr.matches("[0-9a-fA-F]{6}")) {
                                        int rgb = Integer.parseInt(colorStr, 16);
                                        originalColor = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing original color: " + e.getMessage());
                            }

                            for (int i = 0; i < renderInfo.lines.size(); i++) {
                                renderInfo.font.drawTextWithTags(
                                        exitGc,
                                        renderInfo.lines.get(i),
                                        renderInfo.lineStartXPositions.get(i),
                                        renderInfo.lineYPositions.get(i),
                                        originalColor,
                                        renderInfo.shadowed
                                );
                            }
                        }
                    }

                    event.consume();
                });

                componentPane.setPickOnBounds(true);
                componentPane.setMouseTransparent(false);

                componentPaneMap.put(component.getName(), componentPane);

                if (component.getLayer() != null && !component.getLayer().isEmpty() &&
                        !("layer".equals(component.getType()))) {
                    Pane parentPane = componentPaneMap.get(component.getLayer());
                    if (parentPane != null) {
                        if (parentComponent != null) {
                            componentPane.setLayoutX(component.getX());
                            componentPane.setLayoutY(component.getY());
                        }
                        parentPane.getChildren().add(componentPane);
                    } else {
                        areaViewport.getChildren().add(componentPane);
                        System.out.println("Warning: Parent layer pane not found for " + component.getName() + ", adding to root");
                    }
                } else {
                    areaViewport.getChildren().add(componentPane);
                }
                if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                    boolean parentVisible = layerVisibilityMap.getOrDefault(component.getLayer(), true);
                    componentPane.setVisible(parentVisible);
                }
            }
        }
        for (String componentName : componentPaneMap.keySet()) {
            Pane pane = componentPaneMap.get(componentName);
            InterfaceComponent component = componentMap.get(componentName);
            if (pane != null && component != null) {
                updateMouseTransparency(pane, component);
            }
        }
    }

    private void updateMouseTransparency(Pane pane, InterfaceComponent component) {
        if (pane == null || component == null) return;

        boolean isLayer = "layer".equals(component.getType());
        boolean hasActiveState = (component.getActiveGraphicName() != null && !component.getActiveGraphicName().isEmpty());
        boolean hasHoverState = component.getOverColour() != null;
        boolean isInteractiveLayer = isLayer && component.getOverlayer() != null && !component.getOverlayer().isEmpty();
        boolean hasTooltip = (component.getOption() != null && !component.getOption().isEmpty()) ||
                (component.getButtonType() != null && !component.getButtonType().isEmpty());

        boolean hasInteractiveChildren = false;
        if (isLayer) {
            for (InterfaceComponent child : interfaceComponents) {
                if (child != null && component.getName().equals(child.getLayer())) {
                    if ((child.getOption() != null && !child.getOption().isEmpty()) ||
                            (child.getButtonType() != null && !child.getButtonType().isEmpty()) ||
                            (child.getActiveGraphicName() != null && !child.getActiveGraphicName().isEmpty()) ||
                            child.getOverColour() != null) {
                        hasInteractiveChildren = true;
                        break;
                    }
                }
            }
        }

        boolean makeTransparent = !hasActiveState && !hasHoverState && !hasTooltip &&
                !isInteractiveLayer && !hasInteractiveChildren;

        pane.setMouseTransparent(makeTransparent);
        pane.setPickOnBounds(!makeTransparent);
    }

    private void removeDraggable(Pane pane, InterfaceComponent component) {
        if (pane == null) return;
        pane.setOnMousePressed(null);
        pane.setOnMouseDragged(null);
        pane.setOnMouseReleased(null);
        updateMouseTransparency(pane, component);
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void toggleLayerVisibility(
            String layerName,
            boolean visible,
            Map<String, InterfaceComponent> componentMap,
            Map<String, Pane> componentPaneMap,
            Map<String, Boolean> layerVisibilityMap,
            Map<String, List<String>> layerChildrenMap) {

        layerVisibilityMap.put(layerName, visible);

        Pane layerPane = componentPaneMap.get(layerName);
        if (layerPane != null) {
            layerPane.setVisible(visible);

            layerPane.setMouseTransparent(false);

            if (visible) {
                if (!originalViewOrderMap.containsKey(layerName)) {
                    originalViewOrderMap.put(layerName, layerPane.getViewOrder());
                }

                layerPane.setViewOrder(-10.0);
                layerPane.toFront();
            } else {
                Double originalViewOrder = originalViewOrderMap.getOrDefault(layerName, 0.0);
                layerPane.setViewOrder(originalViewOrder);
            }
        }

        List<String> children = layerChildrenMap.get(layerName);
        if (children != null) {
            for (String childName : children) {
                InterfaceComponent childComponent = componentMap.get(childName);
                Pane childPane = componentPaneMap.get(childName);

                if (childComponent != null && childPane != null) {
                    childPane.setVisible(visible);
                    if ("layer".equals(childComponent.getType())) {
                        toggleLayerVisibility(childName, visible, componentMap, componentPaneMap,
                                layerVisibilityMap, layerChildrenMap);
                    } else {
                        childPane.setVisible(visible);
                        if (visible) {
                            if (!originalViewOrderMap.containsKey(childName)) {
                                originalViewOrderMap.put(childName, childPane.getViewOrder());
                            }
                            childPane.setViewOrder(-9.0);
                            if (childPane.getParent() instanceof Pane) {
                                Pane parent = (Pane) childPane.getParent();
                                parent.getChildren().remove(childPane);
                                parent.getChildren().add(childPane);
                            }
                        } else {
                            Double originalViewOrder = originalViewOrderMap.getOrDefault(childName, 0.0);
                            childPane.setViewOrder(originalViewOrder);
                        }
                    }
                }
            }
        }
    }

    private void showTooltip(String text) {
        if (text == null || text.isEmpty()) return;
        tooltipCanvas.getGraphicsContext2D().clearRect(0, 0, tooltipCanvas.getWidth(), tooltipCanvas.getHeight());
        double width = Math.max(100,  assetLoader.getFontManager().getFont("b12").getTextWidth(text) + 10);
        if (width > tooltipCanvas.getWidth()) {
            tooltipCanvas.setWidth(width);
        }
        assetLoader.getFontManager().drawTaggableText(
                tooltipCanvas.getGraphicsContext2D(),
                "b12",
                text,
                12.0, 26.0,
                16777215,
                true
        );
        tooltipPane.setVisible(true);
    }

    private void hideTooltip() {
        tooltipPane.setVisible(false);
        tooltipCanvas.getGraphicsContext2D().clearRect(0, 0, tooltipCanvas.getWidth(), tooltipCanvas.getHeight());
    }

    private void initializeUIComponents() {
        try {
            root = new AnchorPane();
            backgroundCanvas = new Canvas(789, 532);
            gc = backgroundCanvas.getGraphicsContext2D();
            gc.setFill(Color.GRAY);
            gc.fillRect(0, 0, 789, 532);
            root.getChildren().add(backgroundCanvas);
            backgroundCanvas.toBack();
            root.setBackground(Background.EMPTY);
            scene = new Scene(root, 1289, 532);

            tooltipPane = new Pane();
            tooltipPane.setLayoutX(0);
            tooltipPane.setLayoutY(0);
            tooltipCanvas = new Canvas(300, 40);
            tooltipPane.getChildren().add(tooltipCanvas);
            tooltipPane.setVisible(false);
            tooltipPane.setMouseTransparent(true);
            tooltipPane.setViewOrder(-100);
            root.getChildren().add(tooltipPane);

            areaMapback = new Pane();
            areaMapback.setPrefSize(168, 160);
            areaMapback.setLayoutX(561);
            areaMapback.setLayoutY(5);
            mapbackImageView = new ImageView(assetLoader.getSpriteManager().getSprite("mapback", 0));
            areaMapback.getChildren().add(mapbackImageView);

            areaChatback = new Pane();
            areaChatback.setPrefSize(479, 96);
            areaChatback.setLayoutX(22);
            areaChatback.setLayoutY(375);
            chatbackImageView = new ImageView(assetLoader.getSpriteManager().getSprite("chatback", 0));
            areaChatback.getChildren().add(chatbackImageView);

            areaViewport = new Pane();
            areaViewport.setPrefSize(512, 334);
            areaViewport.setLayoutX(8);
            areaViewport.setLayoutY(11);

            areaSidebar = new Pane();
            areaSidebar.setPrefSize(190, 261);
            areaSidebar.setLayoutX(562);
            areaSidebar.setLayoutY(231);

            areaBackbase1 = new Pane();
            areaBackbase1.setPrefSize(501, 61);
            areaBackbase1.setLayoutX(0);
            areaBackbase1.setLayoutY(471);
            backbase1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backbase1", 0));
            areaBackbase1.getChildren().add(backbase1ImageView);

            areaBackbase2 = new Pane();
            areaBackbase2.setPrefSize(288, 40);
            areaBackbase2.setLayoutX(501);
            areaBackbase2.setLayoutY(492);
            backbase2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backbase2", 0));
            ImageView imageSideiconView7 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 7));
            imageSideiconView7.setLayoutX(80);
            imageSideiconView7.setLayoutY(2);

            ImageView imageSideiconView8 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 8));
            imageSideiconView8.setLayoutX(107);
            imageSideiconView8.setLayoutY(3);

            ImageView imageSideiconView9 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 9));
            imageSideiconView9.setLayoutX(142);
            imageSideiconView9.setLayoutY(4);

            ImageView imageSideiconView10 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 10));
            imageSideiconView10.setLayoutX(179);
            imageSideiconView10.setLayoutY(2);

            ImageView imageSideiconView11 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 11));
            imageSideiconView11.setLayoutX(206);
            imageSideiconView11.setLayoutY(2);

            ImageView imageSideiconView12 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 12));
            imageSideiconView12.setLayoutX(230);
            imageSideiconView12.setLayoutY(2);
            areaBackbase2.getChildren().addAll(backbase2ImageView, imageSideiconView7,
                    imageSideiconView8, imageSideiconView9, imageSideiconView10, imageSideiconView11,
                    imageSideiconView12);

            areaBackhmid1 = new Pane();
            areaBackhmid1.setPrefSize(269, 66);
            areaBackhmid1.setLayoutX(520);
            areaBackhmid1.setLayoutY(165);
            backhmid1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backhmid1", 0));
            ImageView imageSideiconView0 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 0));
            imageSideiconView0.setLayoutX(35);
            imageSideiconView0.setLayoutY(34);

            ImageView imageSideiconView1 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 1));
            imageSideiconView1.setLayoutX(59);
            imageSideiconView1.setLayoutY(32);

            ImageView imageSideiconView2 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 2));
            imageSideiconView2.setLayoutX(86);
            imageSideiconView2.setLayoutY(32);

            ImageView imageSideiconView3 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 3));
            imageSideiconView3.setLayoutX(121);
            imageSideiconView3.setLayoutY(33);

            ImageView imageSideiconView4 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 4));
            imageSideiconView4.setLayoutX(157);
            imageSideiconView4.setLayoutY(34);

            ImageView imageSideiconView5 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 5));
            imageSideiconView5.setLayoutX(185);
            imageSideiconView5.setLayoutY(32);

            ImageView imageSideiconView6 = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", 6));
            imageSideiconView6.setLayoutX(212);
            imageSideiconView6.setLayoutY(34);
            areaBackhmid1.getChildren().addAll(backhmid1ImageView, imageSideiconView0,
                    imageSideiconView1, imageSideiconView2, imageSideiconView3, imageSideiconView4,
                    imageSideiconView5, imageSideiconView6);

            areaBackleft1 = new Pane();
            areaBackleft1.setLayoutX(0);
            areaBackleft1.setLayoutY(11);
            backleft1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backleft1", 0));
            areaBackleft1.getChildren().add(backleft1ImageView);

            areaBackleft2 = new Pane();
            areaBackleft2.setLayoutX(0);
            areaBackleft2.setLayoutY(375);
            backleft2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backleft2", 0));
            areaBackleft2.getChildren().add(backleft2ImageView);

            areaBackright1 = new Pane();
            areaBackright1.setLayoutX(729);
            areaBackright1.setLayoutY(5);
            backright1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backright1", 0));
            areaBackright1.getChildren().add(backright1ImageView);

            areaBackright2 = new Pane();
            areaBackright2.setLayoutX(752);
            areaBackright2.setLayoutY(231);
            backright2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backright2", 0));
            areaBackright2.getChildren().add(backright2ImageView);

            areaBacktop1 = new Pane();
            areaBacktop1.setLayoutX(0);
            areaBacktop1.setLayoutY(0);
            backtop1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backtop1", 0));
            areaBacktop1.getChildren().add(backtop1ImageView);

            areaBacktop2 = new Pane();
            areaBacktop2.setLayoutX(561);
            areaBacktop2.setLayoutY(0);
            backtop2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backtop2", 0));
            areaBacktop2.getChildren().add(backtop2ImageView);

            areaBackvmid1 = new Pane();
            areaBackvmid1.setLayoutX(520);
            areaBackvmid1.setLayoutY(11);
            backvmid1ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backvmid1", 0));
            areaBackvmid1.getChildren().add(backvmid1ImageView);

            areaBackvmid2 = new Pane();
            areaBackvmid2.setLayoutX(520);
            areaBackvmid2.setLayoutY(231);
            backvmid2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backvmid2", 0));
            areaBackvmid2.getChildren().add(backvmid2ImageView);

            areaBackvmid3 = new Pane();
            areaBackvmid3.setLayoutX(501);
            areaBackvmid3.setLayoutY(375);
            backvmid3ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backvmid3", 0));
            areaBackvmid3.getChildren().add(backvmid3ImageView);

            areaBackhmid2 = new Pane();
            areaBackhmid2.setLayoutX(0);
            areaBackhmid2.setLayoutY(345);
            backhmid2ImageView = new ImageView(assetLoader.getSpriteManager().getSprite("backhmid2", 0));
            areaBackhmid2.getChildren().add(backhmid2ImageView);

            areaInvback = new Pane();
            areaInvback.setLayoutX(0);
            areaInvback.setLayoutY(0);
            invbackImageView = new ImageView(assetLoader.getSpriteManager().getSprite("invback", 0));
            areaSidebar.getChildren().add(invbackImageView);

            areaCompass = new Pane();
            compassImageView = new ImageView(assetLoader.getSpriteManager().getSprite("compass", 0));
            compassImageView.setLayoutX(-8);
            compassImageView.setLayoutY(-8);
            areaCompass.setLayoutX(areaMapback.getLayoutX());
            areaCompass.setLayoutY(areaMapback.getLayoutY());
            areaCompass.getChildren().add(compassImageView);

            areaMinimap = new Pane();
            areaMinimap.setLayoutX(561);
            areaMinimap.setLayoutY(6);
            Circle minimapCircle = new Circle(100);
            minimapCircle.setFill(Color.BLACK);
            minimapCircle.setCenterX(100);
            minimapCircle.setCenterY(100);

            areaMinimap.getChildren().add(minimapCircle);

            root.getChildren().addAll(
                    areaViewport, areaChatback, areaCompass, areaMinimap, areaMapback,
                    areaBackbase1,  areaBackbase2, areaBackhmid1,
                    areaBackleft1, areaBackleft2, areaBackright1, areaBackright2,
                    areaBacktop1, areaBacktop2, areaBackvmid1, areaBackvmid2, areaBackvmid3,
                    areaBackhmid2, areaSidebar, areaInvback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TextRenderInfo {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<Integer> lineStartXPositions = new ArrayList<>();
        ArrayList<Double> lineYPositions = new ArrayList<>();
        CustomFontHelper font;
        String text;
        boolean shadowed;
        boolean centered;
        double containerWidth;
    }
}

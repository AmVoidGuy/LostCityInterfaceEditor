package org.lostcityinterfaceeditor.fileUtils;

import org.lostcityinterfaceeditor.InterfaceComponent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InterfaceFileWriter {

    public static void writeInterfaceFile(List<InterfaceComponent> components, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < components.size(); i++) {
                InterfaceComponent component = components.get(i);

                writer.write("[" + component.getName() + "]");
                writer.newLine();
                if (component.getLayer() != null) {
                    writer.write("layer=" + component.getLayer());
                    writer.newLine();
                }
                writer.write("type=" + component.getType());
                writer.newLine();
                writer.write("x=" + component.getX());
                writer.newLine();
                writer.write("y=" + component.getY());
                writer.newLine();
                String buttonType = component.getButtonType();
                if (buttonType != null) {
                    if (buttonType.equals("ok")) {
                        writer.write("buttontype=normal");
                        writer.newLine();
                    } else if (buttonType.equals("select")) {
                        writer.write("buttontype=toggle");
                        writer.newLine();
                    } else if (buttonType.equals("close")) {
                        writer.write("buttontype=close");
                        writer.newLine();
                    } else if (buttonType.contains("@gre@")) {
                        writer.write("buttontype=target");
                        writer.newLine();
                    } else {
                        writer.write("buttontype=normal");
                        writer.newLine();
                    }
                }
                if(component.getClientCode() > 0) {
                    writer.write("clientcode=" + component.getClientCode());
                    writer.newLine();
                }
                writer.write("width=" + component.getWidth());
                writer.newLine();
                writer.write("height=" + component.getHeight());
                writer.newLine();
                if (component.getScripts() != null && !component.getScripts().isEmpty()) {
                    for (Map.Entry<Integer, InterfaceFileParser.Script> scriptEntry : component.getScripts().entrySet()) {
                        InterfaceFileParser.Script script = scriptEntry.getValue();
                        for (Map.Entry<Integer, String> opEntry : script.getOperations().entrySet()) {
                            int opNumber = opEntry.getKey();
                            String opValue = opEntry.getValue();

                            writer.write("script" + script.getScriptNumber() + "op" + opNumber + "=" + opValue);
                            writer.newLine();
                        }
                        writer.write("script" + script.getScriptNumber() + "=" + script.getCondition());
                        if (script.getConditionValue() != null && !script.getConditionValue().isEmpty()) {
                            writer.write("," + script.getConditionValue());
                        }
                        writer.newLine();

                    }
                }
                switch (component.getType()) {
                    case "text":
                        if(component.isCenter()) {
                            writer.write("center=" + formatBoolean(component.isCenter()));
                            writer.newLine();
                        }
                        if (component.getFont() != null) {
                            writer.write("font=" + component.getFont());
                            writer.newLine();
                        }
                        if(component.isShadowed()) {
                            writer.write("shadowed=" + formatBoolean(component.isShadowed()));
                            writer.newLine();
                        }
                        if (component.getText() != null) {
                            writer.write("text=" + component.getText());
                            writer.newLine();
                        }
                        if (component.getActiveText() != null) {
                            writer.write("activetext=" + component.getActiveText());
                            writer.newLine();
                        }
                        if (component.getActiveColour() != null) {
                            writer.write("activecolour=" + component.getActiveColour());
                            writer.newLine();
                        }
                        if (component.getColour() != null && !component.getColour().equals("0x000000")) {
                            writer.write("colour=" + component.getColour());
                            writer.newLine();
                        }
                        if (component.getOverColour() != null) {
                            writer.write("overcolour=" + component.getOverColour());
                            writer.newLine();
                        }
                        break;

                    case "graphic":
                        String graphicName = component.getGraphicName();
                        int graphicIndex = component.getGraphicIndex();
                        if (graphicName != null && !graphicName.isEmpty()) {
                            writer.write("graphic=" + graphicName + "," + graphicIndex);
                            writer.newLine();
                        }

                        String activeGraphicName = component.getActiveGraphicName();
                        int activeGraphicIndex = component.getActiveGraphicIndex();
                        if (activeGraphicName != null && !activeGraphicName.isEmpty()) {
                            writer.write("activegraphic=" + activeGraphicName + "," + activeGraphicIndex);
                            writer.newLine();
                        }
                        break;

                    case "layer":
                        if(component.isHide()) {
                            writer.write("hide=" + formatBoolean(component.isHide()));
                            writer.newLine();
                        }
                        if (component.getOverlayer() != null) {
                            writer.write("overlayer=" + component.getOverlayer());
                            writer.newLine();
                        }
                        if(component.getScroll() > 0) {
                            writer.write("scroll=" + component.getScroll());
                            writer.newLine();
                        }
                        break;

                    case "rect":
                        if (component.getColour() != null) {
                            writer.write("colour=" + component.getColour());
                            writer.newLine();
                        }
                        writer.write("fill=" + formatBoolean(component.isFill()));
                        writer.newLine();
                        break;

                    case "model":
                        if (component.getModel() != null) {
                            writer.write("model=" + component.getModel());
                            writer.newLine();
                        }
                        if (component.getAnim() != null) {
                            writer.write("anim=" + component.getAnim());
                            writer.newLine();
                        }
                        if (component.getActiveModel() != null) {
                            writer.write("activemodel=" + component.getActiveModel());
                            writer.newLine();
                        }
                        if (component.getActiveAnim() != null) {
                            writer.write("activeanim=" + component.getActiveAnim());
                            writer.newLine();
                        }
                        writer.write("zoom=" + component.getZoom());
                        writer.newLine();
                        if(component.getXan() > 0) {
                            writer.write("xan=" + component.getXan());
                            writer.newLine();
                        }
                        if(component.getYan() > 0) {
                            writer.write("yan=" + component.getYan());
                            writer.newLine();
                        }
                        break;

                    case "inv":
                        if(component.isDraggable()) {
                            writer.write("draggable=" + formatBoolean(component.isDraggable()));
                            writer.newLine();
                        }
                        if(component.isInteractable()) {
                            writer.write("interactable=" + formatBoolean(component.isInteractable()));
                            writer.newLine();
                        }
                        if(component.isUsable()) {
                            writer.write("usable=" + formatBoolean(component.isUsable()));
                            writer.newLine();
                        }
                        String margin = component.getMargin();
                        if (margin != null && !margin.isEmpty()) {
                            writer.write("margin=" + margin);
                            writer.newLine();
                        }
                        List<String> invOptions = component.getInvOptions();
                        for (int j = 0; j < invOptions.size(); j++) {
                            String option = invOptions.get(j);
                            if (option != null && !option.isEmpty()) {
                                writer.write("option" + (j + 1) + "=" + option);
                                writer.newLine();
                            }
                        }
                        break;

                    case "invtext":
                        if(component.isCenter()) {
                            writer.write("center=" + formatBoolean(component.isCenter()));
                            writer.newLine();
                        }
                        if (component.getFont() != null) {
                            writer.write("font=" + component.getFont());
                            writer.newLine();
                        }
                        if(component.isShadowed()) {
                            writer.write("shadowed=" + formatBoolean(component.isShadowed()));
                            writer.newLine();
                        }
                        if (component.getColour() != null && !component.getColour().equals("0x000000")) {
                            writer.write("colour=" + component.getColour());
                            writer.newLine();
                        }
                        if(component.isInteractable()) {
                            writer.write("interactable=" + formatBoolean(component.isInteractable()));
                            writer.newLine();
                        }
                        margin = component.getMargin();
                        if (margin != null && !margin.isEmpty()) {
                            writer.write("margin=" + margin);
                            writer.newLine();
                        }
                        List<String> invTextOptions = component.getInvOptions();
                        for (int j = 0; j < invTextOptions.size(); j++) {
                            String option = invTextOptions.get(j);
                            if (option != null && !option.isEmpty()) {
                                writer.write("option" + (j + 1) + "=" + option);
                                writer.newLine();
                            }
                        }
                        break;


                }
                if (component.getOption() != null) {
                    writer.write("option=" + component.getOption());
                    writer.newLine();
                }
                if(component.getActionVerb() != null) {
                    writer.write("actionverb=" + component.getActionVerb());
                    writer.newLine();
                }
                if(component.getActionTarget() != null) {
                    writer.write("actiontarget=" + component.getActionTarget());
                    writer.newLine();
                }
                if(component.getAction() != null) {
                    writer.write("action=" + component.getAction());
                    writer.newLine();
                }
                if (i < components.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    private static String formatBoolean(boolean value) {
        return value ? "yes" : "no";
    }
}
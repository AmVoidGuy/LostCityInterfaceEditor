package org.lostcityinterfaceeditor.fileUtils;

import org.lostcityinterfaceeditor.InterfaceComponent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterfaceFileParser {

    public static List<InterfaceComponent> parseInterfaceFile(String filePath) throws IOException {
        List<InterfaceComponent> components = new ArrayList<>();
        Map<String, String> currentComponent = new HashMap<>();
        String currentComponentName = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    if (currentComponentName != null && !currentComponent.isEmpty()) {
                        components.add(createComponentFromMap(currentComponentName, currentComponent));
                    }

                    currentComponentName = line.substring(1, line.length() - 1);
                    currentComponent = new HashMap<>();
                    continue;
                }

                if (currentComponentName != null && line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        currentComponent.put(key, value);
                    }
                }
            }

            if (currentComponentName != null && !currentComponent.isEmpty()) {
                components.add(createComponentFromMap(currentComponentName, currentComponent));
            }
        }

        return components;
    }

    private static InterfaceComponent createComponentFromMap(String name, Map<String, String> properties) {
        String type = properties.getOrDefault("type", "");
        int x = Integer.parseInt(properties.getOrDefault("x", "0"));
        int y = Integer.parseInt(properties.getOrDefault("y", "0"));
        int width = Integer.parseInt(properties.getOrDefault("width", "0"));
        int height = Integer.parseInt(properties.getOrDefault("height", "0"));
        int clientCode = Integer.parseInt(properties.getOrDefault("clientcode", "0"));
        String layer = properties.getOrDefault("layer", null);
        String option = properties.getOrDefault("option", null);
        String buttonType = properties.getOrDefault("buttontype", null);
        Map<Integer, Script> scripts = parseScripts(properties);
        if(buttonType != null) {
            if (buttonType.equals("normal"))
                buttonType = "ok";
            if (buttonType.equals("toggle"))
                buttonType = "select";
            if (buttonType.equals("target")) {
                String prefix = properties.getOrDefault("actionverb", null);
                if (prefix != null) {
                    int spaceIndex = prefix.indexOf(" ");
                    if (spaceIndex != -1) {
                        prefix = prefix.substring(0, spaceIndex);
                    }
                }
                buttonType = prefix + " @gre@" + properties.getOrDefault("action", null);
            }
        }
        String overLayer = properties.getOrDefault("overlayer", null);
        InterfaceComponent component = new InterfaceComponent(name, type, x, y, width, height, layer, option, buttonType, clientCode, scripts, overLayer);
        if (buttonType != null && buttonType.equals("target")) {
            component.setActionVerb(properties.getOrDefault("actionverb", null));
            component.setActionTarget(properties.getOrDefault("actiontarget", null));
            component.setAction(properties.getOrDefault("action", null));
        }
        if ("text".equals(type)) {
            component.setCenter(parseBoolean(properties.getOrDefault("center", "no")));
            component.setFont(properties.getOrDefault("font", ""));
            component.setShadowed(parseBoolean(properties.getOrDefault("shadowed", "no")));
            component.setText(properties.getOrDefault("text", ""));
            component.setColour(properties.getOrDefault("colour", "0x000000"));
            component.setOverColour(properties.getOrDefault("overcolour", null));
            component.setActiveColour(properties.getOrDefault("activecolour", null));
            component.setActiveText(properties.getOrDefault("activetext", null));
        } else if ("graphic".equals(type)) {
            String graphicStr = properties.getOrDefault("graphic", "");
            String activegraphicStr = properties.getOrDefault("activegraphic", "");
            String graphicName = "";
            int graphicIndex = 0;

            if (graphicStr.contains(",")) {
                String[] parts = graphicStr.split(",");
                graphicName = parts[0];
                graphicIndex = Integer.parseInt(parts[1]);
            } else if (!graphicStr.isEmpty()) {
                graphicName = graphicStr;
            }

            String activegraphicName = "";
            int activegraphicIndex = 0;

            if (activegraphicStr.contains(",")) {
                String[] parts = activegraphicStr.split(",");
                activegraphicName = parts[0];
                activegraphicIndex = Integer.parseInt(parts[1]);
            } else if (!activegraphicStr.isEmpty()) {
                activegraphicName = activegraphicStr;
            }

            component.setGraphicIndex(graphicIndex);
            component.setGraphicName(graphicName);
            component.setActiveGraphicIndex(activegraphicIndex);
            component.setActiveGraphicName(activegraphicName);
        } else if ("layer".equals(type)) {
            component.setHide(parseBoolean(properties.getOrDefault("hide", "no")));
            component.setScroll(Integer.parseInt(properties.getOrDefault("scroll", "0")));
        } else if ("rect".equals(type)) {
            component.setColour(properties.getOrDefault("colour", null));
            component.setFill(parseBoolean(properties.getOrDefault("fill", "no")));
        } else if ("model".equals(type)) {
            component.setXan(Integer.parseInt(properties.getOrDefault("xan", "0")));
            component.setYan(Integer.parseInt(properties.getOrDefault("yan", "0")));
            component.setZoom(Integer.parseInt(properties.getOrDefault("zoom", "0")));
            component.setModel(properties.getOrDefault("model", null));
            component.setActiveModel(properties.getOrDefault("activemodel", null));
            component.setActiveAnim(properties.getOrDefault("activeanim", null));
            component.setAnim(properties.getOrDefault("anim", null));
        } else if ("inv".equals(type)) {
            component.setMargin(properties.getOrDefault("margin", null));
            component.setDraggable(parseBoolean(properties.getOrDefault("draggable", "no")));
            component.setInteractable(parseBoolean(properties.getOrDefault("interactable", "no")));
            component.setUsable(parseBoolean(properties.getOrDefault("usable", "no")));
            for (int i = 1; i <= 5; i++) {
                String optionKey = "option" + i;
                if (properties.containsKey(optionKey)) {
                    component.addInvOption(properties.get(optionKey));
                }
            }
        } else if ("invtext".equals(type)) {
            component.setCenter(parseBoolean(properties.getOrDefault("center", "no")));
            component.setFont(properties.getOrDefault("font", ""));
            component.setShadowed(parseBoolean(properties.getOrDefault("shadowed", "no")));
            component.setColour(properties.getOrDefault("colour", "0x000000"));
            component.setMargin(properties.getOrDefault("margin", null));
            component.setInteractable(parseBoolean(properties.getOrDefault("interactable", "no")));
            for (int i = 1; i <= 5; i++) {
                String optionKey = "option" + i;
                if (properties.containsKey(optionKey)) {
                    component.addInvOption(properties.get(optionKey));
                }
            }
        }
        return component;
    }

    private static Map<Integer, Script> parseScripts(Map<String, String> properties) {
        Map<Integer, Script> scripts = new TreeMap<>();
        Pattern scriptPattern = Pattern.compile("script(\\d+)(?:op(\\d+))?");

        Map<Integer, List<ScriptOperation>> scriptOperations = new HashMap<>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Matcher matcher = scriptPattern.matcher(key);
            if (matcher.matches()) {
                int scriptNumber = Integer.parseInt(matcher.group(1));

                if (matcher.group(2) != null) {
                    int opNumber = Integer.parseInt(matcher.group(2));

                    scriptOperations.computeIfAbsent(scriptNumber, k -> new ArrayList<>())
                            .add(new ScriptOperation(opNumber, value));
                } else {
                    String[] parts = value.split(",", 2);
                    String condition = parts[0];
                    String conditionValue = parts.length > 1 ? parts[1] : "";

                    if (!scripts.containsKey(scriptNumber)) {
                        scripts.put(scriptNumber, new Script(scriptNumber, condition, conditionValue));
                    } else {
                        Script script = scripts.get(scriptNumber);
                        script.setCondition(condition);
                        script.setConditionValue(conditionValue);
                    }
                }
            }
        }

        for (Map.Entry<Integer, List<ScriptOperation>> entry : scriptOperations.entrySet()) {
            int scriptNumber = entry.getKey();
            List<ScriptOperation> operations = entry.getValue();

            if (!scripts.containsKey(scriptNumber)) {
                scripts.put(scriptNumber, new Script(scriptNumber, "", ""));
            }

            Script script = scripts.get(scriptNumber);
            for (ScriptOperation op : operations) {
                script.addOperation(op.getOperationNumber(), op.getValue());
            }
        }

        return scripts;
    }

    private static boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("yes");
    }

    public static class Script {
        private final int scriptNumber;
        private String condition;
        private String conditionValue;
        private final Map<Integer, String> operations;

        public Script(int scriptNumber, String condition, String conditionValue) {
            this.scriptNumber = scriptNumber;
            this.condition = condition;
            this.conditionValue = conditionValue;
            this.operations = new TreeMap<>();
        }

        public int getScriptNumber() {
            return scriptNumber;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getConditionValue() {
            return conditionValue;
        }

        public void setConditionValue(String conditionValue) {
            this.conditionValue = conditionValue;
        }

        public Map<Integer, String> getOperations() {
            return operations;
        }

        public void addOperation(int operationNumber, String value) {
            operations.put(operationNumber, value);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("script").append(scriptNumber).append("=").append(condition);
            if (!conditionValue.isEmpty()) {
                sb.append(",").append(conditionValue);
            }
            sb.append("\n");

            for (Map.Entry<Integer, String> op : operations.entrySet()) {
                sb.append("script").append(scriptNumber).append("op")
                        .append(op.getKey()).append("=").append(op.getValue()).append("\n");
            }

            return sb.toString();
        }
    }

    private static class ScriptOperation {
        private final int operationNumber;
        private final String value;

        public ScriptOperation(int operationNumber, String value) {
            this.operationNumber = operationNumber;
            this.value = value;
        }

        public int getOperationNumber() {
            return operationNumber;
        }

        public String getValue() {
            return value;
        }
    }
}
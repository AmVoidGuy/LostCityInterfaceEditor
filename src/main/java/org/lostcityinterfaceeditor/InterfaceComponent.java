package org.lostcityinterfaceeditor;

import org.lostcityinterfaceeditor.fileUtils.InterfaceFileParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InterfaceComponent {
    private String name;
    private String type;
    private int x;
    private int y;
    private int width;
    private int height;
    private String layer;
    private String option;
    private int clientCode;
    private String buttonType;
    private Map<Integer, InterfaceFileParser.Script> scripts;
    private String actionVerb;
    private String action;
    private String actionTarget;
    private String overlayer;

    // Graphics-specific
    private String graphicName;
    private int graphicIndex;
    private String activeGraphicName;
    private int activeGraphicIndex;

    // Text-specific
    private boolean center;
    private String font;
    private boolean shadowed;
    private String text;
    private String colour;
    private String overcolour;
    private String activecolour;
    private String activetext;

    // Layer-specific
    private boolean hide;
    private boolean fill;
    private int scroll;

    // Model-specific
    private String model;
    private String anim;
    private String activeModel;
    private String activeAnim;
    private int zoom;
    private int xan;
    private int yan;

    //Inv-specific
    private boolean draggable;
    private boolean interactable;
    private boolean usable;
    private String margin;
    private List<String> invOptions = new ArrayList<>();

    public InterfaceComponent(String name, String type, int x, int y, int width, int height, String layer, String option, String buttonType, int clientCode, Map<Integer, InterfaceFileParser.Script> scripts, String overlayer) {
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.layer = layer;
        this.option = option;
        this.buttonType = buttonType;
        this.clientCode = clientCode;
        this.scripts = scripts;
        this.overlayer = overlayer;
    }

    // Basic
    public String getName() { return name; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getClientCode() { return clientCode; }
    public String getLayer() { return layer; }
    public String getOption() { return option; }
    public String getButtonType() { return buttonType; }
    public Map<Integer, InterfaceFileParser.Script> getScripts() { return scripts; }
    public String getActionTarget() { return actionTarget; }
    public void setActionTarget(String actionTarget) { this.actionTarget = actionTarget; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActionVerb() { return actionVerb; }
    public void setActionVerb(String actionVerb) { this.actionVerb = actionVerb; }
    public String getOverlayer() { return overlayer; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setClientCode(int clientCode) { this.clientCode = clientCode; }
    public void setLayer(String layer) { this.layer = layer; }
    public void setOption(String option) { this.option = option; }
    public void setButtonType(String buttonType) { this.buttonType = buttonType; }
    public void setScripts(Map<Integer, InterfaceFileParser.Script> scripts) { this.scripts = scripts; }
    public void setOverlayer(String overlayer) { this.overlayer = overlayer; }

    // Graphic-specific
    public String getGraphicName() { return graphicName; }
    public int getGraphicIndex() { return graphicIndex; }
    public void setGraphicName(String name) { this.graphicName = name; }
    public void setGraphicIndex(int index) { this.graphicIndex = index; }
    public String getActiveGraphicName() { return activeGraphicName; }
    public int getActiveGraphicIndex() { return activeGraphicIndex; }
    public void setActiveGraphicName(String name) { this.activeGraphicName = name; }
    public void setActiveGraphicIndex(int index) { this.activeGraphicIndex = index; }


    // Text-specific
    public boolean isCenter() { return center; }
    public String getFont() { return font; }
    public boolean isShadowed() { return shadowed; }
    public String getText() { return text; }
    public String getColour() { return colour; }
    public String getOverColour() { return overcolour; }
    public void setCenter(boolean center) { this.center = center; }
    public void setFont(String font) { this.font = font; }
    public void setShadowed(boolean shadowed) { this.shadowed = shadowed; }
    public void setText(String text) { this.text = text; }
    public void setColour(String colour) { this.colour = colour; }
    public void setOverColour(String overcolour) { this.overcolour = overcolour; }
    public String getActiveColour() { return activecolour; }
    public void setActiveColour(String activecolour) { this.activecolour = activecolour; }
    public String getActiveText() { return activetext; }
    public void setActiveText(String activetext) { this.activetext = activetext; }

    // Layer-specific
    public boolean isHide() { return hide; }
    public void setHide(boolean hide) { this.hide = hide; }
    public int getScroll() { return scroll; }
    public void setScroll(int scroll) { this.scroll = scroll; }

    // Rect-specific
    public boolean isFill() { return fill; }
    public void setFill(boolean fill) {
        this.fill = fill;
    }

    // Model-specific
    public String getModel() { return model; }
    public int getXan() { return xan; }
    public int getYan() { return yan; }
    public int getZoom() { return zoom; }
    public void setXan(int xan) { this.xan = xan; }
    public void setYan(int yan) { this.yan = yan; }
    public void setZoom(int zoom) { this.zoom = zoom; }
    public void setModel(String model) { this.model = model; }
    public String getActiveModel() { return activeModel; }
    public void setActiveModel(String activeModel) { this.activeModel = activeModel; }
    public String getActiveAnim() { return activeAnim; }
    public void setActiveAnim(String activeAnim) { this.activeAnim = activeAnim; }
    public String getAnim() { return anim; }
    public void setAnim(String anim) { this.anim = anim; }

    //Inv-specific
    public List<String> getInvOptions() { return invOptions; }
    public void setInvOptions(List<String> invOptions) { this.invOptions = invOptions; }
    public void addInvOption(String option) { this.invOptions.add(option); }
    public String getMargin() { return margin; }
    public void setMargin(String margin) { this.margin = margin; }
    public boolean isDraggable() { return draggable; }
    public void setDraggable(boolean draggable) { this.draggable = draggable; }
    public boolean isInteractable() { return interactable; }
    public void setInteractable(boolean interactable) { this.interactable = interactable; }
    public boolean isUsable() { return usable; }
    public void setUsable(boolean usable) { this.usable = usable; }
}
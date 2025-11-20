package com.prism.components.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tool {
    private UUID id;
    private String name;
    private String description;
    private String shortcut;
    private List<String> arguments = new ArrayList<>();

    public Tool() {

    }

    public Tool(UUID id, String name, String description, String shortcut, List<String> arguments) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.shortcut = shortcut;
        this.arguments = arguments != null ? arguments : new ArrayList<>();
    }

    public Tool(String name, String description, String shortcut, List<String> arguments) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.shortcut = shortcut;
        this.arguments = arguments != null ? arguments : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getShortcut() {
        return shortcut;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(String argument) {
        if (this.arguments == null) {
            this.arguments = new ArrayList<>();
        }
        this.arguments.add(argument);
    }
}

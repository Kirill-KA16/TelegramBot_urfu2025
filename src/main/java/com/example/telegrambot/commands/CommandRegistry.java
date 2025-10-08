package com.example.telegrambot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry 
{
    private final Map<String, Command> commands = new HashMap<>();
    private final List<Command> commandList = new ArrayList<>();

    public void registerCommand(Command command) 
    {
        commands.put(command.getName(), command);
        commandList.add(command);
        System.out.println("The command registered: /" + command.getName());
    }

    public Command getCommand(String input) 
    {
        String cleanInput = input.replace("/", "").trim();
        return commands.get(cleanInput);
    }

    public Command getCommandByName(String name) 

    {
        return commands.get(name);
    }

    public List<Command> getAllCommands() 

    {
        return new ArrayList<>(commandList);
    }

    public boolean isCommand(String text) 
    {
        return text != null && text.startsWith("/");
    }
}
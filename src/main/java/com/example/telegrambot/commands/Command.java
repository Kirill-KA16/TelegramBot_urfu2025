package com.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class Command 
    {
    private final String name;
    private final String description;

    public Command(String name, String description) 
    {
        this.name = name;
        this.description = description;
    }

    public String getName() 
    {
        return name;
    }

    public String getDescription() 
    {
        return description;
    }

    public abstract SendMessage execute(Message message);

    public String getDetailedHelp() 
    {
        return description;
    }

    public boolean matches(String input) 
    {
        return input.equals("/" + name);
    }
}
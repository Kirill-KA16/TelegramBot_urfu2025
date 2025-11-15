package com.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class AuthorsCommand extends Command
{
    
    public AuthorsCommand() 
    {
        super("authors", "Shows information about authors");
    }

    @Override
    public SendMessage execute(Message message) 
    {
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        reply.setText("""
            Made by Kirill and Evelina
            """);
        return reply;
    }
}

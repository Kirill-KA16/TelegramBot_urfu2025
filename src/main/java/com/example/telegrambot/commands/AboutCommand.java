package com.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class AboutCommand extends Command 
{
    
    public AboutCommand() 
    {
        super("about", "Shows information abouth this bot");
    }

    @Override
    public SendMessage execute(Message message) 
    {
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        reply.setText("""
            it's a test text
            """);
        return reply;
    }

    @Override
    public String getDetailedHelp() 
    {
        return "/about - shows more information about this bot";
    }
}
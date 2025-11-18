package com.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ProfileEditCommand extends Command 
{
    
    public ProfileEditCommand() 
    {
        super("profile edit", "Edit your profile");
    }

    @Override
    public SendMessage execute(Message message) 
    {
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        reply.setText("?? Choose your aim:\n• "Weight loss"\n• Mass gain\n• Maintain fitness");
        return reply;
    }
}        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        List<String> goals = dbManager.getAvailableGoals();
        for (String goal : goals) {
            KeyboardRow row = new KeyboardRow();
            row.add(goal);
            keyboard.add(row);
        }
        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        reply.setReplyMarkup(keyboardMarkup);
        
        return reply;
    }
}
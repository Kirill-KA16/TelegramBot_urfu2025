package com.example.telegrambot.commands;

import com.example.telegrambot.database.FitnessDatabaseManager;
import com.example.telegrambot.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileCommand extends Command 
{
    
    private final DatabaseManager dbManager;
    
    public ProfileCommand() 
    {
        super("profile", "Show your profile");
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public SendMessage execute(Message message) 
    {
        Long userId = message.getFrom().getId();
        User user = dbManager.getUser(userId);
        
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        
        if (user != null && user.getGoal() != null) 
        {
            reply.setText(user.toString());
        } else {
            reply.setText("Profile isn't found. Use /profile edit to make a profile");
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton editBtn = new InlineKeyboardButton();
        editBtn.setText("Edit Profile");
        editBtn.setCallbackData("profile_edit");
        row1.add(editBtn);
        
        InlineKeyboardButton planBtn = new InlineKeyboardButton();
        planBtn.setText("Get Plan");
        planBtn.setCallbackData("get_plan");
        row1.add(planBtn);
        
        rows.add(row1);
        keyboardMarkup.setKeyboard(rows);
        reply.setReplyMarkup(keyboardMarkup);
        
        return reply;
    }
}
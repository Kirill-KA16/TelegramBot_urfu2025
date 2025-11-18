package com.example.telegrambot.commands;

import com.example.telegrambot.database.FitnessDatabaseManager;
import com.example.telegrambot.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ProfileCommand extends Command {
    
    private final FitnessDatabaseManager dbManager;
    
    public ProfileCommand() {
        super("profile", "Показать ваш фитнес-профиль");
        this.dbManager = FitnessDatabaseManager.getInstance();
    }

    @Override
    public SendMessage execute(Message message) {
        Long userId = message.getFrom().getId();
        User user = dbManager.getUser(userId);
        
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        
        if (user != null) {
            reply.setText(user.toString());
        } else {
            reply.setText(" Профиль не найден. Используйте /setup для создания профиля");
        }

        // Кнопки для управления профилем
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton setupBtn = new InlineKeyboardButton();
        setupBtn.setText("Настроить профиль");
        setupBtn.setCallbackData("setup_profile");
        row1.add(setupBtn);
        
        InlineKeyboardButton planBtn = new InlineKeyboardButton();
        planBtn.setText("Получить план");
        planBtn.setCallbackData("get_plan");
        row1.add(planBtn);
        
        rows.add(row1);
        keyboardMarkup.setKeyboard(rows);
        reply.setReplyMarkup(keyboardMarkup);
        
        return reply;
    }
}
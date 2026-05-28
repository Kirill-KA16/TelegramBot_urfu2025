package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class StartCommand extends Command
{
    public StartCommand()
    {
        super("start", "Начать работу с ботом");
    }

    @Override
    public SendMessage execute(Message message)
    {
        long userId = message.getFrom().getId();
        String firstName = message.getFrom().getFirstName() != null ? message.getFrom().getFirstName() : "друг";

        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId().toString());
        sm.setText("Привет, " + firstName + "!\n" +
                "Это фитнес-бот\n\n" +
                "Приступи к найстройке профиля");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(createButton("Мой профиль", "profile_view")),
                List.of(createButton("Редактировать профиль", "profile_edit"))
        ));
        sm.setReplyMarkup(markup);

        Database.getInstance().getUser(userId).orElseGet(() ->
        {
            User newUser = new User();
            newUser.setUserId(userId);
            Database.getInstance().updateUser(newUser);
            return newUser;
        });

        return sm;
    }

    private InlineKeyboardButton createButton(String text, String callbackData)
    {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}

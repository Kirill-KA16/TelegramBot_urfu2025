package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import com.example.telegrambot.state.StateManager;
import com.example.telegrambot.state.UserState;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class ProfileCommand extends Command
{
    public ProfileCommand()
    {
        super("profile", "Просмотреть и редактировать профиль");
    }

    @Override
    public SendMessage execute(Message message)
    {
        return showProfile(message.getFrom().getId(), message.getChatId().toString());
    }

    public SendMessage handleCallback(CallbackQuery callbackQuery)
    {
        String data = callbackQuery.getData();
        long userId = callbackQuery.getFrom().getId();
        String chatId = callbackQuery.getMessage().getChatId().toString();

        return switch (data)
        {
            case "profile_view" -> showProfile(userId, chatId);
            case "profile_edit" -> startEditProfile(chatId, userId);
            case "cancel_edit" ->
            {
                StateManager.getInstance().clearState(userId);
                yield showProfile(userId, chatId);
            }
            default -> handleEditStep(data, userId, chatId);
        };
    }

    private SendMessage showProfile(long userId, String chatId)
    {
        User user = Database.getInstance().getUser(userId).orElse(new User());
        String text;

        if (user.getGoal() == null)
        {
            text = "Профиль ещё не заполнен\n\nНажми «Редактировать профиль», чтобы начать!";
        }
        else
        {
            text = """
                    *Твой профиль*

                    Цель: %s
                    Пол: %s
                    Возраст: %s
                    Вес: %s кг
                    Рост: %s см
                    Уровень подготовки: %s
                    Оборудование: %s
                    """.formatted(
                    orEmpty(user.getGoal()),
                    orEmpty(user.getGender()),
                    user.getAge() != null ? user.getAge() + " лет" : "—",
                    user.getWeight() != null ? String.format("%.1f", user.getWeight()) : "—",
                    user.getHeight() != null ? String.format("%.0f", user.getHeight()) : "—",
                    orEmpty(user.getFitnessLevel()),
                    orEmpty(user.getEquipment())
            );
        }

        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText(text);
        sm.enableMarkdown(true);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(createButton("Редактировать профиль", "profile_edit"))
        ));
        sm.setReplyMarkup(markup);

        return sm;
    }

    private String orEmpty(String s)
    {
        return s == null || s.isBlank() ? "не указано" : s;
    }

    private SendMessage startEditProfile(String chatId, long userId)
    {
        StateManager.getInstance().setState(userId, UserState.AWAITING_GOAL);

        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText("Редактирование профиля\n\nВыбери цель тренировок:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(createButton("Похудение", "goal_Похудение"), createButton("Набор массы", "goal_Набор массы")),
                List.of(createButton("Поддержание формы", "goal_Поддержание формы")),
                List.of(createButton("Сила и выносливость", "goal_Сила и выносливость")),
                List.of(createButton("Отмена", "cancel_edit"))
        ));
        sm.setReplyMarkup(markup);

        return sm;
    }

    private SendMessage handleEditStep(String data, long userId, String chatId)
    {
        User user = Database.getInstance().getUser(userId).orElse(new User());
        UserState state = StateManager.getInstance().getState(userId);

        if (data.startsWith("goal_") && state == UserState.AWAITING_GOAL)
        {
            user.setGoal(data.substring(5));
            Database.getInstance().updateUser(user);
            StateManager.getInstance().setState(userId, UserState.AWAITING_GENDER);
            return askGender(chatId);
        }

        if (data.startsWith("gender_") && state == UserState.AWAITING_GENDER)
        {
            user.setGender(data.equals("gender_Мужской") ? "Мужской" : "Женский");
            Database.getInstance().updateUser(user);
            StateManager.getInstance().setState(userId, UserState.AWAITING_AGE);
            return askAge(chatId);
        }

        if (data.startsWith("level_") && state == UserState.AWAITING_FITNESS_LEVEL)
        {
            user.setFitnessLevel(switch (data)
            {
                case "level_Новичок" -> "Новичок";
                case "level_Средний" -> "Средний";
                case "level_Продвинутый" -> "Продвинутый";
                default -> user.getFitnessLevel();
            });
            Database.getInstance().updateUser(user);
            StateManager.getInstance().setState(userId, UserState.AWAITING_EQUIPMENT);
            return askEquipment(chatId);
        }

        if (data.startsWith("equip_") && state == UserState.AWAITING_EQUIPMENT)
        {
            user.setEquipment(switch (data)
            {
                case "equip_Дома без инвентаря" -> "Дома без инвентаря";
                case "equip_Дома с инвентарем" -> "Дома с инвентарем";
                case "equip_Зал" -> "Зал";
                default -> user.getEquipment();
            });
            Database.getInstance().updateUser(user);
            StateManager.getInstance().clearState(userId);

            SendMessage sm = new SendMessage();
            sm.setChatId(chatId);
            sm.setText("Профиль успешно обновлён!");
            sm.setReplyMarkup(showProfile(userId, chatId).getReplyMarkup());
            return sm;
        }

        return null;
    }

    private SendMessage askGender(String chatId)
    {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText("Укажи свой пол:");

        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(List.of(
                List.of(createButton("Мужской", "gender_Мужской"), createButton("Женский", "gender_Женский")),
                List.of(createButton("Отмена", "cancel_edit"))
        ));
        sm.setReplyMarkup(m);
        return sm;
    }

    private SendMessage askAge(String chatId)
    {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText("Сколько тебе лет? (напиши число)");
        return sm;
    }

    private SendMessage askEquipment(String chatId)
    {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText("Где ты тренируешься?");

        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(List.of(
                List.of(createButton("Дома без инвентаря", "equip_Дома без инвентаря")),
                List.of(createButton("Дома с гантелями/резинками", "equip_Дома с инвентарем")),
                List.of(createButton("В тренажёрном зале", "equip_Зал")),
                List.of(createButton("Отмена", "cancel_edit"))
        ));
        sm.setReplyMarkup(m);
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

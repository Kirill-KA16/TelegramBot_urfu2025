package com.example.telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.example.telegrambot.commands.*;
import com.example.telegrambot.models.*;
import com.example.telegrambot.database.*;

import java.util.Properties;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot
{
    private final String botToken;
    private final String botUsername;
    private final CommandRegistry commandRegistry;
    private final DatabaseManager dbManager;
    
    private Map<Long, String> userStates = new HashMap<>();

    public Bot()
    {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/bot.properties"))
        {
            props.load(fis);
            this.botToken = props.getProperty("bot.token");
            this.botUsername = props.getProperty("bot.username");
            if (botToken == null || botUsername == null)
            {
                throw new RuntimeException("botToken or botUsername not set in bot.properties file");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load configuration", e);
        }
        
        this.commandRegistry = new CommandRegistry();
        this.dbManager = DatabaseManager.getInstance();
        registerCommands();
    }
    
    private void registerCommands()
    {
        commandRegistry.registerCommand(new AboutCommand());
        commandRegistry.registerCommand(new AuthorsCommand());
        commandRegistry.registerCommand(new HelpCommand(commandRegistry));
        commandRegistry.registerCommand(new ProfileEditCommand());
        commandRegistry.registerCommand(new ProfileCommand());
        commandRegistry.registerCommand(new TrainingPlanCommand());
    }

    @Override
    public String getBotUsername()
    {
        return botUsername;
    }

    @Override
    public String getBotToken()
    {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        if (update.hasMessage() && update.getMessage().hasText())
        {
            Message msg = update.getMessage();
            Long userId = msg.getFrom().getId();
            String messageText = msg.getText();
            
            String userState = userStates.get(userId);
            
            if (userState != null) {
                User user = dbManager.getUser(userId);
                if (user == null) {
                    user = new User();
                    user.setUserId(userId);
                }
                processProfileEditStep(userId, messageText, userState, user);
                return;
            }
            
            if (commandRegistry.isCommand(messageText))
            {
                processCommand(update);
            }
            else
            {
                processTextMessage(update);
            }
        }
        else if (update.hasCallbackQuery()) 
        {
            processCallbackQuery(update);
        }
    }
    
    private void processCommand(Update update)
    {
        String messageText = update.getMessage().getText();
        
        if (commandRegistry.isExactCommand(messageText))
        {
            Command command = commandRegistry.getCommand(messageText);
            
            if (command != null)
            {
                if (command instanceof ProfileEditStep) {
                    Long userId = update.getMessage().getFrom().getId();
                    userStates.put(userId, "waiting_goal");
                    sendMessage(userId, "Выберите вашу цель:\n• Похудение\n• Набор массы\n• Поддержание формы");
                    return;
                }
                
                SendMessage reply = command.execute(update.getMessage());
                try
                {
                    execute(reply);
                }
                catch (TelegramApiException e)
                {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            else
            {
                processTextMessage(update);
            }
        }
        else
        {
            processTextMessage(update);
        }
    }

    private void processProfileEditStep(Long userId, String messageText, String step, User user) {
        switch (step) {
            case "waiting_goal":
                user.setGoal(messageText);
                dbManager.saveUser(user);
                sendMessage(userId, "Цель установлена: " + messageText + "\n Укажите ваш пол (М/Ж):");
                userStates.put(userId, "waiting_gender");
                break;
                
            case "waiting_gender":
                user.setGender(messageText);
                dbManager.saveUser(user);
                sendMessage(userId, "Пол установлен\n Укажите ваш возраст:");
                userStates.put(userId, "waiting_age");
                break;
                
            case "waiting_age":
                try {
                    user.setAge(Integer.parseInt(messageText));
                    dbManager.saveUser(user);
                    sendMessage(userId, "Возраст установлен\n Укажите ваш вес (кг):");
                    userStates.put(userId, "waiting_weight");
                } catch (NumberFormatException e) {
                    sendMessage(userId, "Введите корректный возраст (число):");
                }
                break;
                
            case "waiting_weight":
                try {
                    user.setWeight(Double.parseDouble(messageText));
                    dbManager.saveUser(user);
                    sendMessage(userId, "Вес установлен\n Укажите ваш рост (см):");
                    userStates.put(userId, "waiting_height");
                } catch (NumberFormatException e) {
                    sendMessage(userId, "Введите корректный вес (число):");
                }
                break;
                
            case "waiting_height":
                try {
                    user.setHeight(Double.parseDouble(messageText));
                    dbManager.saveUser(user);
                    sendMessage(userId, "Рост установлен\n Выберите уровень подготовки (Начинающий/Средний/Продвинутый):");
                    userStates.put(userId, "waiting_level");
                } catch (NumberFormatException e) {
                    sendMessage(userId, "Введите корректный рост (число):");
                }
                break;
                
            case "waiting_level":
                user.setFitnessLevel(messageText);
                dbManager.saveUser(user);
                sendMessage(userId, "Уровень установлен\n Укажите доступный инвентарь (Без инвентаря/Гантели/Тренажерный зал):");
                userStates.put(userId, "waiting_equipment");
                break;
                
            case "waiting_equipment":
                user.setEquipment(messageText);
                dbManager.saveUser(user);
                sendMessage(userId, "?? Профиль настроен! Используйте /plan для получения плана тренировок");
                userStates.remove(userId); // Завершаем настройку
                break;
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void processCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Message message = update.getCallbackQuery().getMessage();
        Long userId = update.getCallbackQuery().getFrom().getId();
        
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        
        switch (callbackData) {
            case "profile_edit":
                userStates.put(userId, "waiting_goal");
                reply.setText("?? Выберите вашу цель:\n• Похудение\n• Набор массы\n• Поддержание формы");
                break;
            case "get_plan":
                User user = dbManager.getUser(userId);
                if (user != null && user.getGoal() != null) {
                    reply.setText("Используйте команду /plan для получения вашего плана тренировок");
                } else {
                    reply.setText("Сначала настройте профиль командой /profile edit");
                }
                break;
            default:
                reply.setText("Неизвестная команда");
        }
        
        try {
            execute(reply);
        } catch (TelegramApiException e) {
            System.err.println("Error processing callback: " + e.getMessage());
        }
    }

    private void processTextMessage(Update update)
    {
        SendMessage reply = new SendMessage();
        reply.setChatId(update.getMessage().getChatId().toString());
        reply.setText("??");
        try
        {
            execute(reply);
        }
        catch (TelegramApiException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        try
        {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
        }
        catch (TelegramApiException e)
        {
            e.printStackTrace();
        }
    }
}
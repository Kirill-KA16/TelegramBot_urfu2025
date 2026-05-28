package com.example.telegrambot;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.telegrambot.commands.*;
import com.example.telegrambot.database.*;
import com.example.telegrambot.entity.User;
import com.example.telegrambot.state.StateManager;
import com.example.telegrambot.state.UserState;

import java.util.Properties;
import java.io.FileInputStream;
import java.util.List;

public class Bot extends TelegramLongPollingBot
{
    private final String botToken;
    private final String botUsername;
    private final CommandRegistry commandRegistry;
    private final Database db = Database.getInstance();

    public Bot(String botToken, String botUsername)
    {
        super(new DefaultBotOptions(), botToken);

        this.botToken = botToken;
        this.botUsername = botUsername;

        this.commandRegistry = new CommandRegistry();
        registerCommands();
    }

    private void registerCommands()
    {
        commandRegistry.registerCommand(new StartCommand());
        commandRegistry.registerCommand(new AboutCommand());
        commandRegistry.registerCommand(new AuthorsCommand());
        commandRegistry.registerCommand(new HelpCommand(commandRegistry));
        commandRegistry.registerCommand(new ProfileCommand());
        commandRegistry.registerCommand(new CalculatorsCommand());
        commandRegistry.registerCommand(new CreatePlanCommand());
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
            String messageText = update.getMessage().getText();
            if (commandRegistry.isCommand(messageText) || messageText.startsWith("/help "))
            {
                processCommand(update);
            }
            else
            {
                long userId = update.getMessage().getFrom().getId();
                UserState state = StateManager.getInstance().getState(userId);
                if (state != UserState.NONE)
                {
                    processProfileTextInput(update, state);
                }
                else
                {
                    processTextMessage(update);
                }
            }
        }
        else if (update.hasCallbackQuery())
        {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();

            if (data.startsWith("profile_")
                    || data.startsWith("goal_")
                    || data.startsWith("gender_")
                    || data.startsWith("level_")
                    || data.startsWith("equip_")
                    || "cancel_edit".equals(data))
            {
                Command profileCmd = commandRegistry.getCommandByName("profile");
                if (profileCmd instanceof ProfileCommand profileCommand)
                {
                    SendMessage response = profileCommand.handleCallback(callbackQuery);
                    if (response != null)
                    {
                        try
                        {
                            execute(response);
                        }
                        catch (TelegramApiException e)
                        {
                            System.err.println("Error sending callback response: " + e.getMessage());
                        }
                    }

                    AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build();

                    try
                    {
                        execute(answer);
                    }
                    catch (TelegramApiException e)
                    {
                        System.err.println("Error answering callback query: " + e.getMessage());
                    }
                }
            }
            else if (data.startsWith("calc_"))
            {
                Command calculatorsCmd = commandRegistry.getCommandByName("calculators");
                if (calculatorsCmd instanceof CalculatorsCommand calculatorsCommand)
                {
                    SendMessage response = calculatorsCommand.handleCallback(callbackQuery);
                    if (response != null)
                    {
                        try
                        {
                            execute(response);
                        }
                        catch (TelegramApiException e)
                        {
                            System.err.println("Error sending callback response: " + e.getMessage());
                        }
                    }

                    AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                            .callbackQueryId(callbackQuery.getId())
                            .build();

                    try
                    {
                        execute(answer);
                    }
                    catch (TelegramApiException e)
                    {
                        System.err.println("Error answering callback query: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void processCommand(Update update)
    {
        String messageText = update.getMessage().getText();
        Command command = null;

        if (messageText.startsWith("/help"))
        {
            command = commandRegistry.getCommand("/help");
        }
        else
        {
            command = commandRegistry.getCommand(messageText);
        }

        if (command != null)
        {
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
            SendMessage errorReply = new SendMessage();
            errorReply.setChatId(update.getMessage().getChatId().toString());
            errorReply.setText("unknown command");
            try
            {
                execute(errorReply);
            }
            catch (TelegramApiException e)
            {
                System.out.println("Error: " + e.getMessage());
            }
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

    private void processProfileTextInput(Update update, UserState state)
    {
        Message message = update.getMessage();
        long userId = message.getFrom().getId();
        String text = message.getText().trim().replace(",", ".");
        String chatId = message.getChatId().toString();

        User user = db.getUser(userId).orElseGet(() ->
        {
            User newUser = new User();
            newUser.setUserId(userId);
            db.updateUser(newUser);
            return newUser;
        });

        SendMessage reply = new SendMessage();
        reply.setChatId(chatId);

        try
        {
            switch (state)
            {
                case AWAITING_AGE:
                {
                    int age = Integer.parseInt(text);
                    if (age < 10 || age > 120)
                    {
                        reply.setText("Возраст должен быть от 10 до 120 лет. Введи корректное значение:");
                        execute(reply);
                        return;
                    }
                    user.setAge(age);
                    db.updateUser(user);
                    StateManager.getInstance().setState(userId, UserState.AWAITING_WEIGHT);
                    reply.setText("Укажи свой текущий вес в кг (например: 72.5):");
                    break;
                }
                case AWAITING_WEIGHT:
                {
                    double weight = Double.parseDouble(text);
                    if (weight < 30 || weight > 300)
                    {
                        reply.setText("Вес должен быть от 30 до 300 кг. Введи корректное значение:");
                        execute(reply);
                        return;
                    }
                    user.setWeight(weight);
                    db.updateUser(user);
                    StateManager.getInstance().setState(userId, UserState.AWAITING_HEIGHT);
                    reply.setText("Укажи свой текущий рост в сантиметрах (например: 175):");
                    break;
                }
                case AWAITING_HEIGHT:
                {
                    double height = Double.parseDouble(text);
                    if (height < 130 || height > 250)
                    {
                        reply.setText("Рост должен быть от 130 до 250 см. Введи корректное значение:");
                        execute(reply);
                        return;
                    }
                    user.setHeight(height);
                    db.updateUser(user);
                    StateManager.getInstance().setState(userId, UserState.AWAITING_FITNESS_LEVEL);
                    reply.setText("Какой у тебя уровень подготовки?");

                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    markup.setKeyboard(List.of(
                            List.of(createButton("Новичок", "level_Новичок")),
                            List.of(createButton("Средний", "level_Средний")),
                            List.of(createButton("Продвинутый", "level_Продвинутый")),
                            List.of(createButton("Отмена", "cancel_edit"))
                    ));
                    reply.setReplyMarkup(markup);
                    break;
                }
                default:
                {
                    reply.setText("Продолжаем редактирование профиля...");
                    break;
                }
            }
            execute(reply);
        }
        catch (NumberFormatException e)
        {
            reply.setText("Пожалуйста, введи корректное число:");
            try
            {
                execute(reply);
            }
            catch (TelegramApiException ex)
            {
                System.err.println("Error: " + ex.getMessage());
            }
        }
        catch (TelegramApiException e)
        {
            System.err.println("Error processing profile input: " + e.getMessage());
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData)
    {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public static void main(String[] args)
    {
        try
        {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("src/main/resources/bot.properties"))
            {
                props.load(fis);
            }

            String token = props.getProperty("bot.token");
            String username = props.getProperty("bot.username");

            if (token == null || username == null)
            {
                throw new RuntimeException("botToken or botUsername not set in bot.properties file");
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot(token, username));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

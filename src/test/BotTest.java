package com.example.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bot Tests")
class BotTest
{
    private Bot bot;

    @BeforeEach
    void setUp()
    {
        bot = new Bot();
    }

    private Update createTextUpdate(String text, long userId)
    {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        user.setId(userId);
        chat.setId(12345L);
        message.setChat(chat);
        message.setText(text);
        message.setFrom(user);
        update.setMessage(message);

        return update;
    }

    private Update createCallbackUpdate(String callbackData)
    {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        user.setId(123L);
        chat.setId(12345L);
        message.setChat(chat);
        message.setFrom(user);
        callbackQuery.setData(callbackData);
        callbackQuery.setId("callback_123");
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(user);
        update.setCallbackQuery(callbackQuery);

        return update;
    }

    @Test
    @DisplayName("Bot should be created successfully")
    void testBotCreation()
    {
        assertNotNull(bot);

        String username = bot.getBotUsername();
        String token = bot.getBotToken();

        assertNotNull(username);
        assertNotNull(token);
        assertFalse(username.isEmpty());
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Bot should have all commands registered")
    void testCommandRegistration() throws Exception
    {
        Field registryField = Bot.class.getDeclaredField("commandRegistry");
        registryField.setAccessible(true);

        CommandRegistry registry = (CommandRegistry) registryField.get(bot);
        assertNotNull(registry);

        int commandCount = registry.getAllCommands().size();
        assertTrue(commandCount >= 6, "Expected at least 6 commands, found: " + commandCount);
    }

    @Test
    @DisplayName("Bot should process commands without errors")
    void testProcessCommands()
    {
        String[] commands = {"/start", "/about", "/authors", "/help", "/profile", "/calculators", "/help about"};

        for (String command : commands)
        {
            Update update = createTextUpdate(command, 100L);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update), "Failed on command: " + command);
        }
    }

    @Test
    @DisplayName("Bot should handle unknown command")
    void testUnknownCommand()
    {
        Update update = createTextUpdate("/unknown_command", 100L);
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle plain text message")
    void testPlainTextMessage()
    {
        Update update = createTextUpdate("Hello bot", 100L);
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle empty message")
    void testEmptyMessage()
    {
        Update update = createTextUpdate("", 100L);
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle null message")
    void testNullMessage()
    {
        Update update = new Update();
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle profile callbacks")
    void testProfileCallbacks()
    {
        String[] callbacks = {"profile_edit", "goal_lose", "gender_male", "level_Новичок", "cancel_edit"};

        for (String callback : callbacks)
        {
            Update update = createCallbackUpdate(callback);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update), "Failed on callback: " + callback);
        }
    }

    @Test
    @DisplayName("Bot should handle calculators callbacks")
    void testCalculatorsCallbacks()
    {
        String[] callbacks = {"calc_bmi", "calc_water", "calc_calories", "calc_bfp"};

        for (String callback : callbacks)
        {
            Update update = createCallbackUpdate(callback);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update), "Failed on callback: " + callback);
        }
    }

    @Test
    @DisplayName("Bot should handle unknown callback")
    void testUnknownCallback()
    {
        Update update = createCallbackUpdate("unknown_callback");
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle message with null text")
    void testMessageWithNullText()
    {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        User user = new User();

        user.setId(123L);
        chat.setId(12345L);
        message.setChat(chat);
        message.setText(null);
        message.setFrom(user);
        update.setMessage(message);

        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle very long message")
    void testVeryLongMessage()
    {
        StringBuilder longText = new StringBuilder();

        for (int i = 0; i < 1000; i++)
        {
            longText.append("very long text ");
        }

        Update update = createTextUpdate(longText.toString(), 100L);
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    @Test
    @DisplayName("Bot should handle multiple messages from same user")
    void testMultipleMessages()
    {
        for (int i = 0; i < 10; i++)
        {
            Update update = createTextUpdate("/about", 100L);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update));
        }
    }

    @Test
    @DisplayName("Bot should handle messages from different users")
    void testMessagesFromDifferentUsers()
    {
        for (long userId = 1; userId <= 5; userId++)
        {
            Update update = createTextUpdate("/start", userId);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update));
        }
    }

    @Test
    @DisplayName("Bot main method should exist")
    void testMainMethodExists()
    {
        assertDoesNotThrow(() -> Bot.class.getMethod("main", String[].class));
    }

    @Test
    @DisplayName("Bot should have required Telegram methods")
    void testBotStructure()
    {
        assertDoesNotThrow(() ->
        {
            Bot.class.getMethod("getBotUsername");
            Bot.class.getMethod("getBotToken");
            Bot.class.getMethod("onUpdateReceived", Update.class);
        });
    }
}

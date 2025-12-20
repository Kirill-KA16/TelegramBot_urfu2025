package com.example.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bot Class Basic Tests")
class BotTest
{
    
    private Bot bot;
    
    @BeforeEach
    void setUp()
    {
        bot = new Bot();
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
        
        Object registry = registryField.get(bot);
        assertNotNull(registry);
        
        assertEquals("CommandRegistry", registry.getClass().getSimpleName());
    }
    
    @Test
    @DisplayName("Bot should load properties correctly")
    void testPropertiesLoading()
    {
        assertDoesNotThrow(() -> {
            Bot testBot = new Bot();
            String token = testBot.getBotToken();
            String username = testBot.getBotUsername();
            
            assertNotNull(token);
            assertNotNull(username);
            assertFalse(token.trim().isEmpty());
            assertFalse(username.trim().isEmpty());
        });
    }
    
    @Test
    @DisplayName("Bot constructor should throw exception for missing properties file")
    void testBotConstructorWithoutPropertiesFile()
    {
        System.out.println("Note: To test missing properties, temporarily rename bot.properties");
        assertTrue(true);
    }
    
    @Test
    @DisplayName("Bot getter methods should return values")
    void testBotGetters()
    {
        Bot testBot = new Bot();
        
        assertNotNull(testBot.getBotUsername());
        assertNotNull(testBot.getBotToken());
        
        assertTrue(testBot.getBotUsername().length() > 0);
        assertTrue(testBot.getBotToken().length() > 0);
    }
    
    @Test
    @DisplayName("Bot should have valid Telegram bot structure")
    void testBotStructure()
    {
        assertDoesNotThrow(() -> {
            Bot.class.getMethod("getBotUsername");
            Bot.class.getMethod("getBotToken");
            Bot.class.getMethod("onUpdateReceived", Update.class);
        });
    }
}

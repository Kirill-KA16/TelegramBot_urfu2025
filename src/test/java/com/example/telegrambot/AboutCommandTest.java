package com.example.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.example.telegrambot.commands.AboutCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;

import static org.junit.jupiter.api.Assertions.*;

class AboutCommandTest
{
    
    private AboutCommand aboutCommand;
    private Message message;
    
    @BeforeEach
    void setUp()
    {
        aboutCommand = new AboutCommand();
        
        message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
    }
    
    @Test
    void testConstructorAndGetters()
    {
        assertEquals("about", aboutCommand.getName());
        assertEquals("Shows information abouth this bot", aboutCommand.getDescription());
    }
    
    @Test
    void testExecuteMethod()
    {
        SendMessage result = aboutCommand.execute(message);
        
        assertNotNull(result);
        assertEquals("12345", result.getChatId());
        assertTrue(result.getText().contains("it's a test text"));
    }
    
    @Test
    void testMatchesMethod()
    {
        assertTrue(aboutCommand.matches("/about"));
        assertFalse(aboutCommand.matches("about"));
        assertFalse(aboutCommand.matches("/help"));
        assertFalse(aboutCommand.matches("/about g"));
        assertFalse(aboutCommand.matches("/ about"));
    }
    
    @Test
    void testGetDetailedHelp()
    {
        String detailedHelp = aboutCommand.getDetailedHelp();
        assertNotNull(detailedHelp);
        assertTrue(detailedHelp.contains("/about"));
        assertTrue(detailedHelp.contains("shows more information"));
    }
}

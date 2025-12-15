package com.example.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.example.telegrambot.commands.AuthorsCommand;

import org.telegram.telegrambots.meta.api.objects.Chat;

import static org.junit.jupiter.api.Assertions.*;

class AuthorsCommandTest {
    
    private AuthorsCommand authorsCommand;
    private Message message;
    
    @BeforeEach
    void setUp() {
        authorsCommand = new AuthorsCommand();
        
        message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
    }
    
    @Test
    void testExecuteReturnsSendMessageWithText() {
        SendMessage result = authorsCommand.execute(message);
        
        assertNotNull(result);
        assertEquals("12345", result.getChatId());
        assertTrue(result.getText().contains("Kirill"));
        assertTrue(result.getText().contains("Evelina"));
    }
    
    @Test
    void testCommandHasCorrectName() {
        assertEquals("authors", authorsCommand.getName());
    }
    
    @Test
    void testCommandHasCorrectDescription() {
        assertEquals("Shows information about authors", 
                    authorsCommand.getDescription());
    }
    
    @Test
    void testMatchesMethod() {
        assertTrue(authorsCommand.matches("/authors"));
        assertFalse(authorsCommand.matches("authors"));
        assertFalse(authorsCommand.matches("/about"));
    }
}
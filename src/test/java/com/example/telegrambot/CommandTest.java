package com.example.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.example.telegrambot.commands.Command;

import org.telegram.telegrambots.meta.api.objects.Chat;

import static org.junit.jupiter.api.Assertions.*;

// Concrete implementation for testing abstract class Command
class TestCommand extends Command {
    public TestCommand() {
        super("test", "Test command");
    }
    
    @Override
    public SendMessage execute(Message message) {
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        reply.setText("Test executed");
        return reply;
    }
}

class CommandTest {
    
    private TestCommand testCommand;
    
    @BeforeEach
    void setUp() {
        testCommand = new TestCommand();
    }
    
    @Test
    void testNameAndDescriptionGetters() {
        assertEquals("test", testCommand.getName());
        assertEquals("Test command", testCommand.getDescription());
    }
    
    @Test
    void testMatchesWithSlash() {
        assertTrue(testCommand.matches("/test"));
    }
    
    @Test
    void testMatchesWithoutSlash() {
        assertFalse(testCommand.matches("test"));
        assertFalse(testCommand.matches("/other"));
    }
    
    @Test
    void testDefaultDetailedHelp() {
        assertEquals("Test command", testCommand.getDetailedHelp());
    }
    
    @Test
    void testExecuteMethod() {
        // Arrange
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
        
        // Act
        SendMessage result = testCommand.execute(message);
        
        // Assert
        assertNotNull(result);
        assertEquals("12345", result.getChatId());
        assertEquals("Test executed", result.getText());
    }
}
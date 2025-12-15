package com.example.telegrambot;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.example.telegrambot.commands.Command;
import com.example.telegrambot.commands.CommandRegistry;
import com.example.telegrambot.commands.HelpCommand;
import com.example.telegrambot.commands.AboutCommand;
import com.example.telegrambot.commands.AuthorsCommand;

import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelpCommandTest {
    
    private CommandRegistry commandRegistry;
    private HelpCommand helpCommand;
    private Message message;
    
    @BeforeEach
    void setUp() {
        commandRegistry = mock(CommandRegistry.class);
        helpCommand = new HelpCommand(commandRegistry);
        
        message = new Message();
        Chat chat = new Chat();
        chat.setId(12345L);
        message.setChat(chat);
    }
    
    @Test
    void testConstructorAndGetters() {
        assertEquals("help", helpCommand.getName());
        assertEquals("Shows help for commands", helpCommand.getDescription());
    }
    
    @Nested
    class GeneralHelpTests {
        
        @Test
        void testExecuteGeneralHelp() {
            message.setText("/help");
            
            Command aboutCommand = new AboutCommand();
            Command authorsCommand = new AuthorsCommand();
            
            List<Command> commands = Arrays.asList(aboutCommand, authorsCommand, helpCommand);
            
            when(commandRegistry.getAllCommands()).thenReturn(commands);
            
            SendMessage result = helpCommand.execute(message);
            
            assertNotNull(result);
            assertEquals("12345", result.getChatId());
            String response = result.getText();
            
            assertTrue(response.contains("/about"));
            assertTrue(response.contains("/authors"));
            assertFalse(response.contains("/help"));
            assertTrue(response.contains("Commands available:"));
            assertTrue(response.contains("use help"));
        }
        
        @Test
        void testExecuteWithNoOtherCommands() {
            message.setText("/help");
            
            List<Command> commands = Arrays.asList(helpCommand);
            when(commandRegistry.getAllCommands()).thenReturn(commands);
            
            SendMessage result = helpCommand.execute(message);
            
            String response = result.getText();
            assertTrue(response.contains("Commands available:"));
            assertFalse(response.contains("/about"));
            assertFalse(response.contains("/authors"));
        }
    }
    
    @Nested
    class SpecificHelpTests {
        
        @Test
        void testExecuteSpecificHelp() {
            message.setText("/help about");
            
            Command aboutCommand = new AboutCommand();
            when(commandRegistry.getCommandByName("about")).thenReturn(aboutCommand);
            
            SendMessage result = helpCommand.execute(message);
            
            assertNotNull(result);
            assertEquals("12345", result.getChatId());
            assertTrue(result.getText().contains("/about - shows more information about this bot"));
        }
        
        @Test
        void testExecuteNonExistentCommandHelp() {
            message.setText("/help unknown");
            
            when(commandRegistry.getCommandByName("unknown")).thenReturn(null);
            
            SendMessage result = helpCommand.execute(message);
            
            String response = result.getText();
            assertTrue(response.contains("Command 'unknown' not found"));
        }
        
        @Test
        void testExecuteHelpWithExtraSpaces() {
            message.setText("/help  about  ");
            
            Command aboutCommand = new AboutCommand();
            when(commandRegistry.getCommandByName("about")).thenReturn(aboutCommand);
            
            SendMessage result = helpCommand.execute(message);
            
            assertTrue(result.getText().contains("/about - shows more information about this bot"));
        }
        
        @Test
        void testExecuteHelpWithSlashInParameter() {
            message.setText("/help /about");
            
            Command aboutCommand = new AboutCommand();
            when(commandRegistry.getCommandByName("about")).thenReturn(aboutCommand);
            
            SendMessage result = helpCommand.execute(message);
            
            assertTrue(result.getText().contains("/about - shows more information about this bot"));
        }
    }
    
    @Test
    void testMatchesMethod() {
        assertTrue(helpCommand.matches("/help"));
        assertFalse(helpCommand.matches("help"));
        assertFalse(helpCommand.matches("/about"));
    }
    
    @Test
    void testGetDetailedHelp() {
        assertEquals("Shows help for commands", helpCommand.getDetailedHelp());
    }
}
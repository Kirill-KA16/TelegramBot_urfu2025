package com.example.telegrambot;

import org.junit.jupiter.api.Test;

import com.example.telegrambot.commands.Command;
import com.example.telegrambot.commands.CommandRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandRegistryTest {
    
    private CommandRegistry commandRegistry;
    private Command mockCommand1;
    private Command mockCommand2;
    
    @BeforeEach
    void setUp() {
        commandRegistry = new CommandRegistry();
        
        mockCommand1 = mock(Command.class);
        when(mockCommand1.getName()).thenReturn("test1");
        when(mockCommand1.getDescription()).thenReturn("Test command 1");
        
        mockCommand2 = mock(Command.class);
        when(mockCommand2.getName()).thenReturn("test2");
        when(mockCommand2.getDescription()).thenReturn("Test command 2");
    }
    
    @Test
    void testRegisterCommand() {
        // Act
        commandRegistry.registerCommand(mockCommand1);
        
        // Assert
        Command retrieved = commandRegistry.getCommandByName("test1");
        assertNotNull(retrieved);
        assertEquals("test1", retrieved.getName());
    }
    
    @Test
    void testRegisterMultipleCommands() {
        // Arrange & Act
        commandRegistry.registerCommand(mockCommand1);
        commandRegistry.registerCommand(mockCommand2);
        
        // Assert
        assertEquals(2, commandRegistry.getAllCommands().size());
    }
    
    @Nested
    class GetCommandTests {
        
        @BeforeEach
        void setUp() {
            commandRegistry.registerCommand(mockCommand1);
            commandRegistry.registerCommand(mockCommand2);
        }
        
        @Test
        void testGetCommandByName() {
            Command result = commandRegistry.getCommandByName("test1");
            assertNotNull(result);
            assertEquals("test1", result.getName());
        }
        
        @Test
        void testGetCommandByNonExistentName() {
            Command result = commandRegistry.getCommandByName("nonexistent");
            assertNull(result);
        }
        
        @Test
        void testGetCommandWithSlash() {
            Command result = commandRegistry.getCommand("/test1");
            assertNotNull(result);
            assertEquals("test1", result.getName());
        }
        
        @Test
        void testGetCommandWithoutSlash() {
            Command result = commandRegistry.getCommand("test1");
            assertNotNull(result);
            assertEquals("test1", result.getName());
        }
        
        @Test
        void testGetCommandWithSpaces() {
            Command result = commandRegistry.getCommand("  /test1  ");
            assertNotNull(result);
            assertEquals("test1", result.getName());
        }
        
        @Test
        void testGetAllCommands() {
            List<Command> commands = commandRegistry.getAllCommands();
            assertEquals(2, commands.size());
            assertTrue(commands.contains(mockCommand1));
            assertTrue(commands.contains(mockCommand2));
        }
    }
    
    @Nested
    class IsCommandTests {
        
        @Test
        void testIsCommandWithSlash() {
            assertTrue(commandRegistry.isCommand("/start"));
            assertTrue(commandRegistry.isCommand("/help test"));
        }
        
        @Test
        void testIsCommandWithoutSlash() {
            assertFalse(commandRegistry.isCommand("start"));
            assertFalse(commandRegistry.isCommand("help test"));
        }
        
        @Test
        void testIsCommandWithNull() {
            assertFalse(commandRegistry.isCommand(null));
        }
        
        @Test
        void testIsCommandWithEmptyString() {
            assertFalse(commandRegistry.isCommand(""));
        }
        
        @Test
        void testIsCommandWithSpaces() {
            assertFalse(commandRegistry.isCommand("   "));
        }
    }
}
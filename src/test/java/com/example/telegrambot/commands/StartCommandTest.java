package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class StartCommandTest {

    private static final long USER_ID = 123456L;
    private static final long CHAT_ID = 987654L;
    private static final String FIRST_NAME = "Алексей";

    private StartCommand startCommand;

    private MockedStatic<Database> databaseMock;
    private Database databaseInstance;

    @BeforeEach
    void setUp() {
        startCommand = new StartCommand();

        databaseInstance = mock(Database.class);
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getInstance).thenReturn(databaseInstance);
    }

    @AfterEach
    void tearDown() {
        databaseMock.close();
    }

    @Test
    @DisplayName("/start от нового пользователя — приветствие, кнопки и создание записи в БД")
    void execute_NewUser_ShouldGreetCreateUserAndShowButtons() {
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.empty());

        Message message = createMessage(FIRST_NAME);

        SendMessage result = startCommand.execute(message);

        assertEquals("Привет, " + FIRST_NAME + "!\nЭто фитнес-бот\n\nПриступи к найстройке профиля", result.getText());
        assertEquals(String.valueOf(CHAT_ID), result.getChatId());

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        assertNotNull(markup);
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();
        assertEquals(2, keyboard.size());

        assertEquals("Мой профиль", keyboard.get(0).get(0).getText());
        assertEquals("profile_view", keyboard.get(0).get(0).getCallbackData());

        assertEquals("Редактировать профиль", keyboard.get(1).get(0).getText());
        assertEquals("profile_edit", keyboard.get(1).get(0).getCallbackData());

        verify(databaseInstance).getUser(USER_ID);
        verify(databaseInstance).updateUser(argThat(user ->
                user != null && USER_ID == user.getUserId()
        ));
    }

    @Test
    @DisplayName("/start от существующего пользователя — приветствие и кнопки, без лишнего update")
    void execute_ExistingUser_ShouldGreetAndShowButtonsWithoutCreating() {
        User existingUser = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(existingUser));

        Message message = createMessage("Мария");

        SendMessage result = startCommand.execute(message);

        assertTrue(result.getText().startsWith("Привет, Мария!"));
        assertTrue(result.getText().contains("Это фитнес-бот"));

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        assertNotNull(markup);
        assertEquals(2, markup.getKeyboard().size());

        verify(databaseInstance).getUser(USER_ID);
        verify(databaseInstance, never()).updateUser(any());
    }

    @Test
    @DisplayName("/start когда firstName == null — использует fallback 'друг'")
    void execute_FirstNameNull_ShouldUseFallbackName() {
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.empty());

        Message message = createMessage(null);

        SendMessage result = startCommand.execute(message);

        assertEquals("Привет, друг!\nЭто фитнес-бот\n\nПриступи к найстройке профиля", result.getText());
    }

    private Message createMessage(String firstName) {
        Message message = new Message();
        message.setChat(new Chat(CHAT_ID, "private"));

        org.telegram.telegrambots.meta.api.objects.User telegramUser =
                new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        if (firstName != null) {
            telegramUser.setFirstName(firstName);
        }
        message.setFrom(telegramUser);

        return message;
    }
}

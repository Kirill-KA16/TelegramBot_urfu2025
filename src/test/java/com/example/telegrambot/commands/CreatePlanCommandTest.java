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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreatePlanCommandTest {

    private static final long USER_ID = 123456L;
    private static final long CHAT_ID = 987654L;

    private CreatePlanCommand createPlanCommand;

    private MockedStatic<Database> databaseMock;
    private Database databaseInstance;

    @BeforeEach
    void setUp() {
        createPlanCommand = new CreatePlanCommand();

        databaseInstance = mock(Database.class);
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getInstance).thenReturn(databaseInstance);
    }

    @AfterEach
    void tearDown() {
        databaseMock.close();
    }

    @Test
    @DisplayName("Если профиль не заполнен - ошибка, план не сохраняется")
    void execute_WhenProfileNotComplete_ShouldReturnError() {
        Message message = createTelegramMessage();

        User incompleteUser = new User(USER_ID);

        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(incompleteUser));

        SendMessage response = createPlanCommand.execute(message);

        assertNotNull(response);
        assertEquals(String.valueOf(CHAT_ID), response.getChatId());
        assertTrue(response.getText().contains("заполните профиль"));
    }

    @Test
    @DisplayName("Если профиль заполнен - план должен сгенерироваться")
    void execute_WhenProfileComplete_ShouldGeneratePlan() {
        Message message = createTelegramMessage();

        User completeUser = createCompleteUser();
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(completeUser));

        SendMessage response = createPlanCommand.execute(message);

        assertNotNull(response);
        assertEquals(String.valueOf(CHAT_ID), response.getChatId());
        assertNotNull(response.getText());
        assertTrue(response.getText().length() > 0);
    }

    @Test
    @DisplayName("Если пользователь не найден в БД - должен вернуть ошибку о заполнении профиля")
    void execute_WhenUserNotFound_ShouldReturnProfileError() {
        Message message = createTelegramMessage();

        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.empty());

        SendMessage response = createPlanCommand.execute(message);

        assertTrue(response.getText().contains("заполните профиль"));
    }

    private Message createTelegramMessage() {
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        message.setChat(chat);

        org.telegram.telegrambots.meta.api.objects.User telegramUser = 
            new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        message.setFrom(telegramUser);

        return message;
    }

    private User createCompleteUser() {
        User user = new User(USER_ID);
        user.setAge(30);
        user.setWeight(75.0);
        user.setHeight(175.0);
        user.setFitnessLevel("Средний");
        user.setGoal("Набор");
        user.setEquipment("гантели");
        return user;
    }
}

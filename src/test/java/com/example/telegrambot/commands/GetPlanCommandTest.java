package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class GetPlanCommandTest {

    private static final long USER_ID = 123456L;
    private static final long CHAT_ID = 987654L;

    private GetPlanCommand getPlanCommand;

    private MockedStatic<Database> databaseMock;
    private Database databaseInstance;

    @BeforeEach
    void setUp() {
        getPlanCommand = new GetPlanCommand();

        databaseInstance = mock(Database.class);
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getInstance).thenReturn(databaseInstance);
    }

    @AfterEach
    void tearDown() {
        databaseMock.close();
    }

    @Test
    @DisplayName("Если плана нет - вернуть сообщение с предложением создать план")
    void execute_WhenNoPlan_ShouldSuggestCreate() {
        Message message = createTelegramMessage();

        when(databaseInstance.getWorkoutPlan(USER_ID)).thenReturn(Optional.empty());

        SendMessage response = getPlanCommand.execute(message);

        assertNotNull(response);
        assertEquals(String.valueOf(CHAT_ID), response.getChatId());
        assertTrue(response.getText().contains("нет") || 
                   response.getText().contains("создайте") ||
                   response.getText().contains("/create_plan"));
    }

    @Test
    @DisplayName("Если план есть - вернуть его без изменений")
    void execute_WhenPlanExists_ShouldReturnPlanAsIs() {
        Message message = createTelegramMessage();

        String existingPlan = "Ваш тренировочный план на неделю\n\nПонедельник - Силовая тренировка\n- Приседания - 3x8-10";
        when(databaseInstance.getWorkoutPlan(USER_ID)).thenReturn(Optional.of(existingPlan));

        SendMessage response = getPlanCommand.execute(message);

        assertNotNull(response);
        assertEquals(String.valueOf(CHAT_ID), response.getChatId());
        assertEquals(existingPlan, response.getText(), "План должен вернуться без изменений");
    }

    @Test
    @DisplayName("При ошибке БД вернуть сообщение об ошибке")
    void execute_WhenDatabaseError_ShouldReturnErrorMessage() {
        Message message = createTelegramMessage();

        when(databaseInstance.getWorkoutPlan(anyLong())).thenThrow(new RuntimeException("Connection error"));

        SendMessage response = getPlanCommand.execute(message);

        assertNotNull(response);
        assertNotNull(response.getText());
        assertTrue(response.getText().contains("Ошибка") || response.getText().contains("ошибк"));
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
}
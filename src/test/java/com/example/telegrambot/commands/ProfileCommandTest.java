package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User; // наш User
import com.example.telegrambot.state.StateManager;
import com.example.telegrambot.state.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProfileCommandTest {

    private static final long USER_ID = 123456L;
    private static final long CHAT_ID = 987654L;
    private static final String CHAT_ID_STR = String.valueOf(CHAT_ID);

    private ProfileCommand profileCommand;

    private MockedStatic<Database> databaseMock;
    private MockedStatic<StateManager> stateManagerMock;

    private Database databaseInstance;
    private StateManager stateManagerInstance;

    @BeforeEach
    void setUp() {
        profileCommand = new ProfileCommand();

        databaseInstance = mock(Database.class);
        databaseMock = Mockito.mockStatic(Database.class);
        databaseMock.when(Database::getInstance).thenReturn(databaseInstance);

        stateManagerInstance = mock(StateManager.class);
        stateManagerMock = Mockito.mockStatic(StateManager.class);
        stateManagerMock.when(StateManager::getInstance).thenReturn(stateManagerInstance);
    }

    @AfterEach
    void tearDown() {
        databaseMock.close();
        stateManagerMock.close();
    }

    @Test
    @DisplayName("execute() должен вызывать showProfile через рефлексию или косвенно")
    void execute_ShouldReturnProfileMessage() {
        org.telegram.telegrambots.meta.api.objects.Message message = createTelegramMessage();

        SendMessage result = profileCommand.execute(message);

        assertNotNull(result);
        assertEquals(CHAT_ID_STR, result.getChatId());
        assertNotNull(result.getText());
    }

    @Test
    @DisplayName("handleCallback с profile_view должен показывать профиль")
    void handleCallback_ProfileView() {
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(new User(USER_ID)));

        CallbackQuery cq = createCallbackQuery("profile_view");

        SendMessage result = profileCommand.handleCallback(cq);

        assertNotNull(result);
        assertTrue(result.getText().contains("*Твой профиль*") || result.getText().contains("Профиль ещё не заполнен"));
    }

    @Test
    @DisplayName("handleCallback с profile_edit должен начинать редактирование")
    void handleCallback_ProfileEdit() {
        CallbackQuery cq = createCallbackQuery("profile_edit");

        SendMessage result = profileCommand.handleCallback(cq);

        assertEquals("Редактирование профиля\n\nВыбери цель тренировок:", result.getText());
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_GOAL);
    }

    @Test
    @DisplayName("handleCallback с cancel_edit должен очистить состояние и показать профиль")
    void handleCallback_CancelEdit() {
        CallbackQuery cq = createCallbackQuery("cancel_edit");

        SendMessage result = profileCommand.handleCallback(cq);

        verify(stateManagerInstance).clearState(USER_ID);
        assertNotNull(result.getText());
        assertNotNull(result.getReplyMarkup());
    }

    @Test
    @DisplayName("Редактирование: выбор цели → сохранение и переход к полу")
    void handleEditStep_GoalSelection() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_GOAL);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(user));

        SendMessage result = profileCommand.handleCallback(createCallbackQuery("goal_Похудение"));

        assertEquals("Укажи свой пол:", result.getText());
        assertEquals("Похудение", user.getGoal());
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_GENDER);
    }

    @Test
    @DisplayName("Редактирование: выбор пола → сохранение и запрос возраста")
    void handleEditStep_GenderSelection() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_GENDER);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(user));

        SendMessage result = profileCommand.handleCallback(createCallbackQuery("gender_Мужской"));

        assertEquals("Сколько тебе лет? (напиши число)", result.getText());
        assertEquals("Мужской", user.getGender());
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_AGE);
    }

    @Test
    @DisplayName("Редактирование: выбор уровня подготовки → переход к оборудованию")
    void handleEditStep_FitnessLevel() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_FITNESS_LEVEL);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(user));

        SendMessage result = profileCommand.handleCallback(createCallbackQuery("level_Средний"));

        assertEquals("Где ты тренируешься?", result.getText());
        assertEquals("Средний", user.getFitnessLevel());
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_EQUIPMENT);
    }

    @Test
    @DisplayName("Редактирование: выбор оборудования → завершение и очистка состояния")
    void handleEditStep_EquipmentSelection() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_EQUIPMENT);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(user));
        when(databaseInstance.getUser(USER_ID)).thenReturn(Optional.of(user));

        SendMessage result = profileCommand.handleCallback(createCallbackQuery("equip_Зал"));

        assertEquals("Профиль успешно обновлён!", result.getText());
        assertEquals("Зал", user.getEquipment());
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).clearState(USER_ID);
        assertNotNull(result.getReplyMarkup());
    }

    @Test
    @DisplayName("handleCallback с неизвестным callback_data должен вернуть null")
    void handleCallback_UnknownData() {
        when(stateManagerInstance.getState(anyLong())).thenReturn(UserState.NONE);

        SendMessage result = profileCommand.handleCallback(createCallbackQuery("unknown_123"));

        assertNull(result);
    }


    private org.telegram.telegrambots.meta.api.objects.Message createTelegramMessage() {
        org.telegram.telegrambots.meta.api.objects.Message message = new org.telegram.telegrambots.meta.api.objects.Message();
        message.setChat(new Chat(CHAT_ID, "private"));

        org.telegram.telegrambots.meta.api.objects.User telegramUser = new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        message.setFrom(telegramUser);

        return message;
    }

    private CallbackQuery createCallbackQuery(String data) {
        CallbackQuery cq = new CallbackQuery();
        cq.setData(data);

        org.telegram.telegrambots.meta.api.objects.User telegramUser = new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        cq.setFrom(telegramUser);

        org.telegram.telegrambots.meta.api.objects.Message message = new org.telegram.telegrambots.meta.api.objects.Message();
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        message.setChat(chat);
        cq.setMessage(message);

        return cq;
    }
}

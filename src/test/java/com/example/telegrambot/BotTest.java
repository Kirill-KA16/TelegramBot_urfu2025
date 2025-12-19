package com.example.telegrambot;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User; 
import com.example.telegrambot.state.StateManager;
import com.example.telegrambot.state.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BotTest {
    private static final long USER_ID = 123456L;
    private static final long CHAT_ID = 987654L;

    private Bot bot;
    private MockedStatic<Database> databaseMock;
    private MockedStatic<StateManager> stateManagerMock;
    private Database databaseInstance;
    private StateManager stateManagerInstance;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws Exception {
        System.setOut(new PrintStream(outContent));

        databaseInstance = mock(Database.class);
        databaseMock = mockStatic(Database.class);
        databaseMock.when(Database::getInstance).thenReturn(databaseInstance);

        stateManagerInstance = mock(StateManager.class);
        stateManagerMock = mockStatic(StateManager.class);
        stateManagerMock.when(StateManager::getInstance).thenReturn(stateManagerInstance);

        bot = new Bot();

        Field dbField = Bot.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(bot, databaseInstance);

        bot = spy(bot);
        doReturn(null).when(bot).execute(any(BotApiMethod.class));
        doReturn(true).when(bot).execute(any(AnswerCallbackQuery.class));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        if (databaseMock != null) databaseMock.close();
        if (stateManagerMock != null) stateManagerMock.close();
    }

    @Test
    @DisplayName("Все callback-данные профиля (profile_*, goal_*, etc.) обрабатываются (не падают с NPE)")
    void onUpdateReceived_ProfileCallbacks_DoNotThrow() {
        String[] callbacks = {
                "profile_view", "profile_edit", "cancel_edit",
                "goal_Похудение", "goal_Набор массы",
                "gender_Мужской", "gender_Женский",
                "level_Новичок", "level_Средний", "level_Продвинутый",
                "equip_Дома без инвентаря", "equip_Дома с инвентарем", "equip_Зал"
        };

        for (String data : callbacks) {
            Update update = createCallbackUpdate(data);
            assertDoesNotThrow(() -> bot.onUpdateReceived(update));
        }
    }

    @Test
    @DisplayName("Текстовый ввод валидного возраста — сохраняет в БД и переходит к весу")
    void processProfileTextInput_ValidAge_SavesAndChangesState() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_AGE);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(java.util.Optional.of(user));

        Update update = createTextUpdate("35");
        bot.onUpdateReceived(update);

        assertEquals(Integer.valueOf(35), user.getAge());
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_WEIGHT);
    }

    @Test
    @DisplayName("Текстовый ввод некорректного возраста — ничего не сохраняет")
    void processProfileTextInput_InvalidAge_DoesNotSave() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_AGE);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(java.util.Optional.of(user));

        Update update = createTextUpdate("8");
        bot.onUpdateReceived(update);

        assertNull(user.getAge());
        verify(databaseInstance, never()).updateUser(any());
        verify(stateManagerInstance, never()).setState(anyLong(), any());
    }

    @Test
    @DisplayName("Текстовый ввод веса (с запятой) — корректно парсится")
    void processProfileTextInput_WeightWithComma_ParsesCorrectly() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_WEIGHT);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(java.util.Optional.of(user));

        Update update = createTextUpdate("72,5");
        bot.onUpdateReceived(update);

        assertEquals(72.5, user.getWeight(), 0.001);
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_HEIGHT);
    }

    @Test
    @DisplayName("Текстовый ввод роста — сохраняет и переходит к уровню подготовки")
    void processProfileTextInput_HeightInput_SavesAndChangesState() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.AWAITING_HEIGHT);
        User user = new User(USER_ID);
        when(databaseInstance.getUser(USER_ID)).thenReturn(java.util.Optional.of(user));

        Update update = createTextUpdate("175");
        bot.onUpdateReceived(update);

        assertEquals(175.0, user.getHeight(), 0.001);
        verify(databaseInstance).updateUser(user);
        verify(stateManagerInstance).setState(USER_ID, UserState.AWAITING_FITNESS_LEVEL);
    }

    @Test
    @DisplayName("Обычный текст в состоянии NONE — не падает и заходит в processTextMessage")
    void processTextMessage_InNoneState_DoesNotThrow() {
        when(stateManagerInstance.getState(USER_ID)).thenReturn(UserState.NONE);
        Update update = createTextUpdate("привет бот");
        assertDoesNotThrow(() -> bot.onUpdateReceived(update));
    }

    private Update createTextUpdate(String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);

        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        message.setChat(chat);

        org.telegram.telegrambots.meta.api.objects.User telegramUser = new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        message.setFrom(telegramUser);

        update.setMessage(message);
        return update;
    }

    private Update createCallbackUpdate(String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("test_callback_id");

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        message.setChat(chat);
        callbackQuery.setMessage(message);

        org.telegram.telegrambots.meta.api.objects.User telegramUser = new org.telegram.telegrambots.meta.api.objects.User();
        telegramUser.setId(USER_ID);
        callbackQuery.setFrom(telegramUser);

        update.setCallbackQuery(callbackQuery);
        return update;
    }
}

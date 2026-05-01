package com.example.telegrambot;

import com.example.telegrambot.commands.CalculatorsCommand;
import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CalculatorsCommandTest
{
    private CalculatorsCommand command;
    private MockedStatic<Database> databaseMockedStatic;

    @Mock
    private Database databaseMock;
    @Mock
    private User userMock;
    @Mock
    private CallbackQuery callbackQueryMock;
    @Mock
    private Message messageMock;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        command = new CalculatorsCommand();

        databaseMockedStatic = mockStatic(Database.class);
        databaseMockedStatic.when(Database::getInstance).thenReturn(databaseMock);

        when(callbackQueryMock.getFrom()).thenReturn(new org.telegram.telegrambots.meta.api.objects.User(1L, "Test", false));
        when(callbackQueryMock.getMessage()).thenReturn(messageMock);
        when(messageMock.getChatId()).thenReturn(12345L);
        
        when(databaseMock.getUser(anyLong())).thenReturn(Optional.of(userMock));
    }

    @AfterEach
    void tearDown()
    {
        databaseMockedStatic.close();
    }

    @ParameterizedTest
    @CsvSource({
            "50.0, 180.0, Недостаток веса",
            "70.0, 180.0, Нормальный вес",
            "85.0, 180.0, Избыточный вес",
            "100.0, 180.0, Ожирение"
    })
    @DisplayName("ИМТ: Проверка всех категорий веса")
    void testCalculateImt(double weight, double height, String expectedPhrase)
    {
        setupUser(weight, height, 25, "male", "intermediate", "maintain");
        when(callbackQueryMock.getData()).thenReturn("calc_imt|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        String text = response.getText();
        assertTrue(text.contains(expectedPhrase), 
            "Текст должен содержать '" + expectedPhrase + "', но получен: " + text);
    }

    @Test
    @DisplayName("Вода: Расчет нормы воды по весу")
    void testCalculateWater()
    {
        setupUser(100.0, 180.0, 25, "male", "intermediate", "maintain");
        when(callbackQueryMock.getData()).thenReturn("calc_water|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        assertTrue(response.getText().contains("3,50") || response.getText().contains("3.50"), 
            "Должно быть 3.50 литра для 100кг. Получено: " + response.getText());
    }

    @Test
    @DisplayName("Калории: Мужчина, набор массы, средняя активность")
    void testCaloriesMaleGainIntermediate()
    {
        setupUser(80.0, 180.0, 25, "male", "intermediate", "gain");
        when(callbackQueryMock.getData()).thenReturn("calc_calories|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        assertTrue(response.getText().contains("3298"), 
            "Ожидается 3298 ккал. Получено: " + response.getText());
    }

    @Test
    @DisplayName("Калории: Женщина, похудение, новичок")
    void testCaloriesFemaleLossBeginner()
    {
        setupUser(60.0, 165.0, 30, "female", "beginner", "loss");
        when(callbackQueryMock.getData()).thenReturn("calc_calories|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        assertTrue(response.getText().contains("1384") || response.getText().contains("1184"), 
            "Ожидается около 1184-1384 ккал. Получено: " + response.getText());
    }

    @Test
    @DisplayName("БЖУ: Похудение (проверка пропорций)")
    void testBjuLoss()
    {
        setupUser(60.0, 165.0, 30, "female", "beginner", "loss");
        when(callbackQueryMock.getData()).thenReturn("calc_bju|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        String text = response.getText();
        assertTrue(text.contains("Белки"), "Должен быть расчет белков. Получено: " + text);
        assertTrue(text.contains("Жиры"), "Должен быть расчет жиров. Получено: " + text);
    }

    @Test
    @DisplayName("БЖУ: Поддержание/Набор (проверка пропорций)")
    void testBjuMaintain()
    {
        setupUser(60.0, 165.0, 30, "female", "advanced", "maintain");
        when(callbackQueryMock.getData()).thenReturn("calc_bju|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        String text = response.getText();
        assertTrue(text.contains("Белки"), "Должен быть расчет белков. Получено: " + text);
        assertTrue(text.contains("Жиры"), "Должен быть расчет жиров. Получено: " + text);
    }

    @Test
    @DisplayName("Ошибка: Профиль не найден")
    void testProfileNotFound()
    {
        when(databaseMock.getUser(anyLong())).thenReturn(Optional.empty());
        when(callbackQueryMock.getData()).thenReturn("calc_imt|999");

        SendMessage response = command.handleCallback(callbackQueryMock);

        assertTrue(response.getText().contains("Профиль не найден") || response.getText().contains("профиль"), 
            "Должно быть сообщение об ошибке. Получено: " + response.getText());
    }

    @Test
    @DisplayName("Ошибка: Неизвестный тип калькулятора")
    void testUnknownCalculator()
    {
        setupUser(70.0, 170.0, 25, "male", "beginner", "maintain");
        when(callbackQueryMock.getData()).thenReturn("calc_unknown|1");

        SendMessage response = command.handleCallback(callbackQueryMock);

        assertTrue(response.getText().contains("Неизвестный") || response.getText().contains("unknown"), 
            "Должно быть сообщение о неизвестном калькуляторе. Получено: " + response.getText());
    }

    @Test
    @DisplayName("Execute: Просит заполнить профиль, если данных нет")
    void testExecuteEmptyProfile()
    {
        when(userMock.getWeight()).thenReturn(null);
        when(databaseMock.getUser(anyLong())).thenReturn(Optional.of(userMock));
        
        when(messageMock.getFrom()).thenReturn(new org.telegram.telegrambots.meta.api.objects.User(1L, "User", false));
        when(messageMock.getChatId()).thenReturn(100L);

        SendMessage response = command.execute(messageMock);

        assertTrue(response.getText().contains("заполните профиль") || response.getText().contains("Сначала"), 
            "Должно быть сообщение о заполнении профиля. Получено: " + response.getText());
    }

    private void setupUser(double weight, double height, int age, String gender, String fitness, String goal)
    {
        when(userMock.getWeight()).thenReturn(weight);
        when(userMock.getHeight()).thenReturn(height);
        when(userMock.getAge()).thenReturn(age);
        when(userMock.getGender()).thenReturn(gender);
        when(userMock.getFitnessLevel()).thenReturn(fitness);
        when(userMock.getGoal()).thenReturn(goal);
    }
}

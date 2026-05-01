package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

public class CalculatorsCommand extends Command {

    public CalculatorsCommand() {
        super("calculators", "Калькуляторы: ИМТ, калории, БЖУ, норма воды");
    }

    @Override
    public SendMessage execute(Message message) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        Optional<User> userOpt = Database.getInstance().getUser(userId);
        if (userOpt.isEmpty() || userOpt.get().getWeight() == null || userOpt.get().getHeight() == null || userOpt.get().getAge() == 0) {
            return SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Сначала заполните профиль: возраст, рост, вес и пол.\nИспользуйте /profile")
                    .build();
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(btn("Подсчёт калорий", "calc_calories|" + userId)),
                List.of(btn("Расчёт БЖУ", "calc_bju|" + userId)),
                List.of(btn("Калькулятор ИМТ", "calc_imt|" + userId)),
                List.of(btn("Норма воды", "calc_water|" + userId))
        ));

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("Выберите калькулятор:")
                .replyMarkup(markup)
                .build();
    }

    public SendMessage handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        long userId = callbackQuery.getFrom().getId();
        long chatId = callbackQuery.getMessage().getChatId();

        if (!data.startsWith("calc_")) {
            return null;
        }

        String[] parts = data.split("\\|", 2);
        String calcType = parts[0];
        long targetUserId = parts.length > 1 ? Long.parseLong(parts[1]) : userId;

        Optional<User> userOpt = Database.getInstance().getUser(userId);
        if (userOpt.isEmpty()) {
            return SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Профиль не найден")
                    .build();
        }

        User user = userOpt.get();
        String result = switch (calcType) {
            case "calc_imt" -> calculateImt(user);
            case "calc_water" -> calculateWater(user);
            case "calc_calories" -> calculateCalories(user);
            case "calc_bju" -> calculateBju(user);
            default -> "Неизвестный калькулятор";
        };

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(result)
                .build();
    }


    private String calculateImt(User user) {
        double weight = user.getWeight();
        double heightCm = user.getHeight();
        double heightM = heightCm / 100.0;

        double imt = weight / (heightM * heightM);
        String status = imt < 18.5 ? "Недостаток веса" :
                imt < 25 ? "Нормальный вес" :
                        imt < 30 ? "Избыточный вес" : "Ожирение";

        return String.format("Ваш ИМТ: %.1f\n%s", imt, status);
    }

    private String calculateWater(User user) {
        double weight = user.getWeight();
        return String.format("Рекомендуемая норма воды:\n%.2f литра в день\n(примерно 35 мл на 1 кг веса)", weight * 0.035);
    }

    private String calculateCalories(User user) {
        double calories = calculateBaseCalories(user);
        String goalText = getGoalText(user.getGoal());

        return String.format("Суточная норма калорий (%s):\n%.0f ккал", goalText, calories);
    }

    private String calculateBju(User user) {
        double calories = calculateBaseCalories(user);
        String goalText = getGoalText(user.getGoal());

        boolean isLoss = goalText.equals("похудение");
        double proteinRatio = isLoss ? 0.35 : 0.30;
        double fatRatio = isLoss ? 0.25 : 0.30;
        double carbsRatio = 1.0 - proteinRatio - fatRatio;

        int protein = (int) Math.round(calories * proteinRatio / 4);
        int fat = (int) Math.round(calories * fatRatio / 9);
        int carbs = (int) Math.round(calories * carbsRatio / 4);

        return String.format("""
                Суточные БЖУ (%s):
                Белки: %d г
                Жиры: %d г
                Углеводы: %d г
                Всего: %.0f ккал""", goalText, protein, fat, carbs, calories);
    }


    private double calculateBaseCalories(User user) {
        double weight = user.getWeight();
        double heightCm = user.getHeight();
        int age = user.getAge();
        String gender = (user.getGender() != null ? user.getGender() : "male").toLowerCase();

        double bmr = gender.contains("male") || gender.contains("муж")
                ? 10 * weight + 6.25 * heightCm - 5 * age + 5
                : 10 * weight + 6.25 * heightCm - 5 * age - 161;

        double activityMultiplier = switch ((user.getFitnessLevel() != null ? user.getFitnessLevel() : "intermediate").toLowerCase()) {
            case "beginner", "новичок" -> 1.2;
            case "intermediate", "средний" -> 1.55;
            case "advanced", "продвинутый" -> 1.725;
            default -> 1.375;
        };

        double calories = bmr * activityMultiplier;

        String goal = (user.getGoal() != null ? user.getGoal() : "maintain").toLowerCase();
        if (goal.contains("loss") || goal.contains("похудение")) {
            calories -= 400;
        } else if (goal.contains("gain") || goal.contains("набор")) {
            calories += 500;
        }

        return calories;
    }

    private String getGoalText(String goal) {
        if (goal == null) return "поддержание";
        String lower = goal.toLowerCase();
        if (lower.contains("loss") || lower.contains("похудение")) return "похудение";
        if (lower.contains("gain") || lower.contains("набор")) return "набор массы";
        return "поддержание";
    }

    private InlineKeyboardButton btn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}

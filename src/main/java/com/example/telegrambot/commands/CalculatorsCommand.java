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

        if (!data.startsWith("calc_")) return null;

        String[] parts = data.split("\\|", 2);
        String calcType = parts[0];
        long targetUserId = parts.length > 1 ? Long.parseLong(parts[1]) : userId;

        // Защита от нажатия чужих кнопок
        if (targetUserId != userId) {
            return SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Это не ваши данные!")
                    .build();
        }

        Optional<User> userOpt = Database.getInstance().getUser(userId);
        if (userOpt.isEmpty()) {
            return SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text("Профиль не найден")
                    .build();
        }

        String result = calculate(userOpt.get(), calcType);

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(result)
                .build();
    }

    private String calculate(User user, String type) {
        double weight = user.getWeight();
        double heightCm = user.getHeight();
        double heightM = heightCm / 100.0;
        int age = user.getAge();
        String gender = (user.getGender() != null ? user.getGender() : "male").toLowerCase();
        String goal = (user.getGoal() != null ? user.getGoal() : "maintain").toLowerCase();
        String level = (user.getFitnessLevel() != null ? user.getFitnessLevel() : "intermediate").toLowerCase();

        return switch (type) {
            case "calc_imt" -> {
                double imt = weight / (heightM * heightM);
                String status = imt < 18.5 ? "Недостаток веса" :
                        imt < 25 ? "Нормальный вес" :
                                imt < 30 ? "Избыточный вес" : "Ожирение";
                yield String.format("Ваш ИМТ: %.1f\n%s", imt, status);
            }

            case "calc_water" ->
                    String.format("Рекомендуемая норма воды:\n%.2f литра в день\n(примерно 35 мл на 1 кг веса)", weight * 0.035);

            case "calc_calories", "calc_bju" -> {
                double bmr = gender.contains("male") || gender.contains("муж")
                        ? 10 * weight + 6.25 * heightCm - 5 * age + 5
                        : 10 * weight + 6.25 * heightCm - 5 * age - 161;

                double activity = switch (level) {
                    case "beginner", "новичок" -> 1.2;
                    case "intermediate", "средний" -> 1.55;
                    case "advanced", "продвинутый" -> 1.725;
                    default -> 1.375;
                };

                double calories = bmr * activity;
                if (goal.contains("loss") || goal.contains("похудение")) calories -= 400;
                if (goal.contains("gain") || goal.contains("набор")) calories += 500;

                String goalText = goal.contains("loss") || goal.contains("похудение") ? "похудение" :
                        goal.contains("gain") || goal.contains("набор") ? "набор массы" : "поддержание";

                if ("calc_calories".equals(type)) {
                    yield String.format("Суточная норма калорий (%s):\n%.0f ккал", goalText, calories);
                } else {
                    double p = goal.contains("loss") || goal.contains("похудение") ? 0.35 : 0.30;
                    double f = goal.contains("loss") || goal.contains("похудение") ? 0.25 : 0.30;
                    double c = 1.0 - p - f;

                    int protein = (int) Math.round(calories * p / 4);
                    int fat = (int) Math.round(calories * f / 9);
                    int carbs = (int) Math.round(calories * c / 4);

                    yield String.format("""
                            Суточные БЖУ (%s):
                            Белки: %d г
                            Жиры: %d г
                            Углеводы: %d г
                            Всего: %.0f ккал""", goalText, protein, fat, carbs, calories);
                }
            }
            default -> "Неизвестный калькулятор";
        };
    }

    private InlineKeyboardButton btn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}

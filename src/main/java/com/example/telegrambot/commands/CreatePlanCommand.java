package com.example.telegrambot.commands;

import com.example.telegrambot.WorkoutPlanGenerator;
import com.example.telegrambot.database.Database;
import com.example.telegrambot.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class CreatePlanCommand extends Command
{
    private final WorkoutPlanGenerator generator = new WorkoutPlanGenerator();

    public CreatePlanCommand()
    {
        super("create_plan", "Создать тренировочный план на неделю");
    }

    @Override
    public SendMessage execute(Message message)
    {
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId().toString());

        try
        {
            long userId = message.getFrom().getId();
            Database db = Database.getInstance();
            User user = db.getUser(userId).orElse(new User());

            if (user.getAge() == null || user.getWeight() == null || user.getHeight() == null ||
                user.getFitnessLevel() == null || user.getGoal() == null || user.getEquipment() == null)
            {
                sm.setText("Пожалуйста, полностью заполните профиль перед созданием плана.\n\nИспользуйте команду /profile");
                return sm;
            }

            String planText = generator.generatePlan(user);
            db.saveWorkoutPlan(userId, planText);

            sm.setText(planText);
            return sm;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sm.setText("Ошибка при генерации плана:\n" + e.getClass().getSimpleName() + "\n" + e.getMessage());
            return sm;
        }
    }
}

package com.example.telegrambot.commands;

import com.example.telegrambot.database.Database;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Optional;

public class GetPlanCommand extends Command
{
    public GetPlanCommand()
    {
        super("get_plan", "Показать текущий тренировочный план");
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

            Optional<String> planOpt = db.getWorkoutPlan(userId);

            if (planOpt.isEmpty())
            {
                sm.setText("У вас ещё нет тренировочного плана.\n\nСоздайте его командой /create_plan");
            }
            else
            {
                String plan = planOpt.get();
                sm.setText(plan);
            }
            
            return sm;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sm.setText("Ошибка при получении плана: " + e.getMessage());
            return sm;
        }
    }
}

package com.example.telegrambot;

import com.example.telegrambot.entity.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class WorkoutPlanGenerator
{
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final Random random = new Random();

    private static final String BASE_URL = "https://wger.de/api/v2/";

    public String generatePlan(User user)
    {
        String goal = user.getGoal() != null ? user.getGoal() : "Поддержание";

        StringBuilder sb = new StringBuilder();
        sb.append("Ваш тренировочный план на неделю\n\n");
        sb.append("\n--------------------------------\n\n");

        switch (goal)
        {
            case "Набор" -> sb.append(generateStrengthPlan(3, "3x8-10"));
            case "Похудение" -> sb.append(generateMixedPlan(1, 3, "4x12-15"));
            case "Сила и выносливость" -> sb.append(generateMixedPlan(2, 2, "3x12"));
            default -> sb.append(generateMixedPlan(2, 1, "3x12"));
        }

        return sb.toString();
    }

    private String generateStrengthPlan(int strengthDays, String reps)
    {
        StringBuilder sb = new StringBuilder();
        String[] days = {"Понедельник", "Среда", "Пятница"};

        for (int i = 0; i < strengthDays && i < days.length; i++)
        {
            sb.append(days[i]).append(" - Силовая тренировка\n");
            sb.append(getRandomExercises(6, reps));
            sb.append("\n");
        }
        sb.append("Остальные дни - отдых или активное восстановление.\n");
        return sb.toString();
    }

    private String generateMixedPlan(int strengthDays, int cardioDays, String reps)
    {
        StringBuilder sb = new StringBuilder();
        String[] days = {"Понедельник", "Среда", "Пятница", "Суббота"};
        int idx = 0;

        for (int i = 0; i < strengthDays && idx < days.length; i++)
        {
            sb.append(days[idx++]).append(" - Силовая тренировка\n");
            sb.append(getRandomExercises(6, reps));
            sb.append("\n");
        }
        for (int i = 0; i < cardioDays && idx < days.length; i++)
        {
            sb.append(days[idx++]).append(" - Кардио\n");
            sb.append(getRandomCardio());
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getRandomExercises(int count, String reps)
    {
        List<WgerExercise> exercises = fetchWgerExercises();
        Collections.shuffle(exercises);

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(count, exercises.size());

        for (int i = 0; i < limit; i++)
        {
            sb.append("- ").append(exercises.get(i).getName()).append(" - ").append(reps).append("\n");
        }
        return sb.toString();
    }

    private List<WgerExercise> fetchWgerExercises()
    {
        String url = BASE_URL + "exerciseinfo/?language=6&limit=300";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute())
        {
            if (response.isSuccessful() && response.body() != null)
            {
                String json = response.body().string();

                JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                JsonArray results = jsonObject.getAsJsonArray("results");

                List<WgerExercise> exercises = new ArrayList<>();

                for (JsonElement elem : results)
                {
                    JsonObject obj = elem.getAsJsonObject();

                    JsonArray translations = obj.getAsJsonArray("translations");
                    if (translations != null && translations.size() > 0)
                    {
                        JsonObject translation = translations.get(0).getAsJsonObject();
                        String name = translation.get("name").getAsString();

                        if (name != null && !name.trim().isEmpty())
                        {
                            exercises.add(new WgerExercise(name));
                        }
                    }
                }

                if (!exercises.isEmpty())
                {
                    Collections.shuffle(exercises);
                    System.out.println("Загружено " + exercises.size() + " упражнений из API");
                    return exercises;
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Ошибка при загрузке упражнений из API: " + e.getMessage());
            e.printStackTrace();
        }

        throw new RuntimeException("Не удалось загрузить упражнения из API");
    }

    private String getRandomCardio()
    {
        String[] cardio = {
            "40 минут бега в умеренном темпе",
            "30-35 минут быстрой ходьбы",
            "20 минут прыжков на скакалке"
        };
        return "- " + cardio[random.nextInt(cardio.length)] + "\n";
    }

    static class WgerResponse
    {
        private List<WgerExercise> results;
        public List<WgerExercise> getResults() { return results; }
    }

    static class WgerExercise
    {
        private String name;

        public WgerExercise() {}

        public WgerExercise(String name)
        {
            this.name = name;
        }

        public String getName() { return name; }
    }
}

package com.example.telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Properties;
import java.io.FileInputStream;

public class Bot extends TelegramLongPollingBot
{
    private final String botToken;
    private final String botUsername;

    public Bot()
    {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/bot.properties"))
		{
            props.load(fis);
            this.botToken = props.getProperty("bot.token");
            this.botUsername = props.getProperty("bot.username");
            if (botToken == null || botUsername == null)
	    	{
                throw new RuntimeException("botToken or botUsername not set in bot.properties file");
            }
        }
		catch (Exception e)
		{
            throw new RuntimeException("Failed to load configuration", e);
        }
	}

    @Override
    public String getBotUsername()
    {
        return botUsername;
    }

    @Override
    public String getBotToken()
    {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        if (update.hasMessage() && update.getMessage().hasText())
		{
            String chatId = update.getMessage().getChatId().toString();
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Hello, World!");
            try
	    	{
                execute(message);
            }
	    	catch (TelegramApiException e)
	    	{
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        try
		{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
        }
		catch (TelegramApiException e)
		{
            e.printStackTrace();
        }
    }
}


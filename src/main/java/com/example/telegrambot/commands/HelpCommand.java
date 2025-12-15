package com.example.telegrambot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.List;

public class HelpCommand extends Command 
{
    
    private final CommandRegistry commandRegistry;
    
    public HelpCommand(CommandRegistry commandRegistry) 
    {
        super("help", "Shows help for commands");
        this.commandRegistry = commandRegistry;
    }

    @Override
    public SendMessage execute(Message message) 
    {
        String text = message.getText();
        String[] parts = text.split(" ", 2);
        
        SendMessage reply = new SendMessage();
        reply.setChatId(message.getChatId().toString());
        
        if (parts.length > 1) 
        {
            String commandName = parts[1].replace("/", "").trim();
            reply.setText(getCommandHelp(commandName));
        }
	else 
        {
            reply.setText(getAllCommandsHelp());
        }
        
        return reply;
    }
    
    private String getAllCommandsHelp() 
    {
        StringBuilder helpText = new StringBuilder("Commands available:\n\n");
        
        List<Command> commands = commandRegistry.getAllCommands();
        for (Command command : commands) 
	{
            if (!(command instanceof HelpCommand)) 
	    {
                helpText.append("/")
                       .append(command.getName())
                       .append(" - ")
                       .append(command.getDescription())
                       .append("\n");
            }
        }
        
        helpText.append("use help");
        return helpText.toString();
    }
    
    private String getCommandHelp(String commandName) 
    {
        Command command = commandRegistry.getCommandByName(commandName.trim());
        if (command != null) 
	{
            return command.getDetailedHelp();
        }
	else 
	{
            return "Command '" + commandName + "' not found.\n";
        }
    }
}


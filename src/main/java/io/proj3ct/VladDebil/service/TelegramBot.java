package io.proj3ct.VladDebil.service;

import io.proj3ct.VladDebil.config.BotConfig;
import io.proj3ct.VladDebil.service.commands.RemindCommandHandler;
import io.proj3ct.VladDebil.service.commands.RemindViewerCommandHandler;
import io.proj3ct.VladDebil.service.commands.StartCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final StartCommandHandler startCommandHandler;
    private final RemindCommandHandler remindCommandHandler;
    private final RemindViewerCommandHandler remindViewerCommandHandler;
//    private final RemindDeleterCommandHandler remindDeleterCommandHandler;
    final BotConfig config;
//    private final Map<String, CommandHandler> commandHandlerMap = new HashMap<>();
//    commandHandlerMap.put("/start", new StartCommandHandler());
//    commandHandlerMap.put("/r", new ReminderCommandHandler());

    public TelegramBot(StartCommandHandler startCommandHandler, RemindCommandHandler remindCommandHandler,
                       RemindViewerCommandHandler remindViewerCommandHandler, BotConfig config) {
        this.startCommandHandler = startCommandHandler;
        this.remindCommandHandler = remindCommandHandler;
        this.remindViewerCommandHandler = remindViewerCommandHandler;
        this.config = config;

        List<BotCommand> commandList = new ArrayList<>();

        commandList.add(new BotCommand("/start", "вывод приветственного сообщения"));
        commandList.add(new BotCommand("/r", "напомнить о чем-то"));
        commandList.add(new BotCommand("/viewmyreminds", "показать мои напоминания"));
        commandList.add(new BotCommand("/delete", "удалить напоминание по айди"));

        try {
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            ReminderState currentState = remindCommandHandler.getUserStateMap(chatId);

            if (currentState == null) {
                switch (messageText) {
                    case "/start":
                        SendMessage startMessage = startCommandHandler.handleCommand(update.getMessage());
                        executeCommand(startMessage, "start message");
                        break;

                    case "/r":
                        SendMessage remindMessage = remindCommandHandler.handleCommand(update.getMessage());
                        executeCommand(remindMessage, "remind message");
                        break;

                    case "спасибо":
                        sendMessage(chatId, "Не за что!");
                        break;

                    case "/viewmyreminds":
                        SendMessage remindersListMessage = remindViewerCommandHandler.handleCommand(update.getMessage());
                        executeCommand(remindersListMessage, "remind viewer message");
                        break;

//                    case "/delete":
//                        SendMessage reminderDeleteMessage = remindDeleterCommandHandler.handleCommand(update.getMessage());
//                        executeCommand(reminderDeleteMessage, "delete reminder");
//                        break;

                    default:
                        break;
                }
            } else {
                SendMessage remindMessage = remindCommandHandler.handleCommand(update.getMessage());
                executeCommand(remindMessage, "remind message");
            }

        } else if (update.hasCallbackQuery()) {
            SendMessage callbackResponse = remindCommandHandler.handleCallbackQuery(update.getCallbackQuery());
            executeCommand(callbackResponse, "callback response");
        }

    }

    public void sendReminder(long chatId, String message) {
        sendMessage(chatId, message);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void executeCommand(SendMessage message, String messageRelatedTo){
        try {
            execute(message);
            log.info("Executed {} command: {}", messageRelatedTo, message.getText());
        } catch (TelegramApiException e) {
            log.error("Error occurred while executing " + messageRelatedTo + ": " + e.getMessage());
        }
    }

}
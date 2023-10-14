package io.proj3ct.VladDebil.service;

import io.proj3ct.VladDebil.config.BotConfig;
import io.proj3ct.VladDebil.model.entity.Reminder;
import io.proj3ct.VladDebil.model.repository.ReminderRepository;
import io.proj3ct.VladDebil.model.repository.UserRepository;
import io.proj3ct.VladDebil.service.commands.StartCommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReminderRepository reminderRepository;
    @Autowired
    private StartCommandHandler startCommandHandler;
    private final Map<Long, ReminderState> userStateMap = new HashMap<>();
    private final Map<Long, String> userReminderTextMap = new HashMap<>();
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commandList = new ArrayList<>();

        commandList.add(new BotCommand("/start", "вывод приветственного сообщения"));
        commandList.add(new BotCommand("/r", "напомнить о чем-то. формат: /r сообщение; dd.MM.yyyy HH:mm"));

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

            ReminderState currentState = userStateMap.get(chatId);

            if (currentState == null) {
                switch (messageText) {
                    case "/start":
                        SendMessage startMessage = startCommandHandler.handleCommand(update.getMessage());
                        try {
                            execute(startMessage);
                        } catch (TelegramApiException e) {
                            log.error("Error occurred while executing start message: " + e.getMessage());
                        }
                        break;
                    case "/r":
                        userStateMap.put(chatId, ReminderState.AWAITING_TEXT);
                        remindCommandReceived(chatId, messageText);
                        break;
                    case "спасибо":
                        sendMessage(chatId, "Не за что!");
                }
            } else {
                remindCommandReceived(chatId, messageText);
            }

        }

    }

    private void remindCommandReceived(long chatId, String message) {
        ReminderState currentState = userStateMap.get(chatId);

        switch (currentState) {
            case AWAITING_TEXT:
                userStateMap.put(chatId, ReminderState.AWAITING_DATE);
                sendMessage(chatId, "Введите текст напоминания.");
                break;
            case AWAITING_DATE:
                userReminderTextMap.put(chatId, message);
                sendMessage(chatId, "Введите дату и время когда вам напомнить в формате dd.MM.yyyy HH:mm");
                userStateMap.put(chatId, ReminderState.AWAITING_CONFIRMATION);
                break;
            case AWAITING_CONFIRMATION:
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                LocalDateTime dateTime;

                try {
                    dateTime = LocalDateTime.parse(message, formatter);

                    Reminder reminder = new Reminder();
                    reminder.setChatId(chatId);
                    reminder.setTextToRemind(userReminderTextMap.get(chatId));
                    reminder.setReminderTime(dateTime);

                    reminderRepository.save(reminder);

                    userStateMap.put(chatId, ReminderState.COMPLETED);

                    log.info("User " + chatId + " asked to remind him " + userReminderTextMap.get(chatId) + " on " + dateTime);

                    userReminderTextMap.remove(chatId);

                    sendMessage(chatId, "Хорошо, я напомню вам об этом......");

                    userStateMap.remove(chatId);
                } catch (DateTimeParseException e) {
                    sendMessage(chatId, "Неверный формат даты, надо ввести дату в формате dd.MM.yyyy HH:mm");
                }
                break;
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



}
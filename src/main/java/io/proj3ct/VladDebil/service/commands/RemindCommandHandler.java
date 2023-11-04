package io.proj3ct.VladDebil.service.commands;

import io.proj3ct.VladDebil.model.entity.Reminder;
import io.proj3ct.VladDebil.model.entity.User;
import io.proj3ct.VladDebil.model.repository.ReminderRepository;
import io.proj3ct.VladDebil.model.repository.UserRepository;
import io.proj3ct.VladDebil.service.ReminderState;
import io.proj3ct.VladDebil.service.datepicker.MonthAndDayPicker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RemindCommandHandler implements CommandHandler, CallbackQueryHandler {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final MonthAndDayPicker monthAndDayPicker;
    private final Map<Long, ReminderState> userStateMap = new HashMap<>();
    private final Map<Long, String> userReminderTextMap = new HashMap<>();
    private final Map<Long, Month> userSelectedMonthMap = new HashMap<>();
    private final Map<Long, Integer> userSelectedDayMap = new HashMap<>();

    public RemindCommandHandler(UserRepository userRepository, ReminderRepository reminderRepository, MonthAndDayPicker monthAndDayPicker) {
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
        this.monthAndDayPicker = monthAndDayPicker;
    }

    public ReminderState getUserStateMap(long chatId) {
        return userStateMap.get(chatId);
    }

    @Override
    public SendMessage handleCommand(Message message) {
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        ReminderState currentState = userStateMap.getOrDefault(chatId, ReminderState.AWAITING_TEXT);
        log.info("Current state for chatId {}: {}", chatId, currentState);

        switch (currentState) {
            case AWAITING_TEXT:
                userStateMap.put(chatId, ReminderState.AWAITING_MONTH);
                sendMessage.setText("Введите текст напоминания.");
                break;

            case AWAITING_MONTH:
                userReminderTextMap.put(chatId, message.getText());
                sendMessage = monthAndDayPicker.getAllKeyboardMonthButtonsAndMessage(chatId);
                userStateMap.put(chatId, ReminderState.AWAITING_DAY);
                break;

            case AWAITING_TIME:
                String timeInput = message.getText();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                try {
                    LocalTime time = LocalTime.parse(timeInput, timeFormatter);
                    Month month = userSelectedMonthMap.get(chatId);
                    Integer day = userSelectedDayMap.get(chatId);

                    if (month != null && day != null) {
                        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now().getYear(), month, day, time.getHour(), time.getMinute());

                        User user = userRepository.findById(chatId).orElse(null);

                        if (user != null) {

                            Reminder reminder = new Reminder();
                            reminder.setUser(user);
                            reminder.setTextToRemind(userReminderTextMap.get(chatId));
                            reminder.setReminderTime(dateTime);

                            user.addReminder(reminder);

                            reminderRepository.save(reminder);

                            sendMessage.setText("Напоминание создано на: " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                            log.info("User " + chatId + " set a reminder for " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                                    + " Reminder: " + userReminderTextMap.get(chatId));
                        } else {
                            sendMessage.setText("Произошла ошибка при поиске пользователя");
                        }

                        userReminderTextMap.remove(chatId);
                        userSelectedMonthMap.remove(chatId);
                        userSelectedDayMap.remove(chatId);
                        userStateMap.remove(chatId);
                    } else {
                        sendMessage.setText("Не был выбран месяц или день");
                    }

                    log.info("User {} is now in {} state", chatId, userStateMap.get(chatId));

                } catch (DateTimeParseException e) {
                    sendMessage.setText("Неверный формат времени, надо ввести время в формате HH:mm");
                    log.error("Error occurred while inserting time: " + e.getMessage());
                }
                break;
        }

        return sendMessage;
    }

    @Override
    public SendMessage handleCallbackQuery(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        ReminderState currentState = userStateMap.getOrDefault(chatId, ReminderState.AWAITING_TEXT);
        log.info("Current state for chatId {}: {}", chatId, currentState);

        if (data.startsWith("MONTH_")) {
            Month month = Month.valueOf(data.substring(6));
            userSelectedMonthMap.put(chatId, month);
            sendMessage = monthAndDayPicker.getAllKeyboardDaysButtonsAndMessage(chatId, month);
            userStateMap.put(chatId, ReminderState.AWAITING_DAY);

        } else if (currentState == ReminderState.AWAITING_DAY && data.startsWith("DAY_")) {
            int day = Integer.parseInt(data.substring(4));
            userSelectedDayMap.put(chatId, day);

            sendMessage.setText("Введите время в формате HH:mm");
            userStateMap.put(chatId, ReminderState.AWAITING_TIME);

        } else {
            sendMessage.setText("Произошла ошибка");
        }

        return sendMessage;
    }

}

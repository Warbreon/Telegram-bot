package io.proj3ct.VladDebil.service.commands;

import io.proj3ct.VladDebil.model.entity.Reminder;
import io.proj3ct.VladDebil.model.entity.User;
import io.proj3ct.VladDebil.model.repository.ReminderRepository;
import io.proj3ct.VladDebil.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class RemindViewerCommandHandler implements CommandHandler {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    public RemindViewerCommandHandler(UserRepository userRepository, ReminderRepository reminderRepository) {
        this.userRepository = userRepository;
        this.reminderRepository = reminderRepository;
    }

    @Override
    public SendMessage handleCommand(Message message) {
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        User user = userRepository.findById(chatId).orElse(null);
        if (user == null) {
            sendMessage.setText("Не удалось найти пользователя");
            return sendMessage;
        }

        List<Reminder> reminderList = reminderRepository.findAllByUser(user);
        if (reminderList.isEmpty()) {
            sendMessage.setText("У вас нет активных напоминаний");

        } else {
            StringBuilder reminders = new StringBuilder("Ваши напоминания:\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for(Reminder reminder : reminderList){
                reminders.append(String.format("%s - Время: %s - ID: %d\n",
                        reminder.getTextToRemind(),
                        reminder.getReminderTime().format(formatter),
                        reminder.getId()));
            }

            sendMessage.setText(reminders.toString());
        }

        log.info("User " + chatId + " asked for a list of his reminders");
        return sendMessage;
    }
}

package io.proj3ct.VladDebil.service.commands;

import io.proj3ct.VladDebil.model.entity.Reminder;
import io.proj3ct.VladDebil.model.repository.ReminderRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;


public class RemindDeleterCommandHandler implements CommandHandler {

    private final ReminderRepository reminderRepository;

    public RemindDeleterCommandHandler(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    public SendMessage handleCommand(Message message) {
        long chatId = message.getChatId();
        String[] messageParts = message.getText().split("\\s+");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (messageParts.length < 2) {
            sendMessage.setText("Пожалуйста, укажите ID напоминания для удаления, например /delete 123");
            return sendMessage;
        }

        try {
            long reminderId = Long.parseLong(messageParts[1]);
            Reminder reminder = reminderRepository.findById(reminderId).orElse(null);

            if (reminder != null && reminder.getUser().getChatId().equals(chatId)) {
                reminderRepository.delete(reminder);
                sendMessage.setText("Напоминание " + reminderId + " удалено");

            } else {
                sendMessage.setText("Напоминание не найдено");
            }

        } catch (NumberFormatException e) {
            sendMessage.setText("ID должен быть числом, например /delete 123");
        }

        return sendMessage;
    }
}

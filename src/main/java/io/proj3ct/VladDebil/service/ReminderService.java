package io.proj3ct.VladDebil.service;

import io.proj3ct.VladDebil.model.entity.Reminder;
import io.proj3ct.VladDebil.model.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TelegramBot telegramBot;

    public ReminderService(ReminderRepository reminderRepository, TelegramBot telegramBot){
        this.reminderRepository = reminderRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders(){
        List<Reminder> reminderList = reminderRepository.findByReminderTimeBefore(LocalDateTime.now());

        for(Reminder reminder : reminderList){
            telegramBot.sendReminder(reminder.getUser().getChatId(), reminder.getTextToRemind());
            reminderRepository.delete(reminder);
        }
    }
}

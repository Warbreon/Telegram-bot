package io.proj3ct.VladDebil.service;

import io.proj3ct.VladDebil.model.Reminder;
import io.proj3ct.VladDebil.model.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;
    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(fixedRate = 60000)
    public void checkAndSendReminders(){
        List<Reminder> reminderList = reminderRepository.findByReminderTimeBefore(LocalDateTime.now());

        for(Reminder reminder : reminderList){
            telegramBot.sendReminder(reminder.getChatId(), reminder.getTextToRemind());
            reminderRepository.delete(reminder);
        }
    }
}

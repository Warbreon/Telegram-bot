package io.proj3ct.VladDebil.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name="usersReminds")
public class Reminder {

    @Id
    private Long chatId;
    private String textToRemind;
    private LocalDateTime reminderTime;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getTextToRemind() {
        return textToRemind;
    }

    public void setTextToRemind(String textToRemind) {
        this.textToRemind = textToRemind;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(chatId, reminder.chatId) &&
                Objects.equals(textToRemind, reminder.textToRemind) &&
                Objects.equals(reminderTime, reminder.reminderTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, textToRemind, reminderTime);
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "chatId=" + chatId +
                ", textToRemind='" + textToRemind + '\'' +
                ", reminderTime=" + reminderTime +
                '}';
    }
}

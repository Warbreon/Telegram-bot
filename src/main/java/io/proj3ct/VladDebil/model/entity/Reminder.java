package io.proj3ct.VladDebil.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name="usersReminds")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String textToRemind;
    private LocalDateTime reminderTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", referencedColumnName = "chatId")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id) && Objects.equals(textToRemind, reminder.textToRemind)
                && Objects.equals(reminderTime, reminder.reminderTime) && Objects.equals(user, reminder.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, textToRemind, reminderTime, user);
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", textToRemind='" + textToRemind + '\'' +
                ", reminderTime=" + reminderTime +
                ", user=" + user +
                '}';
    }
}

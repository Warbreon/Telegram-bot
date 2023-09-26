package io.proj3ct.VladDebil.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByReminderTimeBefore(LocalDateTime date);
}

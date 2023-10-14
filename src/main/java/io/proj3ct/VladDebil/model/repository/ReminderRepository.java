package io.proj3ct.VladDebil.model.repository;

import io.proj3ct.VladDebil.model.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByReminderTimeBefore(LocalDateTime date);
}

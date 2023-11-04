package io.proj3ct.VladDebil.service.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {
    SendMessage handleCommand(Message message);
}

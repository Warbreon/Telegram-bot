package io.proj3ct.VladDebil.service.commands;

import io.proj3ct.VladDebil.model.entity.User;
import io.proj3ct.VladDebil.model.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;

@Slf4j
@Component
public class StartCommandHandler implements CommandHandler{

    private final UserRepository userRepository;

    public StartCommandHandler(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public SendMessage handleCommand(Message message) {
        long chatId = message.getChatId();
        String name = message.getChat().getFirstName();

        registerUser(message);

        String answer = "Привет, " + name + ", рад тебя видеть!";
        log.info("Replied to user(Id:" + chatId + "/Name:" + name + "): " + answer);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(answer);

        return sendMessage;
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(message.getChatId());
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved: " + user);
        }
    }
}

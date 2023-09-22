package io.proj3ct.VladDebil.service;

import io.proj3ct.VladDebil.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config){
        this.config = config;
        List<BotCommand> commandList = new ArrayList<>();

        commandList.add(new BotCommand("/start", "вывод приветственного сообщения"));
        commandList.add(new BotCommand("/remind", "напомнить о чем-то"));

        try{
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e){
            log.error("Error setting bot command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken(){
        return config.getBotToken();
    }
    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/remind":
                    remindCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getText());
                    break;
                default:
                    sendMessage(chatId, "А че это ты пытаешься сделать, а?");
            }
        }


        }
        private void startCommandReceived(long chatId, String name) {

            switch (name){
                case "Nastia":
                    name = "ГНИДСКАЯ ПАСКУДОВЫПИЗДНУТАЯ ПРОБЛЯДЬ";
                    break;
                case "Vlad":
                    name = "Мой повелитель";
            }

            String answer = "Привет, " + name + ", рад тебя видеть!";
            log.info("Replied to user: " + name + " - " + answer);

            sendMessage(chatId, answer);
        }

        private void remindCommandReceived(long chatId, String name, String message){


            String answer = "Вы попросили меня напомнить вам о: " + message + "\nТак уж и быть, " + name + ", я напомню вам об этом.....";
            log.info("Replied to user: " + name + " - " + answer);
            sendMessage(chatId, answer);
        }

        private void sendMessage(long chatId, String textToSend){
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(textToSend);

            try{
                execute(message);
            }
            catch (TelegramApiException e){
                log.error("Error occurred: " + e.getMessage());
            }
        }

    }
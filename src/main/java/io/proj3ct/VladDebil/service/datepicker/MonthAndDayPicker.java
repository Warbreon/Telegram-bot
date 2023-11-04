package io.proj3ct.VladDebil.service.datepicker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MonthAndDayPicker {

    public static int getDaysInMonth(Month month) {
        return month.length(LocalDate.now().isLeapYear());
    }

    public SendMessage getAllKeyboardMonthButtonsAndMessage(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите месяц:");

        InlineKeyboardMarkup monthsInlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> monthsButtonsRows = new ArrayList<>();

        for (Month month : Month.values()) {
            List<InlineKeyboardButton> monthsCurrentRow = new ArrayList<>();

            InlineKeyboardButton monthButton = new InlineKeyboardButton();
            monthButton.setText(month.name());
            monthButton.setCallbackData("MONTH_" + month.name());
            monthsCurrentRow.add(monthButton);

            monthsButtonsRows.add(monthsCurrentRow);
        }

        monthsInlineKeyboard.setKeyboard(monthsButtonsRows);
        sendMessage.setReplyMarkup(monthsInlineKeyboard);

        log.info("getAllKeyboardMonthButtonsAndMessage method executed");
        return sendMessage;
    }

    public SendMessage getAllKeyboardDaysButtonsAndMessage(long chatId, Month month) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите день:");

        InlineKeyboardMarkup daysInlineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> daysButtonsRows = new ArrayList<>();
        List<InlineKeyboardButton> daysCurrentRow = new ArrayList<>();

        int buttonCount = 0;
        for (int day = 1; day <= getDaysInMonth(month); day++) {

            InlineKeyboardButton dayButton = new InlineKeyboardButton();
            dayButton.setText(String.valueOf(day));
            dayButton.setCallbackData("DAY_" + day);
            daysCurrentRow.add(dayButton);
            buttonCount++;

            if(buttonCount % 3 == 0){
                daysButtonsRows.add(daysCurrentRow);
                daysCurrentRow = new ArrayList<>();
            }
        }
        if(!daysCurrentRow.isEmpty()){
            daysButtonsRows.add(daysCurrentRow);
        }

        daysInlineKeyboard.setKeyboard(daysButtonsRows);
        sendMessage.setReplyMarkup(daysInlineKeyboard);

        log.info("getAllKeyboardDaysButtonsAndMessage method executed");
        return sendMessage;
    }
}

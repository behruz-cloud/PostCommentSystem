package uz.pdp.bot.entity;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uz.pdp.bot.entity.BotService.*;

public class BotController {
    ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                executorService.execute(() -> {
                    handleUpdate(update);
                });
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdate(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            TgUser tgUser = BotService.getOrCreateUser(message.chat().id());
            if (message.text() != null) {
                if (message.text().equals("/start")) {
                    tgUser.setName(message.chat().firstName());
                    System.out.println(tgUser);
                    BotService.sendWelcomeMessageAndShowUser(tgUser.getChat_id());
                } else if (tgUser.getState().equals(TgState.ADD_COMMENT)) {
                    BotService.addedComment(message, tgUser);
                }
            }
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            String data = callbackQuery.data();
            Long id = callbackQuery.from().id();
            TgUser tgUser = getOrCreateUser(id);
            if (tgUser.getState().equals(TgState.SHOW_POSTS)) {
                showUsersAndSendPosts(data, tgUser);
            } else if (tgUser.getState().equals(TgState.COMMENT)) {
                enteredComment(data, tgUser);
            } else if (tgUser.getState().equals(TgState.CONTINUE)) {
                continueProject(data, tgUser);
            } else if (data.equals("back")) {
                back(tgUser);
            }
        }
    }

}

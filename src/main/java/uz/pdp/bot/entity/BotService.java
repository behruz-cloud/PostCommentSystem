package uz.pdp.bot.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.SneakyThrows;
import uz.pdp.bot.DB;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BotService {
    public static TelegramBot telegramBot = new TelegramBot("7946385940:AAEsO0Hh_o0kkCyVC9J47QkKlp87VsxJIC0");

    public static TgUser getOrCreateUser(Long id) {
        return DB.TG_USERS.stream()
                .filter(tgUser -> tgUser.getChat_id().equals(id))
                .findFirst()
                .orElseGet(() -> {
                    TgUser newUser = new TgUser();
                    newUser.setChat_id(id);
                    DB.TG_USERS.add(newUser);
                    return newUser;
                });

    }


    @SneakyThrows
    public static void sendWelcomeMessageAndShowUser(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "        👤USERS👤");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String str = response.body();
        Gson gson = new Gson();
        List<User> users = gson.fromJson(str, new TypeToken<List<User>>() {
        }.getType());
        DB.USERS.addAll(users);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (User user : users) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(user.getName()).callbackData("user"),
                    new InlineKeyboardButton("📬posts📬").callbackData(user.getName()));
        }
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        TgUser tgUser = getOrCreateUser(chatId);
        tgUser.setState(TgState.SHOW_POSTS);
        telegramBot.execute(sendMessage);
    }

    @SneakyThrows
    public static void showUsersAndSendPosts(String data, TgUser tgUser) {
        if (data.equals("user")) {
        } else {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://jsonplaceholder.typicode.com/posts")).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String str = response.body();
            Gson gson = new Gson();
            List<Post> posts = gson.fromJson(str, new TypeToken<List<Post>>() {
            }.getType());
            Optional<User> userOptional = DB.getFakeUser(data);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                List<Post> posts1 = posts.stream().filter(post -> post.getUserId().equals(user.getId())).toList();
                SendMessage sendMessage = new SendMessage(tgUser.getChat_id(), "     " + user.getName().toUpperCase() + " posts");
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                for (Post post : posts1) {
                    inlineKeyboardMarkup.addRow(new InlineKeyboardButton(post.getTitle()).callbackData("title"),
                            new InlineKeyboardButton("💬comment💬").callbackData("comment:" + post.getId()));
                }
                sendMessage.replyMarkup(inlineKeyboardMarkup);
                tgUser.setState(TgState.COMMENT);
                telegramBot.execute(sendMessage);
            }
        }
    }

    public static void enteredComment(String data, TgUser tgUser) {
        if (data.equals("title")) {
        } else {
            String postId = data.split(":")[1];
            StringBuilder stringBuilder = new StringBuilder();
            List<Comment> comments = DB.selectedPostgetComment(postId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (Comment comment : comments) {
                LocalDateTime time = comment.getLocalDateTime();
                String format = time.format(formatter); /// mana shu yerda userni o'zgartirishim kerak!
                stringBuilder.append("👤User :").append(comment.getCommenterName()).append(" \n🕧 ").append(format).append("\n📝Comment : ").append(comment.getComment()).append("\n\n");
            }
            String str = stringBuilder.toString();
            SendMessage sendMessage = new SendMessage(tgUser.getChat_id(), "--------📝 COMMENTS 📝--------\n" + str);
            SendMessage sendMessage1 = new SendMessage(tgUser.getChat_id(),
                    "Leave your opinion in the comments 👇🏻"
            );
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton("🔙 BACK 🔙").callbackData("back"));
            sendMessage.replyMarkup(inlineKeyboardMarkup);
            tgUser.setState(TgState.ADD_COMMENT);
            tgUser.setCurrentPostId(postId);
            telegramBot.execute(sendMessage);
            telegramBot.execute(sendMessage1);
        }

    }

    public static void addedComment(Message message, TgUser tgUser) {
        String commentText = message.text();
        String postId = tgUser.getCurrentPostId();
        Comment comment1 = new Comment();
        comment1.setComment(commentText);
        comment1.setPostId(postId);
        comment1.setLocalDateTime(LocalDateTime.now());
        comment1.setCommenterName(tgUser.getName());
        DB.COMMENTS.add(comment1);
        SendMessage sendMessage = new SendMessage(tgUser.getChat_id(),
                "✅Your comment has been added✅\nComment : " + commentText);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton("▶️ continue ▶️").callbackData("continue"));
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        tgUser.setState(TgState.CONTINUE);
        telegramBot.execute(sendMessage);
    }

    @SneakyThrows
    public static void continueProject(String data, TgUser tgUser) {
        if (data.equals("continue")) {
            SendMessage sendMessage = new SendMessage(tgUser.getChat_id(), "        👤USERS👤");
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String str = response.body();
            Gson gson = new Gson();
            List<User> users = gson.fromJson(str, new TypeToken<List<User>>() {
            }.getType());
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            for (User user : users) {
                inlineKeyboardMarkup.addRow(new InlineKeyboardButton(user.getName()).callbackData("user"),
                        new InlineKeyboardButton("📬posts📬").callbackData(user.getName()));
            }
            sendMessage.replyMarkup(inlineKeyboardMarkup);
            tgUser.setState(TgState.SHOW_POSTS);
            telegramBot.execute(sendMessage);
        }
    }

    @SneakyThrows
    public static void back(TgUser tgUser) {
        SendMessage sendMessage = new SendMessage(tgUser.getChat_id(), "        👤USERS👤");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String str = response.body();
        Gson gson = new Gson();
        List<User> users = gson.fromJson(str, new TypeToken<List<User>>() {
        }.getType());
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (User user : users) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(user.getName()).callbackData("user"),
                    new InlineKeyboardButton("📬posts📬").callbackData(user.getName()));
        }
        sendMessage.replyMarkup(inlineKeyboardMarkup);
        tgUser.setState(TgState.SHOW_POSTS);
        telegramBot.execute(sendMessage);
    }
}

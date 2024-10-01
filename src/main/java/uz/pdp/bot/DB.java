package uz.pdp.bot;

import uz.pdp.bot.entity.Comment;
import uz.pdp.bot.entity.Post;
import uz.pdp.bot.entity.TgUser;
import uz.pdp.bot.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface DB {
    List<TgUser> TG_USERS = new ArrayList<>();
    List<User> USERS = new ArrayList<>();
    List<Post> POSTS = new ArrayList<>();
    List<Comment> COMMENTS = new ArrayList<>();

    static Optional<User> getFakeUser(String data) {
        return DB.USERS.stream().filter(user -> user.getName().equals(data)).findFirst();
    }

    static List<Comment> selectedPostgetComment(String postId) {
        return COMMENTS.stream().filter(comment -> comment.getPostId().equals(postId)).toList();
    }
}

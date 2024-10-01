package uz.pdp.bot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private String commenterName;
    private String postId;
    private String comment;
    private LocalDateTime localDateTime;
}

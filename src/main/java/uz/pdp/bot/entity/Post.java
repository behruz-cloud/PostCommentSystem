package uz.pdp.bot.entity;

import lombok.Data;

@Data
public class Post {
    private Integer userId;
    private String title;
    private String body;
    private String id;
}

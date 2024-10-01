package uz.pdp.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgUser {
    private String name;
    private Long chat_id;
    private TgState state;
    private String currentPostId;
}

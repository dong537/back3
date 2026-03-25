package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long fromUserId;
    
    private String type; // like/comment/follow/mention/system
    
    private String targetType; // post/comment
    
    private Long targetId;
    
    private String content;
    
    private Boolean isRead;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User fromUser;
    
    @TableField(exist = false)
    private Post post;
}

package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long postId;
    
    private Long userId;
    
    private Long parentId;
    
    private Long replyToUserId;
    
    private String content;
    
    private Integer likesCount;
    
    private Boolean isAnonymous;
    
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private User replyToUser;
    
    @TableField(exist = false)
    private List<Comment> replies;
    
    @TableField(exist = false)
    private Boolean liked;
}

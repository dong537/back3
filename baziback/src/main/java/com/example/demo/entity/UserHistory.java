package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tb_user_history")
public class UserHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String type; // yijing/tarot/bazi/zodiac
    
    private String title;
    
    private String content;
    
    private String data; // JSON字符串
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

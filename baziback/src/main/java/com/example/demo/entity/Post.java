package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tb_post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String content;
    
    private String title;
    
    private String category; // share/question/discuss/tree_hole
    
    private String images; // JSON字符串
    
    private String tags; // JSON字符串
    
    private Boolean isAnonymous;
    
    private Integer likesCount;
    
    private Integer commentsCount;
    
    private Integer sharesCount;
    
    private Integer viewsCount;
    
    private Boolean isTop;
    
    private Boolean isHot;
    
    private Integer status; // 0-删除 1-正常 2-审核中
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private User user;
    
    @TableField(exist = false)
    private Boolean liked;
    
    @TableField(exist = false)
    private Boolean saved;
    
    @TableField(exist = false)
    private List<String> tagList;
}

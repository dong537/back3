package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbUserFavorite {
    private Long id;
    private Long userId;
    private String favoriteType;
    private String dataId;
    private String title;
    private String summary;
    private String data;
    private LocalDateTime createTime;
}

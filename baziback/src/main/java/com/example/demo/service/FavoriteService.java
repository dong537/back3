package com.example.demo.service;

import com.example.demo.entity.TbUserFavorite;
import com.example.demo.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final AchievementService achievementService;

    public List<TbUserFavorite> getFavoritesByUserId(Long userId) {
        return favoriteMapper.findByUserId(userId);
    }

    public TbUserFavorite addFavorite(Long userId, TbUserFavorite favorite) {
        favorite.setUserId(userId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        try {
            achievementService.checkFavoriteAchievements(userId);
        } catch (Exception e) {
            log.error("检查收藏成就失败: userId={}", userId, e);
        }

        return favorite;
    }

    public boolean deleteFavorite(Long id, Long userId) {
        return favoriteMapper.deleteById(id, userId) > 0;
    }

    public int deleteFavorites(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return favoriteMapper.deleteByIds(ids, userId);
    }

    public int deleteAllFavorites(Long userId) {
        return favoriteMapper.deleteByUserId(userId);
    }
}

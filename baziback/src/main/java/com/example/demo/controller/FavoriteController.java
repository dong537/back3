package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.TbUserFavorite;
import com.example.demo.service.FavoriteService;
import com.example.demo.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final AuthUtil authUtil;

    @GetMapping
    public Result<List<TbUserFavorite>> getFavorites(@RequestHeader("Authorization") String token) {
        try {
            Long userId = authUtil.requireUserId(token);
            List<TbUserFavorite> favorites = favoriteService.getFavoritesByUserId(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result<TbUserFavorite> addFavorite(@RequestHeader("Authorization") String token,
                                              @RequestBody TbUserFavorite favorite) {
        try {
            Long userId = authUtil.requireUserId(token);
            TbUserFavorite newFavorite = favoriteService.addFavorite(userId, favorite);
            return Result.success(newFavorite);
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return Result.error("添加收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFavorite(@RequestHeader("Authorization") String token,
                                       @PathVariable Long id) {
        try {
            Long userId = authUtil.requireUserId(token);
            boolean success = favoriteService.deleteFavorite(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除失败或收藏不存在");
            }
        } catch (Exception e) {
            log.error("删除收藏失败", e);
            return Result.error("删除收藏失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete-batch")
    public Result<Map<String, Integer>> deleteFavorites(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String, List<Long>> payload) {
        try {
            Long userId = authUtil.requireUserId(token);
            List<Long> ids = payload.get("ids");
            int deletedCount = favoriteService.deleteFavorites(ids, userId);
            return Result.success(Map.of("deletedCount", deletedCount));
        } catch (Exception e) {
            log.error("批量删除收藏失败", e);
            return Result.error("批量删除收藏失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear-all")
    public Result<Map<String, Integer>> deleteAllFavorites(@RequestHeader("Authorization") String token) {
        try {
            Long userId = authUtil.requireUserId(token);
            int deletedCount = favoriteService.deleteAllFavorites(userId);
            return Result.success(Map.of("deletedCount", deletedCount));
        } catch (Exception e) {
            log.error("清空收藏失败", e);
            return Result.error("清空收藏失败: " + e.getMessage());
        }
    }
}

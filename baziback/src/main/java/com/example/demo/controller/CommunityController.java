package com.example.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.entity.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.CommunityService;
import com.example.demo.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final AuthUtil authUtil;

    // ==================== 帖子接口 ====================

    @GetMapping("/posts")
    public Result<?> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tab,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.tryGetUserId(token);

        IPage<Post> posts;
        if ("hot".equals(tab) || "热门".equals(tab)) {
            posts = communityService.getHotPosts(page, size, userId);
        } else {
            posts = communityService.getPosts(page, size, category, userId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", posts.getRecords());
        result.put("total", posts.getTotal());
        result.put("page", posts.getCurrent());
        result.put("pages", posts.getPages());

        return Result.success(result);
    }

    @GetMapping("/posts/{id}")
    public Result<?> getPostDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.tryGetUserId(token);
        Post post = communityService.getPostById(id, userId);

        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        return Result.success(post);
    }

    @PostMapping("/posts")
    public Result<?> createPost(
            @RequestBody CreatePostRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);

        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("内容不能为空");
        }

        if (request.getContent().length() < 10) {
            throw new BusinessException("内容至少10个字");
        }

        log.info("用户创建帖子: userId={}, category={}, title={}", userId, request.getCategory(), request.getTitle());
        Post post = communityService.createPost(
                userId,
                request.getContent(),
                request.getTitle(),
                request.getCategory(),
                request.getTags(),
                request.getImages(),
                request.isAnonymous()
        );
        log.info("帖子创建成功: userId={}, postId={}", userId, post.getId());

        return Result.success(post);
    }

    @DeleteMapping("/posts/{id}")
    public Result<?> deletePost(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);

        boolean success = communityService.deletePost(id, userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ==================== 点赞接口 ====================

    @PostMapping("/like")
    public Result<?> toggleLike(
            @RequestBody LikeRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);
        if (request == null || request.getTargetType() == null || request.getTargetId() == null) {
            throw new BusinessException("参数不能为空");
        }

        boolean liked = communityService.toggleLike(userId, request.getTargetType(), request.getTargetId());

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);

        return Result.success(result);
    }

    // ==================== 收藏接口 ====================

    @PostMapping("/favorite")
    public Result<?> toggleFavorite(
            @RequestBody FavoriteRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);
        if (request == null || request.getPostId() == null) {
            throw new BusinessException("postId不能为空");
        }

        boolean saved = communityService.toggleFavorite(userId, request.getPostId());

        Map<String, Object> result = new HashMap<>();
        result.put("saved", saved);

        return Result.success(result);
    }

    // ==================== 评论接口 ====================

    @GetMapping("/posts/{postId}/comments")
    public Result<?> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.tryGetUserId(token);
        IPage<Comment> comments = communityService.getComments(postId, page, size, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", comments.getRecords());
        result.put("total", comments.getTotal());

        return Result.success(result);
    }

    @PostMapping("/comments")
    public Result<?> createComment(
            @RequestBody CreateCommentRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);

        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        log.info("用户创建评论: userId={}, postId={}, parentId={}", userId, request.getPostId(), request.getParentId());
        Comment comment = communityService.createComment(
                userId,
                request.getPostId(),
                request.getContent(),
                request.getParentId(),
                request.getReplyToUserId(),
                request.isAnonymous()
        );
        log.info("评论创建成功: userId={}, commentId={}, postId={}", userId, comment.getId(), request.getPostId());

        return Result.success(comment);
    }

    // ==================== 话题接口 ====================

    @GetMapping("/topics")
    public Result<?> getTopics() {
        List<Topic> topics = communityService.getAllTopics();
        return Result.success(topics);
    }

    @GetMapping("/topics/hot")
    public Result<?> getHotTopics(@RequestParam(defaultValue = "8") int limit) {
        List<Topic> topics = communityService.getHotTopics(limit);
        return Result.success(topics);
    }

    // ==================== 通知接口 ====================

    @GetMapping("/notifications")
    public Result<?> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestHeader(value = "Authorization", required = false) String token) {

        Long userId = authUtil.requireUserId(token);

        IPage<Notification> notifications = communityService.getNotifications(userId, type, page, size);
        int unreadCount = communityService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", notifications.getRecords());
        result.put("total", notifications.getTotal());
        result.put("unreadCount", unreadCount);

        return Result.success(result);
    }

    @PostMapping("/notifications/read-all")
    public Result<?> markAllAsRead(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = authUtil.requireUserId(token);

        communityService.markAllNotificationsAsRead(userId);
        return Result.success("已全部标记为已读");
    }

    @PostMapping("/notifications/{id}/read")
    public Result<?> markAsRead(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {

        authUtil.requireUserId(token);

        communityService.markNotificationAsRead(id);
        return Result.success("已标记为已读");
    }

    // ==================== 请求DTO ====================

    @Data
    public static class CreatePostRequest {
        private String content;
        private String title;
        private String category;
        private List<String> tags;
        private List<String> images;
        private boolean anonymous;
    }

    @Data
    public static class LikeRequest {
        private String targetType; // post/comment
        private Long targetId;
    }

    @Data
    public static class FavoriteRequest {
        private Long postId;
    }

    @Data
    public static class CreateCommentRequest {
        private Long postId;
        private String content;
        private Long parentId;
        private Long replyToUserId;
        private boolean anonymous;
    }
}

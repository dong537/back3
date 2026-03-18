package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {
    
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final TopicMapper topicMapper;
    private final NotificationMapper notificationMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    
    // ==================== 帖子相关 ====================
    
    public IPage<Post> getPosts(int page, int size, String category, Long userId) {
        Page<Post> pageParam = new Page<>(page, size);
        List<Post> posts;
        long total;
        
        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            posts = postMapper.selectPostsByCategory(pageParam, category);
            total = postMapper.countPostsByCategory(category);
        } else {
            posts = postMapper.selectPostsWithUser(pageParam);
            total = postMapper.countAllPosts();
        }
        
        // 设置分页结果
        pageParam.setRecords(posts);
        pageParam.setTotal(total);
        
        // 填充用户交互状态
        if (userId != null) {
            List<Long> likedPostIds = likeMapper.selectLikedIds(userId, "post");
            List<Long> savedPostIds = favoriteMapper.selectFavoritePostIds(userId);
            
            pageParam.getRecords().forEach(post -> {
                post.setLiked(likedPostIds.contains(post.getId()));
                post.setSaved(savedPostIds.contains(post.getId()));
                parsePostTags(post);
                fillUserInfo(post);
            });
        } else {
            pageParam.getRecords().forEach(post -> {
                post.setLiked(false);
                post.setSaved(false);
                parsePostTags(post);
                fillUserInfo(post);
            });
        }
        
        return pageParam;
    }
    
    public IPage<Post> getHotPosts(int page, int size, Long userId) {
        Page<Post> pageParam = new Page<>(page, size);
        List<Post> posts = postMapper.selectHotPosts(pageParam);
        long total = postMapper.countHotPosts();
        
        pageParam.setRecords(posts);
        pageParam.setTotal(total);
        
        if (userId != null) {
            List<Long> likedPostIds = likeMapper.selectLikedIds(userId, "post");
            List<Long> savedPostIds = favoriteMapper.selectFavoritePostIds(userId);
            
            pageParam.getRecords().forEach(post -> {
                post.setLiked(likedPostIds.contains(post.getId()));
                post.setSaved(savedPostIds.contains(post.getId()));
                parsePostTags(post);
                fillUserInfo(post);
            });
        }
        
        return pageParam;
    }
    
    public Post getPostById(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) return null;
        
        // 增加浏览量
        postMapper.incrementViewsCount(postId);
        
        // 填充用户信息
        fillUserInfo(post);
        parsePostTags(post);
        
        if (userId != null) {
            post.setLiked(likeMapper.exists(userId, "post", postId) > 0);
            post.setSaved(favoriteMapper.exists(userId, postId) > 0);
        }
        
        return post;
    }
    
    @Transactional
    public Post createPost(Long userId, String content, String title, String category, 
                          List<String> tags, List<String> images, boolean isAnonymous) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setTitle(title);
        post.setCategory(category != null ? category : "share");
        post.setIsAnonymous(isAnonymous);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setSharesCount(0);
        post.setViewsCount(0);
        post.setIsTop(false);
        post.setIsHot(false);
        post.setStatus(1);
        
        try {
            if (tags != null && !tags.isEmpty()) {
                post.setTags(objectMapper.writeValueAsString(tags));
            }
            if (images != null && !images.isEmpty()) {
                post.setImages(objectMapper.writeValueAsString(images));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
        }
        
        postMapper.insert(post);
        
        // 更新话题帖子数
        if (tags != null) {
            for (String tag : tags) {
                Topic topic = topicMapper.selectByName(tag);
                if (topic != null) {
                    topicMapper.incrementPostsCount(topic.getId());
                }
            }
        }
        
        fillUserInfo(post);
        post.setTagList(tags);
        post.setLiked(false);
        post.setSaved(false);
        
        return post;
    }
    
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !post.getUserId().equals(userId)) {
            return false;
        }
        post.setStatus(0);
        return postMapper.updateById(post) > 0;
    }
    
    // ==================== 点赞相关 ====================
    
    @Transactional
    public boolean toggleLike(Long userId, String targetType, Long targetId) {
        boolean exists = likeMapper.exists(userId, targetType, targetId) > 0;
        
        if (exists) {
            likeMapper.delete(userId, targetType, targetId);
            if ("post".equals(targetType)) {
                postMapper.updateLikesCount(targetId, -1);
            } else if ("comment".equals(targetType)) {
                commentMapper.updateLikesCount(targetId, -1);
            }
            return false;
        } else {
            likeMapper.insert(userId, targetType, targetId);
            if ("post".equals(targetType)) {
                postMapper.updateLikesCount(targetId, 1);
                // 发送通知
                Post post = postMapper.selectById(targetId);
                if (post != null && !post.getUserId().equals(userId)) {
                    createNotification(post.getUserId(), userId, "like", "post", targetId, null);
                }
            } else if ("comment".equals(targetType)) {
                commentMapper.updateLikesCount(targetId, 1);
            }
            return true;
        }
    }
    
    // ==================== 收藏相关 ====================
    
    @Transactional
    public boolean toggleFavorite(Long userId, Long postId) {
        boolean exists = favoriteMapper.exists(userId, postId) > 0;
        
        if (exists) {
            favoriteMapper.delete(userId, postId);
            return false;
        } else {
            favoriteMapper.insert(userId, postId);
            return true;
        }
    }
    
    // ==================== 评论相关 ====================
    
    public IPage<Comment> getComments(Long postId, int page, int size, Long userId) {
        Page<Comment> pageParam = new Page<>(page, size);
        List<Comment> comments = commentMapper.selectCommentsByPostId(pageParam, postId);
        long total = commentMapper.countCommentsByPostId(postId);
        
        pageParam.setRecords(comments);
        pageParam.setTotal(total);
        
        List<Long> likedCommentIds = userId != null ? 
            likeMapper.selectLikedIds(userId, "comment") : Collections.emptyList();
        
        pageParam.getRecords().forEach(comment -> {
            fillCommentUserInfo(comment);
            comment.setLiked(likedCommentIds.contains(comment.getId()));
            // 加载回复
            List<Comment> replies = commentMapper.selectRepliesByParentId(comment.getId());
            replies.forEach(reply -> {
                fillCommentUserInfo(reply);
                reply.setLiked(likedCommentIds.contains(reply.getId()));
            });
            comment.setReplies(replies);
        });
        
        return pageParam;
    }
    
    @Transactional
    public Comment createComment(Long userId, Long postId, String content, 
                                 Long parentId, Long replyToUserId, boolean isAnonymous) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId != null ? parentId : 0L);
        comment.setReplyToUserId(replyToUserId);
        comment.setLikesCount(0);
        comment.setIsAnonymous(isAnonymous);
        comment.setStatus(1);
        
        commentMapper.insert(comment);
        postMapper.updateCommentsCount(postId, 1);
        
        // 发送通知
        Post post = postMapper.selectById(postId);
        if (post != null && !post.getUserId().equals(userId)) {
            createNotification(post.getUserId(), userId, "comment", "post", postId, content);
        }
        if (replyToUserId != null && !replyToUserId.equals(userId)) {
            createNotification(replyToUserId, userId, "comment", "comment", comment.getId(), content);
        }
        
        fillCommentUserInfo(comment);
        comment.setLiked(false);
        
        return comment;
    }
    
    // ==================== 话题相关 ====================
    
    public List<Topic> getHotTopics(int limit) {
        return topicMapper.selectHotTopics(limit);
    }
    
    public List<Topic> getAllTopics() {
        return topicMapper.selectAllTopics();
    }
    
    // ==================== 通知相关 ====================
    
    public IPage<Notification> getNotifications(Long userId, String type, int page, int size) {
        Page<Notification> pageParam = new Page<>(page, size);
        
        List<Notification> notifications;
        long total;
        if (type != null && !type.isEmpty() && !"all".equals(type)) {
            notifications = notificationMapper.selectByUserIdAndType(pageParam, userId, type);
            total = notificationMapper.countByUserIdAndType(userId, type);
        } else {
            notifications = notificationMapper.selectByUserId(pageParam, userId);
            total = notificationMapper.countByUserId(userId);
        }
        
        pageParam.setRecords(notifications);
        pageParam.setTotal(total);
        
        // 填充发送者信息（Mapper已经join了用户表，但需要手动设置fromUser对象）
        pageParam.getRecords().forEach(this::fillNotificationFromUser);
        
        return pageParam;
    }
    
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }
    
    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }
    
    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId);
    }
    
    private void createNotification(Long userId, Long fromUserId, String type, 
                                   String targetType, Long targetId, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setContent(content);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }
    
    // ==================== 辅助方法 ====================
    
    private void fillUserInfo(Post post) {
        if (post.getIsAnonymous() != null && post.getIsAnonymous()) {
            User anonymousUser = new User();
            anonymousUser.setNickname("匿名用户");
            anonymousUser.setAvatar("🎭");
            post.setUser(anonymousUser);
        } else {
            User user = userMapper.findById(post.getUserId());
            if (user != null) {
                user.setPassword(null);
                post.setUser(user);
            }
        }
    }
    
    private void fillCommentUserInfo(Comment comment) {
        if (comment.getIsAnonymous() != null && comment.getIsAnonymous()) {
            User anonymousUser = new User();
            anonymousUser.setNickname("匿名用户");
            anonymousUser.setAvatar("🎭");
            comment.setUser(anonymousUser);
        } else {
            User user = userMapper.findById(comment.getUserId());
            if (user != null) {
                user.setPassword(null);
                comment.setUser(user);
            }
        }
        
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMapper.findById(comment.getReplyToUserId());
            if (replyToUser != null) {
                replyToUser.setPassword(null);
                comment.setReplyToUser(replyToUser);
            }
        }
    }
    
    private void parsePostTags(Post post) {
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            try {
                List<String> tags = objectMapper.readValue(post.getTags(), new TypeReference<List<String>>() {});
                post.setTagList(tags);
            } catch (JsonProcessingException e) {
                post.setTagList(Collections.emptyList());
            }
        } else {
            post.setTagList(Collections.emptyList());
        }
    }
    
    private void fillNotificationFromUser(Notification notification) {
        if (notification.getFromUserId() != null) {
            User fromUser = userMapper.findById(notification.getFromUserId());
            if (fromUser != null) {
                fromUser.setPassword(null);
                notification.setFromUser(fromUser);
            }
        }
    }
}

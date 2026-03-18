import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Heart, MessageSquare, Share2, Bookmark, MoreHorizontal, Send, Loader2, Sparkles } from 'lucide-react';
import { communityApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { toast } from '../components/Toast';

export default function PostDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  
  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadPost = useCallback(async () => {
    try {
      setLoading(true);
      const res = await communityApi.getPostDetail(id);
      setPost(res.data?.data || res.data);
    } catch (err) {
      console.error('Load post failed:', err);
      toast.error('帖子不存在或已删除');
      navigate('/');
    } finally {
      setLoading(false);
    }
  }, [id, navigate]);

  const loadComments = useCallback(async () => {
    try {
      const res = await communityApi.getComments(id, 1, 50);
      const data = res.data?.data || res.data;
      setComments(data.list || []);
    } catch (err) {
      console.error('加载评论失败:', err);
    }
  }, [id]);

  useEffect(() => {
    loadPost();
    loadComments();
  }, [loadPost, loadComments]);

  const handleLike = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录');
      navigate('/login');
      return;
    }
    setPost(prev => ({
      ...prev,
      liked: !prev.liked,
      likesCount: prev.liked ? prev.likesCount - 1 : prev.likesCount + 1
    }));
    try {
      await communityApi.toggleLike('post', id);
    } catch (err) {
      setPost(prev => ({
        ...prev,
        liked: !prev.liked,
        likesCount: prev.liked ? prev.likesCount + 1 : prev.likesCount - 1
      }));
    }
  };

  const handleSave = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录');
      navigate('/login');
      return;
    }
    setPost(prev => ({ ...prev, saved: !prev.saved }));
    try {
      await communityApi.toggleFavorite(id);
    } catch (err) {
      setPost(prev => ({ ...prev, saved: !prev.saved }));
    }
  };

  const handleSubmitComment = async () => {
    if (!isLoggedIn) {
      toast.warning('请先登录');
      navigate('/login');
      return;
    }
    if (!commentText.trim()) {
      toast.warning('请输入评论内容');
      return;
    }
    setSubmitting(true);
    try {
      const res = await communityApi.createComment({
        postId: parseInt(id),
        content: commentText.trim(),
        anonymous: false
      });
      const newComment = res.data?.data || res.data;
      setComments(prev => [newComment, ...prev]);
      setCommentText('');
      setPost(prev => ({ ...prev, commentsCount: (prev.commentsCount || 0) + 1 }));
      toast.success('评论成功');
    } catch (err) {
      toast.error('评论失败');
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    return (date.getMonth() + 1) + '月' + date.getDate() + '日';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#1a1a2e] via-[#16213e] to-[#0f0f23] flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="relative">
            <div className="w-16 h-16 rounded-full bg-gradient-to-r from-purple-500 to-pink-500 animate-pulse" />
            <Sparkles className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-white animate-spin" size={24} />
          </div>
          <span className="text-purple-300 text-sm animate-pulse">加载中...</span>
        </div>
      </div>
    );
  }

  if (!post) return null;

  const user = post.user || {};

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#1a1a2e] via-[#16213e] to-[#0f0f23] pb-24">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-50 backdrop-blur-xl bg-[#1a1a2e]/80 border-b border-purple-500/20">
        <div className="px-4 py-3 flex items-center justify-between">
          <button 
            onClick={() => navigate(-1)} 
            className="p-2 hover:bg-purple-500/20 rounded-xl transition-all duration-300 group"
          >
            <ArrowLeft size={20} className="text-purple-300 group-hover:text-purple-200 transition-colors" />
          </button>
          <h1 className="text-lg font-bold bg-gradient-to-r from-purple-300 to-pink-300 bg-clip-text text-transparent">
            动态详情
          </h1>
          <button className="p-2 hover:bg-purple-500/20 rounded-xl transition-all duration-300 group">
            <MoreHorizontal size={20} className="text-purple-300 group-hover:text-purple-200 transition-colors" />
          </button>
        </div>
      </div>

      {/* 帖子内容卡片 */}
      <div className="m-4 relative group">
        {/* 卡片光晕效果 */}
        <div className="absolute -inset-1 bg-gradient-to-r from-purple-600/30 via-pink-500/30 to-purple-600/30 rounded-2xl blur-lg opacity-60 group-hover:opacity-80 transition-opacity duration-500" />
        
        <div className="relative bg-gradient-to-br from-[#252547]/90 to-[#1a1a35]/90 backdrop-blur-xl rounded-2xl border border-purple-500/30 overflow-hidden">
          {/* 顶部装饰线 */}
          <div className="h-1 bg-gradient-to-r from-purple-500 via-pink-500 to-purple-500" />
          
          <div className="p-5">
            {/* 用户信息 */}
            <div className="flex items-center space-x-3 mb-5">
              <div className="relative">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-purple-500 to-pink-500 p-0.5">
                  <div className="w-full h-full rounded-full bg-[#1a1a2e] flex items-center justify-center text-xl">
                    {user.avatar || '👤'}
                  </div>
                </div>
                <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-[#1a1a2e]" />
              </div>
              <div className="flex-1">
                <span className="font-semibold text-purple-100">{user.nickname || '用户'}</span>
                <div className="text-xs text-purple-400/70">{formatTime(post.createdAt)}</div>
              </div>
            </div>

            {/* 帖子标题和内容 */}
            {post.title && (
              <h2 className="text-xl font-bold text-white mb-3 leading-tight">{post.title}</h2>
            )}
            <p className="text-purple-200/90 leading-relaxed whitespace-pre-line mb-5 text-[15px]">
              {post.content}
            </p>

            {/* 互动按钮 */}
            <div className="flex items-center justify-between pt-4 border-t border-purple-500/20">
              <button 
                onClick={handleLike} 
                className={`flex items-center space-x-2 px-4 py-2 rounded-xl transition-all duration-300 ${
                  post.liked 
                    ? 'text-pink-400 bg-pink-500/20 shadow-lg shadow-pink-500/20' 
                    : 'text-purple-400 hover:bg-purple-500/20'
                }`}
              >
                <Heart 
                  size={20} 
                  fill={post.liked ? 'currentColor' : 'none'} 
                  className={post.liked ? 'animate-pulse' : ''} 
                />
                <span className="text-sm font-medium">{post.likesCount || 0}</span>
              </button>
              
              <div className="flex items-center space-x-2 px-4 py-2 text-purple-400">
                <MessageSquare size={20} />
                <span className="text-sm font-medium">{post.commentsCount || 0}</span>
              </div>
              
              <button className="flex items-center space-x-2 px-4 py-2 text-purple-400 hover:bg-purple-500/20 rounded-xl transition-all duration-300">
                <Share2 size={20} />
              </button>
              
              <button 
                onClick={handleSave} 
                className={`flex items-center space-x-2 px-4 py-2 rounded-xl transition-all duration-300 ${
                  post.saved 
                    ? 'text-yellow-400 bg-yellow-500/20 shadow-lg shadow-yellow-500/20' 
                    : 'text-purple-400 hover:bg-purple-500/20'
                }`}
              >
                <Bookmark 
                  size={20} 
                  fill={post.saved ? 'currentColor' : 'none'} 
                  className={post.saved ? 'animate-pulse' : ''} 
                />
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 评论区 */}
      <div className="px-4">
        <div className="flex items-center space-x-2 mb-4">
          <Sparkles size={18} className="text-purple-400" />
          <h3 className="font-bold text-purple-100">评论</h3>
          <span className="text-sm text-purple-400/70">({comments.length})</span>
        </div>
        
        {comments.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-purple-500/20 flex items-center justify-center">
              <MessageSquare size={28} className="text-purple-400" />
            </div>
            <p className="text-purple-400/70">暂无评论，快来抢沙发吧~</p>
          </div>
        ) : (
          <div className="space-y-3">
            {comments.map((comment, index) => {
              const commentUser = comment.user || {};
              return (
                <div 
                  key={comment.id} 
                  className="relative group"
                  style={{ animationDelay: `${index * 50}ms` }}
                >
                  <div className="absolute -inset-0.5 bg-gradient-to-r from-purple-600/20 to-pink-500/20 rounded-xl blur opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                  
                  <div className="relative bg-[#252547]/60 backdrop-blur-sm rounded-xl p-4 border border-purple-500/20 hover:border-purple-500/40 transition-all duration-300">
                    <div className="flex items-start space-x-3">
                      <div className="w-9 h-9 rounded-full bg-gradient-to-br from-purple-500/50 to-pink-500/50 p-0.5 flex-shrink-0">
                        <div className="w-full h-full rounded-full bg-[#1a1a2e] flex items-center justify-center text-sm">
                          {commentUser.avatar || '👤'}
                        </div>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1.5">
                          <span className="text-sm font-semibold text-purple-200">{commentUser.nickname || '用户'}</span>
                          <span className="text-xs text-purple-400/60">{formatTime(comment.createdAt)}</span>
                        </div>
                        <p className="text-sm text-purple-300/90 leading-relaxed">{comment.content}</p>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* 底部评论输入框 */}
      <div className="fixed bottom-0 left-0 right-0 backdrop-blur-xl bg-[#1a1a2e]/90 border-t border-purple-500/20 p-4">
        <div className="flex items-center space-x-3">
          <div className="flex-1 relative">
            <input
              type="text"
              value={commentText}
              onChange={(e) => setCommentText(e.target.value)}
              placeholder="写下你的评论..."
              className="w-full px-5 py-3 bg-[#252547]/80 rounded-2xl text-sm text-purple-100 placeholder-purple-400/50 border border-purple-500/30 focus:outline-none focus:border-purple-500/60 focus:ring-2 focus:ring-purple-500/20 transition-all duration-300"
              onKeyPress={(e) => e.key === 'Enter' && !submitting && handleSubmitComment()}
            />
          </div>
          <button
            onClick={handleSubmitComment}
            disabled={submitting || !commentText.trim()}
            className={`p-3 rounded-2xl transition-all duration-300 ${
              commentText.trim() 
                ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white shadow-lg shadow-purple-500/30 hover:shadow-purple-500/50 hover:scale-105 active:scale-95' 
                : 'bg-[#252547] text-purple-400/50 border border-purple-500/20'
            }`}
          >
            {submitting ? (
              <Loader2 size={20} className="animate-spin" />
            ) : (
              <Send size={20} />
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

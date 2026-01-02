class SquareManager {
    constructor(currentUserId, currentUsername, currentDisplayName, currentAvatarUrl) {
        this.currentUserId = currentUserId;
        this.currentUsername = currentUsername;
        this.currentDisplayName = currentDisplayName;
        this.currentAvatarUrl = currentAvatarUrl;
        this.page = 1;
        this.pageSize = 5;
        this.total = 0;
    }

    init() {
        this.bindEvents();
        this.fetchPosts();
    }

    bindEvents() {
        const $postsContainer = $('#posts-container');
        $('#postForm').on('submit', (e) => {
            e.preventDefault();
            this.createPost();
        });

        $postsContainer.on('click', '.comment-btn, .like-btn, .delete-post-btn', (e) => {
            const $postItem = $(e.target).closest('.post-item');
            const postId = $postItem.data('post-id');
            if ($(e.target).hasClass('comment-btn')) {
                this.toggleComments(postId);
            } else if ($(e.target).hasClass('like-btn')) {
                this.toggleLike(postId);
            } else if ($(e.target).hasClass('delete-post-btn')) {
                deletePost(postId);
            }
        });

        $postsContainer.on('submit', '.comment-form', (e) => {
            e.preventDefault();
            const $postItem = $(e.target).closest('.post-item');
            const postId = $postItem.data('post-id');
            this.addComment(postId, e.target);
        });

        $('#square-pagination').on('click', '.page-btn', (e) => {
            const p = parseInt($(e.target).data('page'), 10);
            if (!isNaN(p)) {
                this.page = p;
                this.fetchPosts();
            }
        });

        $('#postImage').on('change', function() {
            const file = this.files && this.files[0];
            if (file) {
                const url = URL.createObjectURL(file);
                $('#image-preview').html('<img src="' + url + '" alt="预览" />').show();
            } else {
                $('#image-preview').hide().empty();
            }
        });

        $('#image-preview').on('click', 'img', function() {
            $('#postImage').val('');
            $('#image-preview').hide().empty();
        });

        $postsContainer.on('click', '.comment-delete-btn', (e) => {
            const $comment = $(e.target).closest('.comment-item');
            const commentId = $comment.data('comment-id');
            const postId = $(e.target).closest('.post-item').data('post-id');
            if (!commentId || !postId) return;
            if (!confirm('确定删除这条评论吗？')) return;
            $.ajax({
                url: 'square/posts/' + postId + '/comments/' + commentId,
                method: 'DELETE'
            }).done((data) => {
                $comment.remove();
                if (data && typeof data.postCommentsCount !== 'undefined') {
                    const $commentCount = $('[data-post-id="' + postId + '"] .comment-count');
                    $commentCount.html(this.renderCommentHtml(data.postCommentsCount));
                }
            }).fail((xhr) => {
                const error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '删除失败';
                alert(error);
            });
        });
    }

    fetchPosts() {
        $.get('/square/posts', { page: this.page, pageSize: this.pageSize })
            .done((data) => {
                this.total = data.total || 0;
                // preload friendship statuses for authors
                const posts = data.posts || [];
                const authorIds = Array.from(new Set((posts || []).map(p => p.authorId).filter(id => id && id !== this.currentUserId)));
                const statusMap = {};
                const statusPromises = authorIds.map(id => this.getFriendshipStatus(id).then(s => { statusMap[id] = s; }).catch(() => {}));
                Promise.all(statusPromises).finally(() => {
                    this.renderPosts(posts, statusMap);
                    this.renderPagination();
                });
            })
            .fail(() => alert('加载帖子失败'));
    }

    renderPagination() {
        const totalPages = Math.max(1, Math.ceil((this.total || 0) / this.pageSize));
        let html = '';
        for (let i = 1; i <= totalPages; i++) {
            html += '<button class="page-btn ' + (i === this.page ? 'active' : '') + '" data-page="' + i + '">' + i + '</button>';
        }
        $('#square-pagination').html(html);
    }

    buildAvatar(url, userId) {
        const safe = url ? url : 'assets/images/avatars/default.jpg';
        const img = '<img class="avatar-small" src="' + safe + '" onerror="this.src=\'assets/images/avatars/default.jpg\'" />';
        if (userId) {
            return '<a class="avatar-link" href="profile.jsp?userId=' + userId + '">' + img + '</a>';
        }
        return img;
    }

    renderLikeHtml(count) {
        return '<img src="assets/images/postImages/like.png" alt="like" class="post-icon" onerror="this.src=\'assets/images/avatars/default.jpg\'" /> ' + count;
    }

    renderCommentHtml(count) {
        return '<img src="assets/images/postImages/comment.png" alt="comment" class="post-icon" onerror="this.src=\'assets/images/avatars/default.jpg\'" /> ' + count;
    }

    formatTime(val) {
        if (!val) return new Date().toLocaleString();
        const d = new Date(val);
        if (isNaN(d.getTime())) {
            return new Date().toLocaleString();
        }
        return d.toLocaleString();
    }

    formatAuthor(username, fallbackId) {
        // Only show @username; fallback to 用户{id}
        if (username) return '@' + username;
        return fallbackId ? ('用户' + fallbackId) : '未知用户';
    }

    renderPosts(posts, statusMap = {}) {
        const $container = $('#posts-container');
        $container.empty();
        posts.forEach((post) => {
            const authorUsername = post.authorUsername || post.authorName;
            const authorLabel = this.formatAuthor(authorUsername, post.authorId);
            let postHTML = '';
            postHTML += '<div class="post-item" data-post-id="' + post.id + '">';
            postHTML +=   '<div class="post-header">';
            postHTML +=     '<div class="post-author">' + this.buildAvatar(post.authorAvatarUrl, post.authorId) +
                              '<span class="author-name" data-author-id="' + post.authorId + '">' + authorLabel + '</span>' +
                             '<span class="post-time">' + new Date(post.createdAt).toLocaleString() + '</span>' +
                             '<span class="friend-status" data-author-id="' + post.authorId + '"></span>' +
                             (post.authorId !== this.currentUserId && !(statusMap[post.authorId] && statusMap[post.authorId].isFriend)
                               ? '<button class="add-friend-inline" data-author-id="' + post.authorId + '" data-author-name="' + authorLabel + '">认识一下</button>'
                               : '') +
                             '</div>';
            if (this.currentUserId === post.authorId) {
                postHTML += '<div class="post-actions"><button class="delete-post-btn">删除</button></div>';
            }
            postHTML +=   '</div>';
            postHTML +=   '<div class="post-content">' + post.content + '</div>';
            if (post.imageUrl) {
                postHTML += '<div class="post-content"><img class="post-image" src="' + post.imageUrl + '" alt="附图" onerror="this.style.display=\'none\'" /></div>';
            }
            postHTML +=   '<div class="post-footer">' +
                            '<div class="post-stats">' +
                            '<span class="like-count">' + this.renderLikeHtml(post.likesCount) + '</span>' +
                            '<span class="comment-count">' + this.renderCommentHtml(post.commentsCount) + '</span>' +
                            '</div>' +
                            '<div class="post-interactions">' +
                                '<button class="like-btn">点赞</button>' +
                                '<button class="comment-btn">评论</button>' +
                            '</div>' +
                          '</div>';
            postHTML +=   '<div class="comments-section" id="comments-' + post.id + '">' +
                            '<div class="comments-list"></div>' +
                            '<form class="comment-form">' +
                                '<textarea class="comment-input" placeholder="写下你的评论..." required></textarea>' +
                                '<button type="submit" class="send-comment-btn">发送</button>' +
                            '</form>' +
                          '</div>';
            postHTML += '</div>';
            $container.append(postHTML);
            this.loadComments(post.id);
        });
        this.bindInlineFriendButtons();
        this.initFriendshipStatus();
    }

    bindInlineFriendButtons() {
        const self = this;
        $('#posts-container .add-friend-inline').off('click').on('click', function() {
            const uid = $(this).data('author-id');
            const name = $(this).data('author-name');
            self.addFriend(uid, name, this);
        });
    }

    createPost() {
        const content = $('#postInput').val().trim();
        if (!content) return;
        const formData = new FormData();
        formData.append('content', content);
        formData.append('contentType', 'text');
        const file = $('#postImage')[0].files[0];
        if (file) formData.append('image', file);

        $.ajax({
            url: 'square/posts',
            method: 'POST',
            processData: false,
            contentType: false,
            data: formData
        }).done(() => {
            $('#postInput').val('');
            $('#postImage').val('');
            $('#image-preview').hide().empty();
            this.page = 1;
            this.fetchPosts();
        }).fail((xhr) => {
            const error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '发布失败';
            alert(error);
        });
    }

    toggleLike(postId) {
        $.post('square/posts/' + postId + '/like')
            .done((result) => {
                const $likeCount = $('[data-post-id="' + postId + '"] .like-count');
                $likeCount.html(this.renderLikeHtml(result.likesCount));
            })
            .fail((xhr) => {
                const error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '操作失败';
                alert(error);
            });
    }

    toggleComments(postId) {
        const $commentsSection = $('#comments-' + postId);
        const $postItem = $('[data-post-id="' + postId + '"]');
        const $commentBtn = $postItem.find('.comment-btn').first();
        if ($commentsSection.length === 0 || $commentBtn.length === 0) return;
        $commentsSection.toggleClass('show-comments');
        if ($commentsSection.hasClass('show-comments')) {
            $commentBtn.text('收起');
            this.loadComments(postId);
        } else {
            $commentBtn.text('评论');
        }
    }

    loadComments(postId) {
        $.get('square/posts/' + postId + '/comments')
            .done((comments) => {
                const $list = $('[data-post-id="' + postId + '"] .comments-list');
                $list.empty();
                comments.forEach((comment) => this.addCommentToDOM(postId, comment));
            })
            .fail(() => console.error('加载评论失败'));
    }

    addComment(postId, form) {
        const $form = $(form);
        const content = $form.find('.comment-input').val().trim();
        if (!content) return;
        $.ajax({
            url: 'square/posts/' + postId + '/comment',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ content: content })

        }).done((data) => {
            const commentResp = data.comment || {};
            if (!commentResp.authorUsername && commentResp.authorName) {
                commentResp.authorUsername = commentResp.authorName;
            }
            this.addCommentToDOM(postId, commentResp.id ? commentResp : {
                id: Date.now(),
                authorId: this.currentUserId,
                authorUsername: this.currentUsername,
                authorAvatarUrl: this.currentAvatarUrl || 'assets/images/avatars/default.jpg',
                createdAt: new Date().toISOString(),
                content: content
            });
            $form.find('.comment-input').val('');
            const $commentCount = $('[data-post-id="' + postId + '"] .comment-count');
            $commentCount.html(this.renderCommentHtml(data.postCommentsCount));
        }).fail((xhr) => {
            const error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '评论失败';
            alert(error);
        });
    }

    addCommentToDOM(postId, comment) {
        const username = comment.authorUsername || comment.authorName || comment.username;
        const authorName = this.formatAuthor(username, comment.authorId);
        const avatar = this.buildAvatar(comment.authorAvatarUrl, comment.authorId);
        const isMine = comment.authorId === this.currentUserId;
        let html = '';
        html += '<div class="comment-item" data-comment-id="' + comment.id + '" data-author-id="' + comment.authorId + '">';
        html +=   '<div class="comment-header">';
        html +=     avatar;
        html +=     '<span class="comment-author">' + authorName + '</span>';
        html +=     '<span class="comment-time">' + this.formatTime(comment.createdAt) + '</span>';
        if (isMine) {
            html +=     '<button class="comment-delete-btn" title="删除">删除</button>';
        }
        html +=   '</div>';
        html +=   '<div class="comment-content">' + comment.content + '</div>';
        html += '</div>';
        $('[data-post-id="' + postId + '"] .comments-list').append(html);
    }

    initFriendshipStatus() {
        const authorIds = new Set();
        $('.author-name[data-author-id]').each(function() {
            const id = parseInt($(this).data('author-id'), 10);
            if (id && id !== window.squareManager.currentUserId) {
                authorIds.add(id);
            }
        });
        const promises = [];
        authorIds.forEach((id) => promises.push(this.updateFriendshipStatus(id)));
        return Promise.all(promises);
    }

    getFriendshipStatus(authorId) {
        return $.ajax({
            url: '/friends/status',
            method: 'GET',
            data: { targetUserId: authorId },
            dataType: 'json'
        }).fail(() => ({ isFriend: false, isBlocked: false, isBlockedByTarget: false }));
    }

    async updateFriendshipStatus(authorId) {
        try {
            const status = await this.getFriendshipStatus(authorId);
            $('.friend-status[data-author-id="' + authorId + '"]').each(function() {
                if (status.isFriend) {
                    $(this).text('已互为好友').attr('class', 'friend-status friend-status-friend');
                } else {
                    $(this).text('').attr('class', 'friend-status');
                }
            });
        } catch (e) {
            // ignore
        }
    }

    addFriend(userId, username, btnEl) {
        const $btn = $(btnEl);
        if (!confirm('确定要与 ' + username + ' 互相认识吗？')) return;
        $btn.prop('disabled', true).text('请求中');
        $.ajax({
            url: '/friends/add',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ targetUserId: userId })
        }).done((res) => {
            if (res.success) {
                $btn.text('已认识');
                window.location.reload();
            } else {
                alert(res.message || '添加好友失败');
                $btn.prop('disabled', false).text('认识一下');
            }
        }).fail(() => {
            alert('添加好友失败');
            $btn.prop('disabled', false).text('认识一下');
        });
    }
}

async function deletePost(postId) {
    if (!confirm('确定要删除这篇帖子吗？')) return;
    $.ajax({ url: 'square/posts/' + postId, method: 'DELETE' })
        .done(() => window.squareManager.fetchPosts())
        .fail((xhr) => {
            const error = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : '删除失败';
            alert(error);
        });
}

async function toggleLike(postId) { if (window.squareManager) window.squareManager.toggleLike(postId); }
async function toggleComments(postId) { if (window.squareManager) window.squareManager.toggleComments(postId); }
async function addComment(postId, form) { if (window.squareManager) window.squareManager.addComment(postId, form); }
async function toggleFriendship(authorId, buttonElement) { if (window.squareManager) window.squareManager.toggleFriendship(authorId, buttonElement); }

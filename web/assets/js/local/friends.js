class FriendsManager {
    constructor() {
        this.$friendsList = $('#friends-list');
        this.$search = $('#friend-search');
        this.$searchBtn = $('#search-friends-btn');
        this.$createGroup = $('#create-group-btn');
    }

    init() {
        this.bindEvents();
        this.loadFriendsList();
    }

    bindEvents() {
        this.$searchBtn.on('click', () => this.filterFriendsList(this.$search.val()));
        if (this.$createGroup.length) {
            this.$createGroup.on('click', () => { window.location.href = '?page=create-group'; });
        }
    }

    loadFriendsList() {
        this.$friendsList.html('<div class="loading-indicator">加载中...</div>');
        $.get('/friends/list')
            .done((data) => {
                if (data && data.friends) {
                    this.renderFriendsList(data.friends);
                } else {
                    this.$friendsList.html('<div class="empty-state">暂无认识的人</div>');
                }
            })
            .fail(() => {
                this.$friendsList.html('<div class="error-state">加载失败，请重试</div>');
            });
    }

    renderFriendsList(friends) {
        if (!friends || friends.length === 0) {
            this.$friendsList.html('<div class="empty-state">暂无认识的人</div>');
            return;
        }
        const html = friends.map((friend) => {
            const avatarUrl = friend.avatarUrl || 'assets/images/avatars/default.jpg';
            const displayName = friend.displayName ? '<span class="friend-display-name">(' + friend.displayName + ')</span>' : '';
            return '' +
                '<div class="friend-item" data-friend-id="' + friend.id + '">' +
                    '<div class="friend-info">' +
                        '<div class="friend-avatar">' +
                            '<img src="' + avatarUrl + '" alt="头像" onerror="this.src=\'assets/images/avatars/default.jpg\'" />' +
                        '</div>' +
                        '<div class="friend-details">' +
                            '<div class="friend-name">' +
                                '<strong>' + friend.username + '</strong>' + displayName +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                    '<div class="friend-actions">' +
                        '<button class="send-message-btn" data-id="' + friend.id + '" data-name="' + friend.username + '">浅聊</button>' +
                        '<button class="remove-friend-btn" data-id="' + friend.id + '" data-name="' + friend.username + '">解除关系</button>' +
                    '</div>' +
                '</div>';
        }).join('');
        this.$friendsList.html(html);
        this.bindListButtons();
    }

    bindListButtons() {
        const self = this;
        this.$friendsList.find('.send-message-btn').off('click').on('click', function() {
            const id = $(this).data('id');
            const name = $(this).data('name');
            self.sendMessageToFriend(id, name, this);
        });
        this.$friendsList.find('.remove-friend-btn').off('click').on('click', function() {
            const id = $(this).data('id');
            const name = $(this).data('name');
            self.removeFriend(id, name);
        });
    }

    filterFriendsList(searchTerm) {
        const term = (searchTerm || '').toLowerCase();
        if (!term) {
            this.$friendsList.find('.friend-item').css('display', 'flex');
            return;
        }
        this.$friendsList.find('.friend-item').each(function() {
            const name = $(this).find('.friend-name').text().toLowerCase();
            $(this).css('display', name.indexOf(term) >= 0 ? 'flex' : 'none');
        });
    }

    sendMessageToFriend(friendId, friendName, btnEl) {
        if (typeof chatManager === 'undefined') {
            alert('聊天管理器未初始化，请刷新页面重试');
            return;
        }
        const $btn = $(btnEl);
        $btn.prop('disabled', true).text('加载中...');
        chatManager.createOrUpdateSession(friendId, friendName, null)
            .then(function(session) {
                var url = new URL(window.location.href);
                url.searchParams.set('page', 'private-chat');
                url.searchParams.set('userId', friendId);
                url.searchParams.set('username', friendName);
                url.searchParams.set('sessionId', session.id);
                window.location.href = url.toString();
            })
            .catch(function() {
                alert('创建私聊会话失败，请重试');
            })
            .finally(function() {
                $btn.prop('disabled', false).text('发消息');
            });
    }

    removeFriend(friendId, friendName) {
        if (!confirm('确定要解除对 ' + friendName + ' 的关系吗？')) return;
        $.ajax({
            url: '/friends/remove',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ targetUserId: friendId })
        }).done((data) => {
            if (data.success) {
                alert('关系已解除');
                this.loadFriendsList();
            } else {
                alert('解除失败: ' + (data.message || '未知错误'));
            }
        }).fail(() => alert('解除失败，请重试'));
    }

    formatTime(timestamp) {
        if (!timestamp) return '未知';
        const date = new Date(timestamp);
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
        if (diffDays === 0) {
            return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else if (diffDays === 1) {
            return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else if (diffDays < 7) {
            return diffDays + '天前';
        }
        return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
    }
}

$(function() {
    window.friendsManager = new FriendsManager();
    window.friendsManager.init();
});
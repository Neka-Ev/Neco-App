class CreateGroupManager {
    constructor() {
        this.selectedFriends = new Set();
    }

    init() {
        this.loadFriendsList();
        this.bindEvents();
    }

    bindEvents() {
        const self = this;
        $('#cancel-btn').on('click', function() {
            window.location.href = '?page=friends';
        });

        $('#create-group-submit').on('click', function() {
            self.createGroup();
        });

        $('#group-name').on('keypress', function(e) {
            if (e.key === 'Enter') {
                self.createGroup();
            }
        });
    }

    loadFriendsList() {
        const $container = $('#friends-select-container');
        $container.html('<div class="loading-indicator">加载好友列表中...</div>');
        $.get('/friends/list')
            .done((data) => {
                if (data && data.friends) {
                    this.renderFriendsList(data.friends);
                } else {
                    $container.html('<div class="empty-state">暂无好友</div>');
                }
            })
            .fail(() => {
                $container.html('<div class="error-state">加载好友列表失败，请重试</div>');
            });
    }

    renderFriendsList(friends) {
        const $container = $('#friends-select-container');
        if (!friends || friends.length === 0) {
            $container.html('<div class="empty-state">暂无好友</div>');
            return;
        }

        let html = '';
        friends.forEach((friend) => {
            const avatar = friend.avatarUrl ? friend.avatarUrl : 'assets/images/avatars/default.jpg';
            html += '<div class="friend-select-item" data-friend-id="' + friend.id + '">';
            html +=   '<input type="checkbox" id="friend-' + friend.id + '" class="friend-checkbox">';
            html +=   '<label for="friend-' + friend.id + '" class="friend-label">';
            html +=     '<div class="friend-avatar">';
            html +=       '<img class="avatar-small" src="' + avatar + '" onerror="this.src=\'assets/images/avatars/default.jpg\'" />';
            html +=     '</div>';
            html +=     '<div class="friend-name">';
            html +=       '<strong>' + (friend.username || '') + '</strong>';
            if (friend.displayName) {
                html += '<span class="friend-display-name">(' + friend.displayName + ')</span>';
            }
            html +=     '</div>';
            html +=   '</label>';
            html += '</div>';
        });

        $container.html(html);

        const self = this;
        $('.friend-checkbox').off('change').on('change', function() {
            const fid = parseInt($(this).attr('id').replace('friend-', ''), 10);
            if (this.checked) {
                self.selectedFriends.add(fid);
            } else {
                self.selectedFriends.delete(fid);
            }
        });
    }

    createGroup() {
        const groupName = $('#group-name').val().trim();
        if (!groupName) {
            alert('请输入群聊名称');
            return;
        }
        if (this.selectedFriends.size === 0) {
            alert('请至少选择一位好友');
            return;
        }

        const $submitBtn = $('#create-group-submit');
        $submitBtn.prop('disabled', true).text('创建中...');

        const payload = {
            groupName: groupName,
            memberIds: Array.from(this.selectedFriends)
        };

        $.ajax({
            url: '/api/groups/create',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload)
        }).done((result) => {
            if (result && result.success) {
                alert('群聊创建成功');
                window.location.href = '?page=group-chat&groupId=' + result.groupId + '&groupName=' + encodeURIComponent(groupName);
            } else {
                const msg = (result && result.message) ? result.message : '创建失败';
                alert(msg);
            }
        }).fail((xhr) => {
            const msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : '创建群聊失败';
            alert(msg);
        }).always(() => {
            $submitBtn.prop('disabled', false).text('创建群聊');
        });
    }
}


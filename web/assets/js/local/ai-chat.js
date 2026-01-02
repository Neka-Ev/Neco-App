(function($) {
    let currentSessionId = null;
    const welcomeMessages = [
        '我是 ATRI Neco，有什么可以帮你的？',
        '你好，我是 Neco，今天想聊点什么？',
        '嘿，Neco 在这里陪你聊天。',
        '需要写代码、查Bug，还是随便聊聊？Neco 都可以帮忙。',
        '你好，这里是你的 AI 助手 Neco。',
        '有任何问题，都可以试着问问 Neco。',
        '欢迎回来，继续和 Neco 的对话吧。',
        '你好呀，我是 Neco，随时为你待命。',
        '嗨，我是 Neco，有什么我可以帮忙的吗？',
        '你好，我是 Neco，今天过得怎么样？',
        '我是 Neco，你的智能伙伴，请多指教。',
        '需要 Neco 帮忙处理工作，还是想放松聊聊天？',
        '你好，Neco 很高兴见到你。',
        '随时都可以找 Neco，我一直在线。',
        '新的一天，Neco 有什么可以协助你的吗？',
        '嘿，Neco 准备好了，尽管问我吧。',
        '欢迎来到 Neco 的空间，希望你能感到轻松。',
        '有什么新想法或任务要和 Neco 分享吗？',
        '你好，Neco 在这里等着为你提供帮助。',
        '今天想和 Neco 探索什么有趣的话题？',
        '我是 Neco，随时准备为你解决问题。',
        'Neco 在此，有什么需要尽管开口。',
        '你好，我是 Neco，今天有什么计划？',
        'Neco 报到！需要什么帮助吗？',
        '嘿，我是 Neco，让我们开始愉快的交流吧。'
    ];

    const md = window.markdownit ? window.markdownit({
        html: false,
        linkify: true,
        breaks: true
    }) : null;

    function updateSessionSelectSelection() {
        const $select = $('#ai-session-select');
        if (!$select.length) return;
        if (currentSessionId) {
            $select.val(String(currentSessionId));
        } else {
            $select.val('');
        }
    }

    function renderWelcome() {
        const $box = $('#ai-messages');
        if ($box.length === 0) return;
        $box.empty();
        const text = welcomeMessages[Math.floor(Math.random() * welcomeMessages.length)];
        const $div = $('<div/>')
            .addClass('message')
            .addClass('other-private');
        $box.append($div);
        typewriterShow($div, text, 50, function() {
            $div.html(renderMarkdown(text));
        });
        updateSessionSelectSelection();
    }

    function renderMessages(list) {
        const $box = $('#ai-messages');
        if ($box.length === 0) return;
        if (!list || list.length === 0) {
            renderWelcome();
            return;
        }
        $box.empty();
        list.forEach(m => {
            const $bubble = appendMessage(m);
            if ($bubble) {
                $bubble.html(renderMarkdown(m.content));
            }
        });
        scrollToBottom();
        updateSessionSelectSelection();
    }

    function escapeHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function renderBold(text) {
        if (!text) return '';
        const regex = /\*\*(.+?)\*\*/g;
        let lastIndex = 0;
        let result = '';
        let match;
        while ((match = regex.exec(text)) !== null) {
            result += escapeHtml(text.slice(lastIndex, match.index));
            result += '<b>' + escapeHtml(match[1]) + '</b>';
            lastIndex = regex.lastIndex;
        }
        result += escapeHtml(text.slice(lastIndex));
        return result;
    }

    function renderMarkdown(text) {
        if (!md) return escapeHtml(text || '');
        return md.render(text || '');
    }

    function typewriterShow($el, fullText, speed, onComplete) {
        $el.text('');
        let idx = 0;
        function step() {
            if (idx <= fullText.length) {
                $el.text(fullText.substring(0, idx));
                idx++;
                setTimeout(step, speed);
            } else if (onComplete) {
                onComplete();
            }
        }
        step();
    }

    function appendMessage(m) {
        const $box = $('#ai-messages');
        if ($box.length === 0) return;
        const mine = m.role === 'USER';
        const $div = $('<div/>')
            .addClass('message')
            .addClass(mine ? 'my-private' : 'other-private');
        // 初始放置为纯文本，后续在动画完成后再渲染 Markdown
        $div.text(m.content || '');
        $box.append($div);
        return $div;
    }

    function appendThinkingBubble() {
        const $box = $('#ai-messages');
        if ($box.length === 0) return null;
        const $div = $('<div/>')
            .addClass('message')
            .addClass('other-message')
            .addClass('ai-thinking')
            .text('AI 正在思考中...');
        $box.append($div);
        scrollToBottom();
        return $div;
    }

    function loadSessions() {
        return $.ajax({
            url: 'api/ai/sessions',
            method: 'GET',
            xhrFields: { withCredentials: true }
        }).done(function(list) {
            list = list || [];
            const $select = $('#ai-session-select');
            if ($select.length) {
                $select.empty();
                $('<option/>').val('').text('选择会话').appendTo($select);
                list.forEach(function(s) {
                    const title = s.title || ('会话 ' + s.id);
                    $('<option/>').val(s.id).text(title).appendTo($select);
                });
                $select.off('change').on('change', function() {
                    const val = $(this).val();
                    if (!val) return;
                    currentSessionId = parseInt(val, 10);
                    loadMessages();
                });
                // 刷新列表后保持当前选择
                updateSessionSelectSelection();
            }

            // 旧的侧边列表
            const $container = $('#ai-sessions-list');
            if ($container.length) {
                $container.empty();
                list.forEach(function(s) {
                    const title = s.title || ('会话 ' + s.id);
                    const $a = $('<a/>')
                        .attr('href', '#')
                        .addClass('nav-item session-item')
                        .text(title)
                        .on('click', function(e) {
                            e.preventDefault();
                            currentSessionId = s.id;
                            loadMessages();
                        });
                    $container.append($a);
                });
            }

            return list;
        });
    }

    function loadMessages() {
        if (!currentSessionId) return;
        $.ajax({
            url: 'api/ai/messages',
            method: 'GET',
            data: { sessionId: currentSessionId },
            xhrFields: { withCredentials: true }
        }).done(function(list) {
            renderMessages(list || []);
        });
    }

    function sendMessage(text) {
        const payload = { sessionId: currentSessionId, message: text };

        // 先在本地追加用户消息
        appendMessage({ role: 'USER', content: text });
        scrollToBottom();

        const $thinking = appendThinkingBubble();

        return $.ajax({
            url: 'api/ai/send',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            xhrFields: { withCredentials: true }
        }).done(function(reply) {
            if ($thinking) {
                $thinking.remove();
            }

            if (reply && reply.sessionId) {
                currentSessionId = reply.sessionId;
                updateSessionSelectSelection();
            }

            var aiMsg = reply && reply.aiMessage ? reply.aiMessage : null;
            if (aiMsg && aiMsg.content) {
                const $aiBubble = appendMessage(aiMsg);
                // 打字机效果，结束后用 markdown-it 全渲染
                typewriterShow($aiBubble, aiMsg.content, 20, function() {
                    $aiBubble.html(renderMarkdown(aiMsg.content));
                });
                scrollToBottom();
            } else {
                loadSessions().then(loadMessages);
            }
        }).fail(function() {
            if ($thinking) {
                $thinking.remove();
            }
            alert('发送失败，请稍后重试');
        });
    }

    function scrollToBottom() {
        const $box = $('#ai-messages');
        if ($box.length === 0) return;
        $box.scrollTop($box.prop('scrollHeight'));
    }

    function populateSessionTable(list) {
        const $tbody = $('#ai-session-table-body');
        if ($tbody.length === 0) return;
        $tbody.empty();
        (list || []).forEach(function(s) {
            const $tr = $('<tr/>');
            $('<td/>').text(s.title || ('会话 ' + s.id)).appendTo($tr);
            const $ops = $('<td/>');
            const $open = $('<button/>')
                .addClass('link-btn')
                .text('打开')
                .on('click', function() {
                    currentSessionId = s.id;
                    $('#ai-session-panel').hide();
                    loadMessages();
                    updateSessionSelectSelection();
                });
            const $rename = $('<button/>')
                .addClass('link-btn')
                .text('重命名')
                .on('click', function() {
                    const newTitle = window.prompt('请输入新的会话标题：', s.title || '');
                    if (!newTitle) return;
                    $.ajax({
                        url: 'api/ai/sessions',
                        method: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify({ sessionId: s.id, title: newTitle })
                    }).done(function() {
                        loadSessions().then(populateSessionTable);
                    });
                });
            const $del = $('<button/>')
                .addClass('link-btn danger')
                .text('删除')
                .on('click', function() {
                    if (!window.confirm('确定要删除该会话吗？此操作不可恢复。')) return;
                    $.ajax({
                        url: 'api/ai/sessions?sessionId=' + encodeURIComponent(s.id),
                        method: 'DELETE'
                    }).done(function() {
                        if (currentSessionId === s.id) {
                            currentSessionId = null;
                            $('#ai-messages').empty();
                            renderWelcome();
                        }
                        $('#ai-session-panel').hide();
                        loadSessions();
                    });
                });
            $ops.append($open).append(' ').append($rename).append(' ').append($del);
            $ops.appendTo($tr);
            $tbody.append($tr);
        });
    }

    $(function() {
        const $form = $('#ai-send-form');
        const $input = $('#ai-input');
        const $newBtn = $('#new-ai-session');
        const $manageBtn = $('#ai-manage-sessions');
        const $panel = $('#ai-session-panel');
        const $panelClose = $('#ai-session-panel-close');

        if ($form.length && $input.length) {
            $form.on('submit', function(e) {
                e.preventDefault();
                const v = $.trim($input.val());
                if (!v) return;
                sendMessage(v);
                $input.val('');
            });
        }

        if ($newBtn.length) {
            $newBtn.on('click', function() {
                currentSessionId = null;
                renderWelcome();
            });
        }

        if ($manageBtn.length && $panel.length) {
            $manageBtn.on('click', function() {
                $panel.show();
                loadSessions().then(populateSessionTable);
            });
        }

        if ($panelClose.length) {
            $panelClose.on('click', function() {
                $('#ai-session-panel').hide();
            });
        }

        loadSessions().then(function() {
            // 初次进入页面：如果没有当前会话或没有消息，展示欢迎语
            if (!currentSessionId) {
                renderWelcome();
            } else {
                loadMessages();
            }
        });
    });
})(jQuery);

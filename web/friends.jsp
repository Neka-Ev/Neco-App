<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>好友列表 - Neco</title>
    <link rel="stylesheet" href="./assets/css/common.css">
    <link rel="stylesheet" href="./assets/css/chat.css">
    <link rel="stylesheet" href="./assets/css/friends.css">
</head>
<body>
    <div class="content-area">
        <h2 class="content-title">认识的人</h2>
        <div class="friends-actions">
            <input type="text" id="friend-search" placeholder="搜索..." class="search-input">
            <button id="search-friends-btn" class="refresh-btn">搜索</button>
            <button id="create-group-btn" class="create-group-btn">发起群聊</button>
        </div>
        
        <div class="friends-content">
            <div id="friends-list" class="friends-list">
                <!-- 好友列表将通过JavaScript动态加载 -->
                <div class="loading-indicator">加载中...</div>
            </div>
        </div>
    </div>

    <script src="assets/js/extra/jquery-3.7.1.min.js"></script>
    <script src="assets/js/local/friends.js"></script>
</body>
</html>
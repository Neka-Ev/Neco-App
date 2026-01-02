<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="assets/css/create-group.css" />
<div class="content-area">
    <h2 class="content-title">创建群聊</h2>
    <div class="create-group-container">
        <div class="create-group-form">
            <div class="form-group">
                <label for="group-name">群聊名称</label>
                <input type="text" id="group-name" class="form-input" placeholder="请输入群聊名称" required>
            </div>
            
            <div class="form-group">
                <label>选择好友</label>
                <div id="friends-select-container" class="friends-select-container">
                    <div class="loading-indicator">加载好友列表中...</div>
                </div>
            </div>
            
            <div class="form-actions">
                <button type="button" id="cancel-btn" class="cancel-btn">取消</button>
                <button type="button" id="create-group-submit" class="submit-btn">创建群聊</button>
            </div>
        </div>
    </div>
</div>

<script src="assets/js/extra/jquery-3.7.1.min.js"></script>
<script src="assets/js/local/create-group.js"></script>
<script>
    $(function() {
        window.createGroupManager = new CreateGroupManager();
        window.createGroupManager.init();
    });
</script>
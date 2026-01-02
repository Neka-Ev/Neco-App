// 工具函数库

/**
 * 显示通知
 */
function showNotification(title, message, icon = '/favicon.ico') {
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(title, {
            body: message,
            icon: icon
        });
    }
}

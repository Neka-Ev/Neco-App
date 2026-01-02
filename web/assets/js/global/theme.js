(function() {
    var themeKey = 'necoTheme';
    var root = document.documentElement;
    var currentTheme = null;

    function parseStoredTheme() {
        try {
            var raw = localStorage.getItem(themeKey);
            return raw ? raw.replace(/"/g, '') : null;
        } catch (e) {
            return null;
        }
    }

    function persistTheme(theme) {
        currentTheme = theme;
        try {
            localStorage.setItem(themeKey, theme);
        } catch (e) {}
    }

    function applyTheme(theme) {
        if (theme === 'dark') {
            root.classList.add('theme-dark');
        } else {
            root.classList.remove('theme-dark');
        }
        updateToggleLabel(theme);
    }

    function toggleTheme() {
        var next = root.classList.contains('theme-dark') ? 'light' : 'dark';
        applyTheme(next);
        persistTheme(next);
    }

    function updateToggleLabel(theme) {
        var toggles = document.querySelectorAll('[data-theme-toggle]');
        toggles.forEach(function(btn) {
            var label = btn.querySelector('[data-theme-label]');
            var icon = btn.querySelector('.theme-toggle-icon');
            if (label) {
                label.textContent = theme === 'dark' ? '切换至明色' : '切换至暗色';
            }
            if (icon) {
                icon.textContent = theme === 'dark' ? '☀️' : '🌙';
            }
        });
    }

    function hydrateFromStorage() {
        var saved = parseStoredTheme();
        if (saved === 'dark') {
            root.classList.add('theme-dark');
            currentTheme = 'dark';
        } else {
            root.classList.remove('theme-dark');
            currentTheme = 'light';
        }
        updateToggleLabel(currentTheme);
    }

    document.addEventListener('DOMContentLoaded', function() {
        hydrateFromStorage();
        document.body.addEventListener('click', function(evt) {
            var target = evt.target.closest('[data-theme-toggle]');
            if (target) {
                toggleTheme();
            }
        });
    });

    hydrateFromStorage();
})();


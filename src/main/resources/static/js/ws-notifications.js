(() => {
    const container = document.getElementById('ws-notifications');
    if (!container) {
        return;
    }

    const isAdmin = document.body && document.body.dataset.admin === 'true';
    const socket = new SockJS('/ws');
    const stomp = Stomp.over(socket);
    stomp.debug = null;

    const displayDurationMs = 3000;
    const storageKey = 'wsNotification';
    let reloadScheduled = false;

    const showNotification = (payload) => {
        if (!payload || !payload.message) {
            return;
        }
        const item = document.createElement('div');
        item.className = 'ws-toast';
        item.textContent = payload.message;
        container.appendChild(item);
        setTimeout(() => {
            item.classList.add('ws-toast-hide');
            setTimeout(() => item.remove(), 400);
        }, displayDurationMs);
    };

    const maybeRefresh = (payload) => {
        if (!payload || !payload.status) {
            return;
        }
        if (reloadScheduled) {
            return true;
        }
        const path = window.location.pathname || "";
        if (isAdmin && payload.status === 'PENDING' && path.startsWith('/admin/places')) {
            reloadScheduled = true;
            sessionStorage.setItem(storageKey, JSON.stringify(payload));
            window.location.reload();
            return true;
        }
        if (!isAdmin && (payload.status === 'APPROVED' || payload.status === 'REJECTED')) {
            if (path === '/' || path.startsWith('/places/')) {
                reloadScheduled = true;
                sessionStorage.setItem(storageKey, JSON.stringify(payload));
                window.location.reload();
                return true;
            }
        }
        return false;
    };

    const stored = sessionStorage.getItem(storageKey);
    if (stored) {
        try {
            const payload = JSON.parse(stored);
            showNotification(payload);
        } catch (err) {
            // Ignore invalid payloads.
        } finally {
            sessionStorage.removeItem(storageKey);
        }
    }

    const handlePayload = (payload) => {
        if (maybeRefresh(payload)) {
            return;
        }
        showNotification(payload);
    };

    stomp.connect({}, () => {
        stomp.subscribe('/user/queue/places', (message) => {
            handlePayload(JSON.parse(message.body));
        });
        if (isAdmin) {
            stomp.subscribe('/topic/admin/places', (message) => {
                handlePayload(JSON.parse(message.body));
            });
        }
    }, () => {
        // No-op on errors to avoid breaking the page.
    });
})();

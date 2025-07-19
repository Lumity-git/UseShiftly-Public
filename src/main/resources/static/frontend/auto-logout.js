// Auto-logout utility for hotel scheduler
// Logs out users after 10 minutes of inactivity

class AutoLogout {
    constructor(timeoutMinutes = 10) {
        this.timeoutMinutes = timeoutMinutes;
        this.timeoutMs = timeoutMinutes * 60 * 1000; // Convert to milliseconds
        this.timeoutId = null;
        this.warningTimeoutId = null;
        this.init();
    }

    init() {
        // Start the timer
        this.resetTimer();
        
        // Listen for user activity
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        events.forEach(event => {
            document.addEventListener(event, () => this.resetTimer(), true);
        });

        // Listen for API calls (which indicate activity)
        this.interceptFetch();
    }

    resetTimer() {
        // Clear existing timers
        if (this.timeoutId) clearTimeout(this.timeoutId);
        if (this.warningTimeoutId) clearTimeout(this.warningTimeoutId);

        // Show warning at 9 minutes (1 minute before logout)
        this.warningTimeoutId = setTimeout(() => {
            this.showWarning();
        }, this.timeoutMs - 60000); // 1 minute before timeout

        // Set logout timer
        this.timeoutId = setTimeout(() => {
            this.logout();
        }, this.timeoutMs);
    }

    showWarning() {
        const userConfirmed = confirm(
            `You will be automatically logged out in 1 minute due to inactivity.\n\n` +
            `Click OK to stay logged in, or Cancel to logout now.`
        );

        if (!userConfirmed) {
            this.logout();
        } else {
            // User chose to stay, reset timer
            this.resetTimer();
        }
    }

    logout() {
        // Clear all timers
        if (this.timeoutId) clearTimeout(this.timeoutId);
        if (this.warningTimeoutId) clearTimeout(this.warningTimeoutId);

        // Clear authentication data
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
        
        // Show logout message
        alert('You have been automatically logged out due to inactivity.');
        
        // Redirect to login
        window.location.href = 'login.html';
    }

    // Intercept fetch calls to reset timer on API activity
    interceptFetch() {
        const originalFetch = window.fetch;
        window.fetch = (...args) => {
            // Reset timer on any API call
            this.resetTimer();
            return originalFetch.apply(this, args);
        };
    }

    // Method to extend session (called when user performs significant actions)
    extendSession() {
        this.resetTimer();
    }

    // Method to manually logout
    manualLogout() {
        this.logout();
    }
}

// Global auto-logout instance
let autoLogout = null;

// Initialize auto-logout when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize if user is logged in
    if (localStorage.getItem('authToken')) {
        autoLogout = new AutoLogout(10); // 10 minutes
    }
});

// Export for manual use
window.AutoLogout = AutoLogout;
window.autoLogout = autoLogout;

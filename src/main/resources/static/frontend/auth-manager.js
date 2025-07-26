// Global authentication utility for hotel scheduler
class AuthManager {
  // Utility: Render large lists/tables in batches for responsive UI
  renderBatch(items, renderFn, container, batchSize = 20) {
    let i = 0;
    function renderNextBatch() {
      const end = Math.min(i + batchSize, items.length);
      for (; i < end; i++) {
        const html = renderFn(items[i], i);
        if (typeof html === 'string') {
          const div = document.createElement('div');
          div.innerHTML = html;
          while (div.firstChild) container.appendChild(div.firstChild);
        } else if (html instanceof HTMLElement) {
          container.appendChild(html);
        }
      }
      if (i < items.length) {
        setTimeout(renderNextBatch, 0);
      }
    }
    renderNextBatch();
  }
  constructor() {
    this.API_BASE_URL = window.location.origin + '/api';
    this.setupGlobalErrorHandler();
  }

  // Get the current authentication token
  getToken() {
    return localStorage.getItem("authToken");
  }

  // Get current user info
  getUserInfo() {
    const userInfo = localStorage.getItem("userInfo");
    try {
      return userInfo ? JSON.parse(userInfo) : null;
    } catch (e) {
      return null;
    }
  }

  // Check if user is authenticated
  isAuthenticated() {
    return !!this.getToken() && !!this.getUserInfo();
  }

  // Global error handling for unauthorized responses
  setupGlobalErrorHandler() {
    // You could set up interceptors for fetch or Axios here
    // e.g., check for 401 responses globally
    console.log('Global error handler set up (TODO)');
  }


  // Validate token with server
  async validateToken() {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    try {
      const response = await fetch(`${this.API_BASE_URL}/auth/validate`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        // Only logout on specific auth errors (401, 403)
        if (response.status === 401 || response.status === 403) {
          this.logout();
          return false;
        }
        // For other errors, just return false without logging out
        return false;
      }

      return true;
    } catch (error) {
      // Network errors shouldn't trigger logout - just log and return false
      return false;
    }
  }

  // Quick token validation without logout side effects
  async validateTokenQuick() {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    try {
      const response = await fetch(`${this.API_BASE_URL}/auth/validate`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });

      return response.ok;
    } catch (error) {
      return false;
    }
  }

  // Check authentication and redirect if necessary
  checkAuth(requiredRole = null) {
    if (!this.isAuthenticated()) {
      this.redirectToLogin();
      return false;
    }

    const userInfo = this.getUserInfo();

    // Check role requirements
    if (requiredRole) {
      if (Array.isArray(requiredRole)) {
        if (!requiredRole.includes(userInfo.role)) {
          return false;
        }
      } else {
        if (userInfo.role !== requiredRole) {
          return false;
        }
      }
    }

    return true;
  }

  // Redirect to appropriate dashboard based on role
  redirectToDashboard() {
    const userInfo = this.getUserInfo();
    if (userInfo) {
      if (userInfo.role === "EMPLOYEE") {
        window.location.href = "/frontend/employee-dashboard.html";
      } else {
        window.location.href = "/frontend/dashboard.html";
      }
    } else {
      this.redirectToLogin();
    }
  }

  // Redirect to login page
  redirectToLogin() {
    this.logout();
    window.location.href = "/frontend/login.html";
  }

  // Logout user
  logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userInfo");
  }

  // Make authenticated API request
  async apiRequest(endpoint, method = "GET", body = null, options = {}) {
    // options: { suppressLogout: true }
    const token = this.getToken();
    if (!token) {
      if (!options.suppressLogout) {
        this.redirectToLogin();
      }
      throw new Error("No authentication token");
    }

    const requestOptions = {
      method: method,
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    };

    if (body && (method === "POST" || method === "PUT" || method === "PATCH")) {
      requestOptions.body = JSON.stringify(body);
    }

    try {
      const response = await fetch(
        `${this.API_BASE_URL}${endpoint}`,
        requestOptions
      );

      // Handle authentication errors more carefully
      if (response.status === 401) {

        // Try to validate the token before automatically logging out
        const tokenValid = await this.validateTokenQuick();
        if (!tokenValid) {
          if (!options.suppressLogout) {
            this.logout();
            this.redirectToLogin();
          }
        } else {
        }
        throw new Error("Authentication failed");
      }

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `API request failed: ${response.status} ${response.statusText} - ${errorText}`
        );
      }

      // Return JSON data for successful requests
      if (response.status === 204 || method === "DELETE") {
        return { success: true }; // Return success indicator for no-content responses
      }

      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        const json = await response.json();
        return json;
      } else {
        const text = await response.text();
        return text;
      }
    } catch (error) {
      if (error.message.includes("Authentication failed")) {
        throw error;
      }
      throw error;
    }
  }

  // Setup global error handler for fetch requests
  setupGlobalErrorHandler() {
    // Override the global fetch to handle 401 errors automatically
    const originalFetch = window.fetch;
    window.fetch = async (...args) => {
      try {
        const response = await originalFetch.apply(window, args);
        // If this is an API request that returned 401, handle it
        if (response.status === 401 && args[0].includes("/api/")) {
          this.logout();
          this.redirectToLogin();
        }
        return response;
      } catch (error) {
        throw error;
      }
    };
  }

  // Display user info in the UI
  displayUserInfo() {
    const userInfo = this.getUserInfo();
    if (userInfo) {
      const userNameElement = document.getElementById("userName");
      const userRoleElement = document.getElementById("userRole");

      if (userNameElement) {
        userNameElement.textContent = `${userInfo.firstName} ${userInfo.lastName}`;
      }

      if (userRoleElement) {
        userRoleElement.textContent = userInfo.role;
      }
    }
  }

  // Utility: Format date/time as full ISO string with seconds
  formatDateTimeForApi(dateTime) {
    if (!dateTime) return null;
    // Accepts Date object or string
    let d = typeof dateTime === "string" ? new Date(dateTime) : dateTime;
    // Format: YYYY-MM-DDTHH:mm:ss
    return d.toISOString().slice(0, 19);
  }
}

// Create global instance
window.authManager = new AuthManager();

// Helper functions for backward compatibility
window.checkAuth = async (requiredRole) => {
  return window.authManager.checkAuth(requiredRole);
};

window.apiRequest = async (endpoint, method = "GET", body = null) => {
  return await window.authManager.apiRequest(endpoint, method, body);
};

window.logout = () => {
  window.authManager.logout();
  window.location.href = "/frontend/login.html";
};

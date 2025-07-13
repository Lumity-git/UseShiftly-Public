// Global authentication utility for hotel scheduler
class AuthManager {
  constructor() {
    this.API_BASE_URL = "http://localhost:8080/api";
    console.debug('[AuthManager] Constructor called');
    this.setupGlobalErrorHandler();
  }

  // Get the current authentication token
  getToken() {
    const token = localStorage.getItem("authToken");
    console.debug('[AuthManager] getToken:', token);
    return token;
  }

  // Get current user info
  getUserInfo() {
    const userInfo = localStorage.getItem("userInfo");
    console.debug('[AuthManager] getUserInfo raw:', userInfo);
    try {
      return userInfo ? JSON.parse(userInfo) : null;
    } catch (e) {
      console.error('[AuthManager] getUserInfo JSON parse error:', e);
      return null;
    }
  }

  // Check if user is authenticated
  isAuthenticated() {
    const token = this.getToken();
    const userInfo = this.getUserInfo();
    const result = token && userInfo;
    console.debug('[AuthManager] isAuthenticated:', result);
    return result;
  }

  // Validate token with server
  async validateToken() {
    const token = this.getToken();
    console.debug('[AuthManager] validateToken called, token:', token);
    if (!token) {
      console.warn('[AuthManager] validateToken: No token found');
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
      console.debug('[AuthManager] validateToken response:', response.status);

      if (!response.ok) {
        // Only logout on specific auth errors (401, 403)
        if (response.status === 401 || response.status === 403) {
          console.warn(
            "Token validation failed - authentication error:",
            response.status
          );
          this.logout();
          return false;
        }
        // For other errors, just return false without logging out
        console.warn(
          "Token validation failed - server error:",
          response.status
        );
        return false;
      }

      return true;
    } catch (error) {
      // Network errors shouldn't trigger logout - just log and return false
      console.warn("Token validation failed - network error:", error);
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
      console.warn("Quick token validation failed:", error);
      return false;
    }
  }

  // Check authentication and redirect if necessary
  checkAuth(requiredRole = null) {
    console.debug('[AuthManager] checkAuth called, requiredRole:', requiredRole);
    if (!this.isAuthenticated()) {
      console.warn('[AuthManager] checkAuth: Not authenticated');
      this.redirectToLogin();
      return false;
    }

    const userInfo = this.getUserInfo();
    console.debug('[AuthManager] checkAuth: userInfo:', userInfo);

    // Check role requirements
    if (requiredRole) {
      if (Array.isArray(requiredRole)) {
        if (!requiredRole.includes(userInfo.role)) {
          console.warn("Access denied: insufficient role", userInfo.role);
          return false;
        }
      } else {
        if (userInfo.role !== requiredRole) {
          console.warn("Access denied: insufficient role", userInfo.role);
          return false;
        }
      }
    }

    return true;
  }

  // Redirect to appropriate dashboard based on role
  redirectToDashboard() {
    const userInfo = this.getUserInfo();
    console.debug('[AuthManager] redirectToDashboard: userInfo:', userInfo);
    if (userInfo) {
      if (userInfo.role === "EMPLOYEE") {
        console.debug('[AuthManager] redirectToDashboard: EMPLOYEE role');
        window.location.href = "/frontend/employee-dashboard.html";
      } else {
        console.debug('[AuthManager] redirectToDashboard: MANAGER/ADMIN role');
        window.location.href = "/frontend/dashboard.html";
      }
    } else {
      console.warn('[AuthManager] redirectToDashboard: No userInfo, redirecting to login');
      this.redirectToLogin();
    }
  }

  // Redirect to login page
  redirectToLogin() {
    console.warn('[AuthManager] redirectToLogin called');
    this.logout();
    window.location.href = "/frontend/login.html";
  }

  // Logout user
  logout() {
    console.warn('[AuthManager] logout called');
    localStorage.removeItem("authToken");
    localStorage.removeItem("userInfo");
  }

  // Make authenticated API request
  async apiRequest(endpoint, method = "GET", body = null, options = {}) {
    // options: { suppressLogout: true }
    const token = this.getToken();
    console.debug('[AuthManager] apiRequest called:', endpoint, method, body, 'token:', token, 'options:', options);
    if (!token) {
      console.warn('[AuthManager] apiRequest: No token, redirecting to login');
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
      console.debug('[AuthManager] apiRequest response:', response.status, response.statusText);

      // Handle authentication errors more carefully
      if (response.status === 401) {
        console.warn(
          "401 Unauthorized - checking if token is actually invalid"
        );

        // Try to validate the token before automatically logging out
        const tokenValid = await this.validateTokenQuick();
        console.debug('[AuthManager] apiRequest tokenValid:', tokenValid);
        if (!tokenValid) {
          console.warn("Token confirmed invalid - logging out");
          if (!options.suppressLogout) {
            this.logout();
            this.redirectToLogin();
          }
        } else {
          console.warn(
            "Token seems valid but got 401 - may be endpoint permission issue"
          );
        }
        throw new Error("Authentication failed");
      }

      if (!response.ok) {
        const errorText = await response.text();
        console.error('[AuthManager] apiRequest failed:', response.status, response.statusText, errorText);
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
        console.debug('[AuthManager] apiRequest response JSON:', json);
        return json;
      } else {
        const text = await response.text();
        console.debug('[AuthManager] apiRequest response text:', text);
        return text;
      }
    } catch (error) {
      if (error.message.includes("Authentication failed")) {
        throw error;
      }
      console.error("API request error:", error);
      throw error;
    }
  }

  // Setup global error handler for fetch requests
  setupGlobalErrorHandler() {
    // Override the global fetch to handle 401 errors automatically
    const originalFetch = window.fetch;
    window.fetch = async (...args) => {
      console.debug('[AuthManager] Global fetch intercepted:', args);
      try {
        const response = await originalFetch.apply(window, args);
        console.debug('[AuthManager] Global fetch response:', response.status, response.statusText);
        // If this is an API request that returned 401, handle it
        if (response.status === 401 && args[0].includes("/api/")) {
          console.warn("401 Unauthorized - redirecting to login (global fetch)");
          this.logout();
          this.redirectToLogin();
        }
        return response;
      } catch (error) {
        console.error('[AuthManager] Global fetch error:', error);
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

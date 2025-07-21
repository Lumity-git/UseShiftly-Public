// nav-manager.js
function renderNavLinks(currentPage) {
  const nav = document.getElementById("navigation");
  let links = [];
  const userInfo = localStorage.getItem("userInfo");
  if (userInfo) {
    const user = JSON.parse(userInfo);
    if (["MANAGER","ADMIN"].includes(user.role)) {
      links.push('<a href="dashboard" class="nav-link">Dashboard</a>');
      links.push('<a href="shifts" class="nav-link">Shifts</a>');
      links.push('<a href="trades" class="nav-link">Trades</a>');
      links.push('<a href="notifications" class="nav-link">Notifications</a>');
      links.push('<a href="employees" class="nav-link">Employees</a>');
      links.push('<a href="reports" class="nav-link">Reports</a>');
      links.push('<a href="departments" class="nav-link">Departments</a>');
      links.push('<a href="admin-logs" class="nav-link">Logs</a>');
    } else {
      // Employee links use employee-*.html files
      links.push('<a href="employee-dashboard.html" class="nav-link">Dashboard</a>');
      links.push('<a href="employee-shifts.html" class="nav-link">Shifts</a>');
      links.push('<a href="employee-trades.html" class="nav-link">Trades</a>');
      links.push('<a href="employee-notifications.html" class="nav-link">Notifications</a>');
      links.push('<a href="employee-profile.html" class="nav-link">Profile</a>');
    }
  } else {
    links.push('<a href="dashboard" class="nav-link">Dashboard</a>');
    links.push('<a href="login" class="nav-link">Login</a>');
  }
  if (nav) nav.innerHTML = links.join("");
  highlightCurrentNavLink(currentPage);
}

function highlightCurrentNavLink(currentPage) {
  const navLinks = document.querySelectorAll('.nav-link');
  navLinks.forEach(link => {
    if (link.getAttribute('href') === currentPage) {
      link.classList.add('active');
    } else {
      link.classList.remove('active');
    }
  });
}

const token = localStorage.getItem('superAdminToken');
if (!token) window.location.href = '/frontend/super-admin-login.html';

async function loadUsage() {
    const resp = await fetch('/api/super-admin/admins/usage', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const data = await resp.json();
    renderUsageTable(data);
}

async function loadBillingPreview() {
    const resp = await fetch('/api/super-admin/billing/preview', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const preview = await resp.json();
    const usageResp = await fetch('/api/super-admin/admins/usage', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const usage = await usageResp.json();
    usage.forEach(row => {
        row.projectedBill = preview[row.adminEmail] || 0;
    });
    renderUsageTable(usage);
}

function renderUsageTable(data) {
    const tbody = document.querySelector('#usageTable tbody');
    tbody.innerHTML = '';
    const batchSize = 20;
    let i = 0;
    function renderBatch() {
        const end = Math.min(i + batchSize, data.length);
        for (; i < end; i++) {
            const row = data[i];
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${row.adminEmail}</td>
                <td>${row.employees}</td>
                <td>${row.managers}</td>
                <td>${row.buildings}</td>
                <td>${row.package || ''}</td>
                <td>${row.billableUsers !== undefined ? row.billableUsers : Math.max(0, row.employees - 5)}</td>
                <td>${row.overFreeTier !== undefined ? row.overFreeTier : (row.employees > 5 ? 'Yes' : 'No')}</td>
                <td>${row.projectedBill !== undefined ? row.projectedBill : ''}</td>
            `;
            tbody.appendChild(tr);
        }
        if (i < data.length) {
            setTimeout(renderBatch, 0);
        }
    }
    renderBatch();
}


async function generateReceipts() {
    const period = document.getElementById('billingPeriod').value;
    if (!period) {
        document.getElementById('receiptStatus').textContent = 'Select a billing period.';
        return;
    }
    document.getElementById('receiptStatus').textContent = 'Generating...';
    const resp = await fetch(`/api/super-admin/billing/generate-receipts?period=${period}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    });
    if (resp.ok) {
        document.getElementById('receiptStatus').textContent = 'Receipts generated and emailed.';
    } else {
        document.getElementById('receiptStatus').textContent = 'Error generating receipts.';
    }
}

async function loadReceipts() {
    const resp = await fetch('/api/super-admin/billing/receipts', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const receipts = await resp.json();
    let html = '<h3>All Receipts</h3><table class="billing-table"><thead><tr><th>Admin</th><th>Amount</th><th>Period</th><th>Date</th></tr></thead><tbody>';
    receipts.forEach(r => {
        html += `<tr><td>${r.adminEmail}</td><td>$${r.amount}</td><td>${r.period}</td><td>${r.date}</td></tr>`;
    });
    html += '</tbody></table>';
    document.getElementById('receipts').innerHTML = html;
}

async function loadEvents() {
    const resp = await fetch('/api/super-admin/billing/events', {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const events = await resp.json();
    let html = '<h3>Audit Log</h3><table class="billing-table"><thead><tr><th>Type</th><th>Admin</th><th>Details</th><th>Timestamp</th></tr></thead><tbody>';
    events.forEach(e => {
        html += `<tr><td>${e.type}</td><td>${e.adminEmail}</td><td>${JSON.stringify(e.details)}</td><td>${e.timestamp}</td></tr>`;
    });
    html += '</tbody></table>';
    document.getElementById('events').innerHTML = html;
}

async function loadAnalytics() {
  const token = localStorage.getItem('superAdminToken');
  let html = '';
  // Total revenue
  const revResp = await fetch('/api/super-admin/billing/analytics/total-revenue', { headers: { 'Authorization': 'Bearer ' + token } });
  const totalRevenue = await revResp.json();
  html += `<div><strong>Total Revenue:</strong> $${totalRevenue}</div>`;
  // ARPU
  const arpuResp = await fetch('/api/super-admin/billing/analytics/arpu', { headers: { 'Authorization': 'Bearer ' + token } });
  const arpu = await arpuResp.json();
  html += `<div><strong>ARPU:</strong> $${arpu.toFixed(2)}</div>`;
  // Revenue by admin
  const byAdminResp = await fetch('/api/super-admin/billing/analytics/revenue-by-admin', { headers: { 'Authorization': 'Bearer ' + token } });
  const byAdmin = await byAdminResp.json();
  html += '<div><strong>Revenue by Admin:</strong><ul>';
  Object.entries(byAdmin).forEach(([email, amt]) => {
    html += `<li>${email}: $${amt}</li>`;
  });
  html += '</ul></div>';
  document.getElementById('analytics').innerHTML = html;
}

async function exportReceiptsCsv() {
  const token = localStorage.getItem('superAdminToken');
  const resp = await fetch('/api/super-admin/billing/analytics/export-receipts', { headers: { 'Authorization': 'Bearer ' + token } });
  const csv = await resp.text();
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.getElementById('csvDownload');
  a.href = url;
  a.download = 'receipts.csv';
  a.style.display = 'inline';
  a.click();
  setTimeout(() => { URL.revokeObjectURL(url); a.style.display = 'none'; }, 1000);
}

// Auto-load usage on page load
window.onload = loadUsage;

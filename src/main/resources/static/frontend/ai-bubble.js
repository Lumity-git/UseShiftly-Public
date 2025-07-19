// AI Assistant Bubble Logic
(function() {
  const userInfo = window.authManager ? window.authManager.getUserInfo() : null;
  const allowedRoles = ['ADMIN', 'MANAGER'];
  if (!userInfo || !allowedRoles.includes(userInfo.role)) return;

  // Inject AI bubble HTML
  fetch('ai-bubble.html')
    .then(res => res.text())
    .then(html => {
      const container = document.createElement('div');
      container.innerHTML = html;
      document.body.appendChild(container);
      setupAIBubble();
    });

  function setupAIBubble() {
    const bubble = document.getElementById('aiBubble');
    const toggle = document.getElementById('aiBubbleToggle');
    const close = document.getElementById('aiBubbleClose');
    const form = document.getElementById('aiBubbleForm');
    const input = document.getElementById('aiBubbleInput');
    const messages = document.getElementById('aiBubbleMessages');

    toggle.style.display = 'block';
    toggle.onclick = () => { bubble.style.display = 'block'; toggle.style.display = 'none'; };
    close.onclick = () => { bubble.style.display = 'none'; toggle.style.display = 'block'; };

    form.onsubmit = function(e) {
      e.preventDefault();
      const text = input.value.trim();
      if (!text) return;
      addMessage('You', text);
      input.value = '';
      // Simulate AI response (replace with backend call)
      setTimeout(() => addMessage('AI', 'Processing: ' + text), 800);
    };

    function addMessage(sender, text) {
      const msg = document.createElement('div');
      msg.className = 'ai-bubble-message ' + (sender === 'AI' ? 'ai' : 'user');
      msg.innerHTML = `<strong>${sender}:</strong> ${text}`;
      messages.appendChild(msg);
      messages.scrollTop = messages.scrollHeight;
    }
  }
})();

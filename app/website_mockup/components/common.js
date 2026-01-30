// 生成状态栏HTML
function createStatusBar() {
  const now = new Date();
  const hours = now.getHours().toString().padStart(2, "0");
  const minutes = now.getMinutes().toString().padStart(2, "0");

  return `
    <div class="status-bar">
      <div class="time">${hours}:${minutes}</div>
      <div class="icons">
        <i class="fas fa-signal"></i>
        <i class="fas fa-wifi"></i>
        <i class="fas fa-battery-full"></i>
      </div>
    </div>
  `;
}

// 生成底部导航栏HTML
function createTabBar(activeTab) {
  return `
    <div class="tab-bar">
      <a href="home.html" class="tab-item ${
        activeTab === "home" ? "active" : ""
      }">
        <i class="fas fa-compass"></i>
        <span>发现</span>
      </a>
      <a href="chat_list.html" class="tab-item ${
        activeTab === "chat" ? "active" : ""
      }">
        <i class="fas fa-comment-alt"></i>
        <span>聊天</span>
      </a>
      <a href="community.html" class="tab-item ${
        activeTab === "community" ? "active" : ""
      }">
        <i class="fas fa-globe"></i>
        <span>社区</span>
      </a>
      <a href="profile.html" class="tab-item ${
        activeTab === "profile" ? "active" : ""
      }">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </a>
    </div>
  `;
}

// 在页面加载时初始化公共组件
document.addEventListener("DOMContentLoaded", function () {
  // 添加状态栏
  const statusBarContainer = document.getElementById("status-bar");
  if (statusBarContainer) {
    statusBarContainer.innerHTML = createStatusBar();
  }

  // 添加底部导航栏
  const tabBarContainer = document.getElementById("tab-bar");
  if (tabBarContainer) {
    const activeTab = document.body.getAttribute("data-active-tab");
    tabBarContainer.innerHTML = createTabBar(activeTab);
  }

  // 更新时间
  setInterval(() => {
    const statusBarContainer = document.getElementById("status-bar");
    if (statusBarContainer) {
      statusBarContainer.innerHTML = createStatusBar();
    }
  }, 60000); // 每分钟更新一次
});

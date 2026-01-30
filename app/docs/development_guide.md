# AI恋恋 Android应用开发规范文档

## 目录
- [开发环境配置](#开发环境配置)
- [项目结构规范](#项目结构规范)
- [核心功能实现](#核心功能实现)
- [UI/UX实现规范](#uiux实现规范)
- [数据层架构](#数据层架构)
- [第三方服务集成](#第三方服务集成)
- [性能优化要求](#性能优化要求)
- [安全措施](#安全措施)
- [测试要求](#测试要求)
- [部署要求](#部署要求)
- [附录](#附录)

## 开发环境配置

### IDE和SDK要求
- Android Studio：最新稳定版
- Gradle：最新稳定版
- JDK版本：Java 11
- Kotlin版本：最新稳定版（可选）

### API Level要求
- 目标SDK：API Level 34 (Android 14)
- 最低SDK：API Level 28 (Android 9.0)

### 核心开发工具
- ViewBinding：替代findViewById
- Material Design：1.11.0+版本
- 操作系统：Windows 11

### 基础依赖配置
```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 28
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        viewBinding true
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX Core
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    
    // ConstraintLayout
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Navigation Component
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'
    
    // Lifecycle Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    
    // Retrofit & OkHttp
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    
    // WebSocket
    implementation 'com.squareup.okhttp3:okhttp-ws:4.12.0'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-analytics'
    implementation 'com.google.firebase:firebase-messaging'
    
    // ML Kit
    implementation 'com.google.mlkit:smart-reply:17.0.2'
    
    // CameraX
    implementation 'androidx.camera:camera-core:1.3.1'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    implementation 'androidx.camera:camera-view:1.3.1'
    
    // Lottie Animations
    implementation 'com.airbnb.android:lottie:6.1.0'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

## 项目结构规范

### 目录结构
```
app/
├─ src/main/
│  ├─ java/com/ailianlian/ablecisi
│  │  ├─ splash/      # 开屏动画模块
│  │  │  ├─ SplashActivity.java
│  │  │  └─ viewmodel/
│  │  │
│  │  ├─ login/       # 登录认证模块
│  │  │  ├─ activity/
│  │  │  ├─ fragment/
│  │  │  ├─ viewmodel/
│  │  │  └─ repository/
│  │  │
│  │  ├─ find/        # 发现页模块
│  │  ├─ chat/        # 聊天核心模块
│  │  ├─ social/      # 社交功能模块
│  │  ├─ profile/     # 个人资料模块
│  │  ├─ aicustom/    # AI角色定制模块
│  │  ├─ common/      # 公共组件
│  │  └─ MainActivity.java
│  │
│  ├─ res/
│  │  ├─ color/       # 颜色选择器
│  │  ├─ layout/      # 布局文件
│  │  ├─ drawable/    # 图片资源
│  │  ├─ menu/        # 菜单文件
│  │  ├─ values/      # 资源值
│  │  └─ navigation/  # 导航图
```

### 模块职责划分
- **splash/**：启动页和引导页
- **login/**：用户认证和注册
- **find/**：AI角色发现和推荐
- **chat/**：即时通讯核心功能
- **social/**：社交动态和互动
- **profile/**：用户资料管理
- **aicustom/**：AI角色定制
- **common/**：公共工具和组件

## 数据库

### 数据库设计
#### 功能表
用户表 (tb_user)
```Sql
CREATE TABLE tb_user (
    user_id BIGINT PRIMARY KEY,                -- 用户ID，主键
    username VARCHAR(50) NOT NULL,             -- 用户名
    phone_number VARCHAR(20) UNIQUE,           -- 手机号码
    password_hash VARCHAR(128) NOT NULL,       -- 密码哈希
    avatar_url VARCHAR(255),                   -- 头像URL
    nickname VARCHAR(50),                      -- 昵称
    gender TINYINT DEFAULT 0,                  -- 性别：0-未知，1-男，2-女
    birth_date DATE,                           -- 出生日期
    bio TEXT,                                  -- 个人简介
    location VARCHAR(100),                     -- 位置
    registration_type TINYINT NOT NULL,        -- 注册类型：1-手机号，2-QQ，3-微信
    is_vip TINYINT DEFAULT 0,                  -- 是否VIP：0-否，1-是
    is_verified TINYINT DEFAULT 0,             -- 是否实名认证：0-否，1-是
    status TINYINT DEFAULT 1,                  -- 状态：0-禁用，1-正常
    last_login_time DATETIME,                  -- 最后登录时间
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0               -- 是否删除：0-否，1-是
);
-- 索引
CREATE INDEX idx_user_phone ON tb_user(phone_number);
CREATE INDEX idx_user_nickname ON tb_user(nickname);
```

用户-AI角色关系表 (tb_user_character)
```Sql
CREATE TABLE tb_user_character (
    id BIGINT PRIMARY KEY,                     -- 主键ID
    user_id BIGINT NOT NULL,                   -- 用户ID
    character_id BIGINT NOT NULL,              -- AI角色ID
    nickname VARCHAR(50),                      -- 用户对AI的自定义昵称
    intimacy_level INT DEFAULT 0,              -- 亲密度等级
    chat_count BIGINT DEFAULT 0,               -- 聊天次数
    last_chat_time DATETIME,                   -- 最后聊天时间
    is_favorite TINYINT DEFAULT 0,             -- 是否收藏：0-否，1-是
    custom_settings TEXT,                      -- 自定义设置（JSON格式）
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (character_id) REFERENCES tb_ai_character(character_id)
);
-- 索引
CREATE INDEX idx_user_character ON tb_user_character(user_id, character_id);
CREATE INDEX idx_character_intimacy ON tb_user_character(intimacy_level);
```

AI角色表 (tb_ai_character)
```Sql
CREATE TABLE tb_ai_character (
    character_id BIGINT PRIMARY KEY,           -- AI角色ID，主键
    name VARCHAR(50) NOT NULL,                 -- 角色名称
    avatar_url VARCHAR(255),                   -- 角色头像URL
    character_type VARCHAR(50) NOT NULL,       -- 角色类型：温柔女友、知心朋友等
    description TEXT,                          -- 角色描述
    personality TEXT,                          -- 性格特点
    background_story TEXT,                     -- 背景故事
    voice_id VARCHAR(50),                      -- 语音ID
    is_premium TINYINT DEFAULT 0,              -- 是否付费：0-免费，1-付费
    popularity BIGINT DEFAULT 0,               -- 受欢迎程度/使用人数
    status TINYINT DEFAULT 1,                  -- 状态：0-下线，1-上线
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0               -- 是否删除：0-否，1-是
);

-- 索引
CREATE INDEX idx_character_type ON tb_ai_character(character_type);
CREATE INDEX idx_character_popularity ON tb_ai_character(popularity);
```

会话表 (tb_conversation)
```Sql
CREATE TABLE tb_conversation (
    conversation_id BIGINT PRIMARY KEY,        -- 会话ID，主键
    user_id BIGINT NOT NULL,                   -- 用户ID
    character_id BIGINT NOT NULL,              -- AI角色ID
    conversation_name VARCHAR(100),            -- 会话名称（可自定义）
    last_message TEXT,                         -- 最后一条消息内容
    last_message_time DATETIME,                -- 最后一条消息时间
    unread_count INT DEFAULT 0,                -- 未读消息数量
    is_pinned TINYINT DEFAULT 0,               -- 是否置顶：0-否，1-是
    is_archived TINYINT DEFAULT 0,             -- 是否归档：0-否，1-是
    status TINYINT DEFAULT 1,                  -- 状态：0-禁用，1-正常
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (character_id) REFERENCES tb_ai_character(character_id)
);

-- 索引
CREATE INDEX idx_conversation_user ON tb_conversation(user_id);
CREATE INDEX idx_conversation_user_character ON tb_conversation(user_id, character_id);
CREATE INDEX idx_conversation_last_time ON tb_conversation(last_message_time);
```

聊天消息表 (tb_chat_message)
```Sql
CREATE TABLE tb_chat_message (
    message_id BIGINT PRIMARY KEY,             -- 消息ID，主键
    conversation_id BIGINT NOT NULL,           -- 会话ID
    user_id BIGINT NOT NULL,                   -- 用户ID
    character_id BIGINT NOT NULL,              -- AI角色ID
    sender_type TINYINT NOT NULL,              -- 发送者类型：0-用户，1-AI
    content TEXT NOT NULL,                     -- 消息内容
    content_type TINYINT DEFAULT 0,            -- 内容类型：0-文本，1-图片，2-语音，3-视频
    media_url VARCHAR(255),                    -- 媒体URL
    is_read TINYINT DEFAULT 0,                 -- 是否已读：0-未读，1-已读
    is_recalled TINYINT DEFAULT 0,             -- 是否撤回：0-否，1-是
    create_time DATETIME NOT NULL,             -- 创建时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (conversation_id) REFERENCES tb_conversation(conversation_id),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (character_id) REFERENCES tb_ai_character(character_id)
);

-- 索引
CREATE INDEX idx_message_conversation ON tb_chat_message(conversation_id);
CREATE INDEX idx_message_user_character ON tb_chat_message(user_id, character_id);
CREATE INDEX idx_message_create_time ON tb_chat_message(create_time);
```
社区动态表 (tb_community_post)
```Sql
CREATE TABLE tb_community_post (
    post_id BIGINT PRIMARY KEY,                -- 动态ID，主键
    user_id BIGINT NOT NULL,                   -- 发布用户ID
    title VARCHAR(100),                        -- 标题
    content TEXT NOT NULL,                     -- 内容
    post_type TINYINT DEFAULT 0,               -- 动态类型：0-普通，1-精选，2-置顶
    view_count BIGINT DEFAULT 0,               -- 浏览次数
    like_count BIGINT DEFAULT 0,               -- 点赞次数
    comment_count BIGINT DEFAULT 0,            -- 评论次数
    share_count BIGINT DEFAULT 0,              -- 分享次数
    status TINYINT DEFAULT 1,                  -- 状态：0-审核中，1-已发布，2-已拒绝
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- 索引
CREATE INDEX idx_post_user ON tb_community_post(user_id);
CREATE INDEX idx_post_create_time ON tb_community_post(create_time);
CREATE INDEX idx_post_popularity ON tb_community_post(view_count, like_count);
```

动态媒体表 (tb_post_media)
```Sql
CREATE TABLE tb_post_media (
    media_id BIGINT PRIMARY KEY,               -- 媒体ID，主键
    post_id BIGINT NOT NULL,                   -- 动态ID
    media_url VARCHAR(255) NOT NULL,           -- 媒体URL
    media_type TINYINT NOT NULL,               -- 媒体类型：0-图片，1-视频
    sort_order INT DEFAULT 0,                  -- 排序顺序
    create_time DATETIME NOT NULL,             -- 创建时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (post_id) REFERENCES tb_community_post(post_id)
);

-- 索引
CREATE INDEX idx_media_post ON tb_post_media(post_id);
```

文章表 (tb_article)
```Sql
CREATE TABLE tb_article (
    article_id BIGINT PRIMARY KEY,             -- 文章ID，主键
    title VARCHAR(100) NOT NULL,               -- 标题
    cover_url VARCHAR(255),                    -- 封面图URL
    author_id BIGINT,                          -- 作者ID（可能是系统或用户）
    author_name VARCHAR(50) NOT NULL,          -- 作者名称
    author_title VARCHAR(50),                  -- 作者头衔
    content TEXT NOT NULL,                     -- 文章内容
    summary TEXT,                              -- 摘要
    view_count BIGINT DEFAULT 0,               -- 浏览次数
    like_count BIGINT DEFAULT 0,               -- 点赞次数
    comment_count BIGINT DEFAULT 0,            -- 评论次数
    share_count BIGINT DEFAULT 0,              -- 分享次数
    is_featured TINYINT DEFAULT 0,             -- 是否精选：0-否，1-是
    status TINYINT DEFAULT 1,                  -- 状态：0-草稿，1-已发布
    publish_time DATETIME,                     -- 发布时间
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    is_deleted TINYINT DEFAULT 0               -- 是否删除：0-否，1-是
);

-- 索引
CREATE INDEX idx_article_author ON tb_article(author_id);
CREATE INDEX idx_article_publish_time ON tb_article(publish_time);
CREATE INDEX idx_article_popularity ON tb_article(view_count, like_count);
```

文章标签表 (tb_article_tag)
```Sql
CREATE TABLE tb_article_tag (
    id BIGINT PRIMARY KEY,                     -- 主键ID
    article_id BIGINT NOT NULL,                -- 文章ID
    tag_name VARCHAR(50) NOT NULL,             -- 标签名称
    create_time DATETIME NOT NULL,             -- 创建时间
    
    FOREIGN KEY (article_id) REFERENCES tb_article(article_id)
);

-- 索引
CREATE INDEX idx_article_tag ON tb_article_tag(article_id);
CREATE INDEX idx_tag_name ON tb_article_tag(tag_name);
```

#### 通用表
用户设置表 (tb_user_setting)
```Sql
CREATE TABLE tb_user_setting (
    setting_id BIGINT PRIMARY KEY,             -- 设置ID，主键
    user_id BIGINT NOT NULL,                   -- 用户ID
    notification_enabled TINYINT DEFAULT 1,    -- 通知开关：0-关闭，1-开启
    message_reminder TINYINT DEFAULT 1,        -- 消息提醒：0-关闭，1-开启
    dark_mode TINYINT DEFAULT 0,               -- 深色模式：0-关闭，1-开启
    language VARCHAR(20) DEFAULT 'zh_CN',      -- 语言设置
    privacy_level TINYINT DEFAULT 0,           -- 隐私级别：0-公开，1-好友可见，2-私密
    invisible_mode TINYINT DEFAULT 0,          -- 隐身模式：0-关闭，1-开启
    create_time DATETIME NOT NULL,             -- 创建时间
    update_time DATETIME NOT NULL,             -- 更新时间
    
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- 索引
CREATE UNIQUE INDEX idx_setting_user ON tb_user_setting(user_id);
```

评论表 (tb_comment)
```Sql
CREATE TABLE tb_comment (
    comment_id BIGINT PRIMARY KEY,             -- 评论ID，主键
    post_id BIGINT NOT NULL,                   -- 动态ID
    user_id BIGINT NOT NULL,                   -- 评论用户ID
    parent_id BIGINT DEFAULT 0,                -- 父评论ID，0表示一级评论
    content TEXT NOT NULL,                     -- 评论内容
    like_count BIGINT DEFAULT 0,               -- 点赞次数
    reply_count BIGINT DEFAULT 0,              -- 回复次数
    create_time DATETIME NOT NULL,             -- 创建时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (post_id) REFERENCES tb_community_post(post_id),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- 索引
CREATE INDEX idx_comment_post ON tb_comment(post_id);
CREATE INDEX idx_comment_parent ON tb_comment(parent_id);
CREATE INDEX idx_comment_create_time ON tb_comment(create_time);
```

点赞表 (tb_like)
```Sql
CREATE TABLE tb_like (
    like_id BIGINT PRIMARY KEY,                -- 点赞ID，主键
    user_id BIGINT NOT NULL,                   -- 用户ID
    target_id BIGINT NOT NULL,                 -- 目标ID（动态ID或评论ID）
    target_type TINYINT NOT NULL,              -- 目标类型：0-动态，1-评论
    create_time DATETIME NOT NULL,             -- 创建时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- 索引
CREATE INDEX idx_like_user_target ON tb_like(user_id, target_id, target_type);
```

关注关系表 (tb_follow)
```Sql
CREATE TABLE tb_follow (
    follow_id BIGINT PRIMARY KEY,              -- 关注ID，主键
    follower_id BIGINT NOT NULL,               -- 关注者ID
    followed_id BIGINT NOT NULL,               -- 被关注者ID
    create_time DATETIME NOT NULL,             -- 创建时间
    is_deleted TINYINT DEFAULT 0,              -- 是否删除：0-否，1-是
    
    FOREIGN KEY (follower_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (followed_id) REFERENCES tb_user(user_id)
);

-- 索引
CREATE UNIQUE INDEX idx_follow_relation ON tb_follow(follower_id, followed_id);
CREATE INDEX idx_follow_follower ON tb_follow(follower_id);
CREATE INDEX idx_follow_followed ON tb_follow(followed_id);
```

## 核心功能实现

### 1. 实时聊天模块
#### WebSocket通信
```java
public class ChatWebSocket {
    private static final String WS_URL = "wss://api.ailianlian.com/ws";
    private WebSocket webSocket;
    
    public void connect() {
        // WebSocket连接实现
    }
    
    public void sendMessage(ChatMessage message) {
        // 消息发送实现
    }
}
```

#### 智能回复集成
```java
public class SmartReplyManager {
    private SmartReply smartReply;
    
    public List<SmartReplyResponse> getSmartReplies(List<ChatMessage> messages) {
        // ML Kit智能回复实现
    }
}
```

#### 消息分页加载
```java
@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    PagingSource<Integer, ChatMessage> getMessagesPagingSource();
}
```

### 2. AI对话系统
#### API接口定义
```java
public interface AIService {
    @POST("chat/completions")
    Call<AIResponse> getChatCompletion(@Body ChatRequest request);
}
```

#### 上下文管理
```java
@Database(entities = {ChatContext.class}, version = 1)
public abstract class ChatDatabase extends RoomDatabase {
    public abstract ChatContextDao chatContextDao();
}
```

### 3. 社交功能
#### Feed流实现
```java
public class FeedAdapter extends ListAdapter<FeedItem, FeedViewHolder> {
    public FeedAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<FeedItem> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<FeedItem>() {
            // DiffUtil实现
        };
}
```

## UI/UX实现规范

### 1. 布局规范
- 使用ConstraintLayout作为根布局
- 避免布局嵌套超过3层
- 使用dimens资源定义间距

### 2. 主题定义
```xml
<style name="Theme.AILianLian" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- 主题配置 -->
</style>
```

### 3. 颜色规范
```xml
<resources>
    <color name="primary">#C3BEF0</color>
    <color name="primary_light">#D8D5F7</color>
    <color name="primary_dark">#A29BD8</color>
    <color name="accent">#8A7AD8</color>
    <color name="secondary">#DEFCF9</color>
</resources>
```

## 数据层架构

### 1. Clean Architecture实现
```
data/
├─ repository/
├─ datasource/
│  ├─ local/
│  └─ remote/
├─ model/
└─ mapper/

domain/
├─ usecase/
├─ repository/
└─ model/
```

### 2. 数据库加密
```java
@Database(entities = {...}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        // SQLCipher配置
    }
}
```

## 安全措施

### 1. 数据加密
```java
public class SecurityManager {
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    
    public void encryptData(String data) {
        // 加密实现
    }
}
```

### 2. 网络安全
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.ailianlian.com</domain>
        <pin-set>
            <!-- 证书指纹 -->
        </pin-set>
    </domain-config>
</network-security-config>
```

## 测试要求

### 1. 单元测试
```java
@RunWith(MockitoJUnitRunner.class)
public class ChatViewModelTest {
    @Mock
    private ChatRepository chatRepository;
    
    @Test
    public void sendMessage_Success() {
        // 测试实现
    }
}
```

### 2. UI测试
```java
@RunWith(AndroidJUnit4.class)
public class ChatActivityTest {
    @Rule
    public ActivityScenarioRule<ChatActivity> activityRule = 
        new ActivityScenarioRule<>(ChatActivity.class);
    
    @Test
    public void sendMessage_ShowsInList() {
        // UI测试实现
    }
}
```

## 部署要求

### 1. ProGuard配置
```proguard
-keep class com.ailianlian.ablecisi.model.** { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
```

### 2. 多渠道打包
```gradle
android {
    flavorDimensions "default"
    productFlavors {
        xiaomi { /* ... */ }
        huawei { /* ... */ }
        oppo { /* ... */ }
    }
}
```

## 附录

### 权限清单
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
</manifest>
```

### API文档
- 接口文档：[待补充]
- AI服务文档：[待补充]
- 设计资源：[待补充]

### 特殊交互说明
1. 聊天界面输入框动态高度调整
2. 消息列表滑动到底部自动加载历史
3. 图片消息预览和缩放
4. 语音消息录制动画效果

### 版本发布流程
1. 功能代码合并到develop分支
2. 执行自动化测试
3. 生成测试版本供测试团队验证
4. 确认无误后合并到master分支
5. 生成正式版本并提交应用市场

### 注意事项
1. 所有网络请求必须使用HTTPS
2. 敏感数据必须加密存储
3. 遵循Material Design设计规范
4. 保持代码注释的完整性
5. 定期进行性能检测和优化 
# HTML5 与 CSS3 基础

## 一、HTML5 语义化标签

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>语义化页面结构</title>
</head>
<body>
  <header>                    <!-- 页头 -->
    <nav>                     <!-- 导航 -->
      <ul>
        <li><a href="/">首页</a></li>
        <li><a href="/about">关于</a></li>
      </ul>
    </nav>
  </header>

  <main>                      <!-- 主内容区 -->
    <article>                 <!-- 独立内容 -->
      <section>               <!-- 内容分区 -->
        <h1>文章标题</h1>
        <p>正文内容...</p>
        <figure>              <!-- 图片+说明 -->
          <img src="img.png" alt="描述文字" />
          <figcaption>图片说明</figcaption>
        </figure>
      </section>
    </article>

    <aside>                   <!-- 侧边栏 -->
      <p>相关推荐</p>
    </aside>
  </main>

  <footer>                    <!-- 页脚 -->
    <p>&copy; 2025 教育平台</p>
  </footer>
</body>
</html>
```

### 常用表单元素（HTML5新增）
```html
<form action="/submit" method="POST">
  <input type="text"     name="username" placeholder="用户名" required />
  <input type="email"    name="email"    placeholder="邮箱" />
  <input type="password" name="pwd"      minlength="6" />
  <input type="number"   name="age"      min="0" max="150" />
  <input type="date"     name="birthday" />
  <input type="range"    name="score"    min="0" max="100" step="5" />
  <input type="search"   name="keyword"  />
  <input type="file"     name="avatar"   accept="image/*" />
  <textarea name="desc" rows="5" cols="40"></textarea>
  <select name="city">
    <option value="">请选择城市</option>
    <option value="bj">北京</option>
    <option value="sh">上海</option>
  </select>
  <button type="submit">提交</button>
</form>
```

---

## 二、CSS3 核心特性

### 1. 选择器
```css
/* 基础选择器 */
*         { box-sizing: border-box; }  /* 通配符 */
div       { color: #333; }             /* 元素 */
.card     { padding: 16px; }           /* 类 */
#header   { height: 60px; }           /* ID */

/* 关系选择器 */
.parent > .child   { /* 直接子元素 */ }
.prev + .next      { /* 紧邻兄弟 */ }
.el ~ .siblings    { /* 所有后续兄弟 */ }

/* 伪类 */
a:hover            { color: blue; }
input:focus        { outline: 2px solid blue; }
li:nth-child(2n)   { background: #f0f0f0; }  /* 偶数行 */
li:first-child     { font-weight: bold; }
li:last-child      { border-bottom: none; }

/* 伪元素 */
p::before          { content: "» "; color: blue; }
p::after           { content: ""; display: block; }
::placeholder      { color: #999; }
::selection        { background: yellow; }
```

### 2. 盒模型与布局
```css
/* 标准盒模型 vs border-box */
.box {
  box-sizing: border-box;   /* width包含padding和border */
  width: 300px;
  padding: 20px;
  border: 2px solid #ccc;
  margin: 10px auto;        /* 水平居中 */
}

/* Flexbox（一维布局）*/
.flex-container {
  display: flex;
  flex-direction: row;        /* row | column */
  justify-content: space-between; /* 主轴对齐 */
  align-items: center;        /* 交叉轴对齐 */
  flex-wrap: wrap;            /* 换行 */
  gap: 16px;
}
.flex-item {
  flex: 1;                    /* flex-grow: 1 */
  min-width: 200px;
}

/* Grid（二维布局）*/
.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);  /* 3等份列 */
  grid-template-rows: auto;
  gap: 20px;
}
.grid-item-wide {
  grid-column: span 2;        /* 跨2列 */
}
```

### 3. 动画与过渡
```css
/* 过渡 transition */
.btn {
  background: #2563eb;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.btn:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37,99,235,0.4);
}

/* 关键帧动画 @keyframes */
@keyframes slideIn {
  from { transform: translateX(-100%); opacity: 0; }
  to   { transform: translateX(0);     opacity: 1; }
}
.modal {
  animation: slideIn 0.4s ease-out forwards;
}

@keyframes pulse {
  0%   { transform: scale(1); }
  50%  { transform: scale(1.05); }
  100% { transform: scale(1); }
}
.loading { animation: pulse 1.5s infinite; }
```

### 4. 响应式设计
```css
/* 移动优先 */
.container { padding: 16px; }

/* 平板 768px+ */
@media (min-width: 768px) {
  .container { padding: 24px; max-width: 960px; margin: 0 auto; }
  .grid { grid-template-columns: repeat(2, 1fr); }
}

/* 桌面 1024px+ */
@media (min-width: 1024px) {
  .container { max-width: 1280px; }
  .grid { grid-template-columns: repeat(3, 1fr); }
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  body { background: #1a1a1a; color: #f0f0f0; }
}
```

### 5. CSS变量与现代特性
```css
:root {
  --primary: #2563eb;
  --secondary: #64748b;
  --radius: 8px;
  --shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card {
  background: white;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  color: var(--secondary);
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
```

## 知识点总结
- 语义化标签提升可读性、SEO和无障碍访问
- `box-sizing: border-box` 是现代布局的标准做法
- Flexbox适合一维（行/列）布局，Grid适合二维布局
- transition用于状态切换，@keyframes用于复杂动画
- 响应式设计优先考虑移动端，使用媒体查询适配大屏

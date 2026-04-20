# JavaScript ES6+ 核心特性

## 1. 变量声明
```javascript
// let - 块级作用域，可修改
let count = 0;
count = 1;

// const - 块级作用域，不可重新赋值（对象/数组内容可变）
const PI = 3.14159;
const user = { name: '张三' };
user.name = '李四';  // ✓ 可以修改属性
// user = {};         // ✗ 不能重新赋值

// 暂时性死区：let/const不存在变量提升
```

## 2. 解构赋值
```javascript
// 数组解构
const [a, b, ...rest] = [1, 2, 3, 4, 5];
// a=1, b=2, rest=[3,4,5]

// 对象解构
const { name, age = 18, role: userRole } = { name: '张三', role: 'teacher' };
// name='张三', age=18(默认值), userRole='teacher'(重命名)

// 函数参数解构
function greet({ name, greeting = '你好' }) {
    return `${greeting}, ${name}!`;
}
greet({ name: '李四' });  // "你好, 李四!"

// 嵌套解构
const { data: { token, user: { id, username } } } = response;
```

## 3. 模板字符串
```javascript
const name = '张三';
const score = 95;
const msg = `同学 ${name} 的成绩是 ${score} 分，${score >= 90 ? '优秀' : '良好'}！`;

// 多行字符串
const html = `
  <div class="card">
    <h2>${name}</h2>
    <p>分数: ${score}</p>
  </div>
`;

// 标签模板（高级）
function highlight(strings, ...values) {
    return strings.reduce((acc, str, i) =>
        acc + str + (values[i] !== undefined ? `<b>${values[i]}</b>` : ''), '');
}
highlight`姓名: ${name}, 成绩: ${score}`;
```

## 4. 箭头函数
```javascript
// 普通函数 vs 箭头函数
const add = (a, b) => a + b;           // 单行表达式，隐式返回
const square = x => x * x;             // 单参数可省略括号
const getUser = () => ({ id: 1 });     // 返回对象需加括号

// 箭头函数没有自己的 this
class Timer {
    constructor() { this.seconds = 0; }
    start() {
        setInterval(() => {            // this指向Timer实例
            this.seconds++;
            console.log(this.seconds);
        }, 1000);
    }
}
```

## 5. Promise 与 async/await
```javascript
// Promise 基础
const fetchData = (id) => new Promise((resolve, reject) => {
    setTimeout(() => {
        id > 0 ? resolve({ id, name: '张三' }) : reject(new Error('ID无效'));
    }, 1000);
});

// Promise链式调用
fetchData(1)
    .then(user => { console.log(user); return user.id; })
    .then(id => fetchData(id + 1))
    .catch(err => console.error('错误:', err.message))
    .finally(() => console.log('完成'));

// Promise.all — 并发执行
Promise.all([fetchData(1), fetchData(2), fetchData(3)])
    .then(([u1, u2, u3]) => console.log(u1, u2, u3));

// async/await（推荐写法）
async function loadUserProfile(userId) {
    try {
        const user = await fetchData(userId);
        const posts = await fetchPosts(user.id);   // 串行
        return { user, posts };
    } catch (err) {
        console.error('加载失败:', err);
        throw err;
    }
}

// 并发优化
async function loadAll(ids) {
    const users = await Promise.all(ids.map(id => fetchData(id)));
    return users;
}
```

## 6. 模块化（ESM）
```javascript
// utils.js — 命名导出
export const add = (a, b) => a + b;
export function formatDate(date) {
    return date.toLocaleDateString('zh-CN');
}
export const PI = 3.14159;

// math.js — 默认导出
export default class Calculator {
    add(a, b) { return a + b; }
    sub(a, b) { return a - b; }
}

// main.js — 导入
import Calculator from './math.js';        // 默认导入
import { add, formatDate } from './utils.js'; // 命名导入
import * as utils from './utils.js';        // 命名空间导入
import { add as sum } from './utils.js';    // 重命名导入
```

## 7. 展开运算符与剩余参数
```javascript
// 展开数组
const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];
const merged = [...arr1, ...arr2];              // [1,2,3,4,5,6]
const copy = [...arr1];                         // 浅拷贝

// 展开对象
const defaults = { theme: 'light', lang: 'zh' };
const config = { ...defaults, lang: 'en', debug: true };
// { theme:'light', lang:'en', debug:true }

// 剩余参数
function sum(...nums) {
    return nums.reduce((acc, n) => acc + n, 0);
}
sum(1, 2, 3, 4, 5);  // 15
```

## 8. Map / Set
```javascript
// Set — 唯一值集合
const set = new Set([1, 2, 2, 3, 3, 3]);
console.log([...set]);  // [1, 2, 3]
set.add(4); set.delete(1); set.has(2);  // true

// 数组去重
const unique = [...new Set(arr)];

// Map — 键值对，键可以是任意类型
const map = new Map();
map.set('key', 'value');
map.set({ id: 1 }, 'user object as key');
map.get('key');   // 'value'
map.size;         // 2

// 遍历
for (const [key, value] of map) {
    console.log(key, '->', value);
}
```

## 9. 可选链与空值合并
```javascript
const user = { profile: { address: { city: '北京' } } };

// 可选链 ?.  — 避免层层判断
const city = user?.profile?.address?.city;    // '北京'
const zip = user?.profile?.address?.zip;      // undefined（不报错）
const len = user?.hobbies?.length;            // undefined

// 空值合并 ?? — 只有null/undefined时使用默认值
const name = user.name ?? '匿名用户';         // '匿名用户'
const age = user.age ?? 0;                    // 0

// 对比 || 的区别
const val1 = 0 || '默认';    // '默认' (0是falsy)
const val2 = 0 ?? '默认';    // 0      (0不是null/undefined)
```

## 知识点总结
- `let/const`替代`var`，避免变量提升和全局污染
- 解构赋值大幅简化属性提取，配合默认值和重命名更灵活
- `async/await`是处理异步的最清晰写法，本质是Promise语法糖
- ESM模块化是浏览器和Node.js的现代标准，替代CommonJS
- 可选链`?.`和空值合并`??`是防御性编程的利器

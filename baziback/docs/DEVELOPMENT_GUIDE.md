# 开发指南

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 5.7+
- Android Studio (移动端开发)

### 初始化项目

```bash
# 1. 克隆项目
git clone <repository-url>
cd baziback

# 2. 安装后端依赖
mvn clean install

# 3. 安装前端依赖
npm install

# 4. 初始化数据库
mysql -u root -p < database/init_user_table.sql
mysql -u root -p < database/add_credit_system_tables.sql
mysql -u root -p < database/add_referral_system_tables.sql

# 5. 配置数据库连接
# 编辑 src/main/resources/application.yml
```

---

## 📝 开发规范

### 代码风格

#### Java 后端
- 使用 Lombok 简化代码
- 遵循 Spring Boot 最佳实践
- 使用 `@RestController` 和 `@Service` 注解
- 统一使用 `Result` 类返回结果

#### React 前端
- 使用函数式组件和 Hooks
- 组件文件使用 PascalCase
- 工具函数使用 camelCase
- 使用 ESLint 和 Prettier

### Git 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

---

## 🔧 开发流程

### 1. 创建新功能

#### 后端
```bash
# 1. 创建实体类
src/main/java/com/example/demo/entity/NewEntity.java

# 2. 创建 Mapper
src/main/java/com/example/demo/mapper/NewMapper.java

# 3. 创建 Service
src/main/java/com/example/demo/service/NewService.java

# 4. 创建 Controller
src/main/java/com/example/demo/controller/NewController.java
```

#### 前端
```bash
# 1. 创建页面组件
src-frontend/pages/NewPage.jsx

# 2. 创建业务组件
src-frontend/components/NewComponent.jsx

# 3. 添加 API 接口
src-frontend/api/index.js

# 4. 添加路由
src-frontend/App.jsx
```

### 2. 数据库变更

```bash
# 1. 创建 SQL 脚本
database/add_new_feature_tables.sql

# 2. 更新实体类
src/main/java/com/example/demo/entity/

# 3. 更新文档
docs/DATABASE_SCHEMA.md
```

---

## 🧪 测试

### 后端测试
```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=UserServiceTest
```

### 前端测试
```bash
# 运行测试
npm test

# 运行测试并生成覆盖率
npm test -- --coverage
```

---

## 📦 构建

### 后端构建
```bash
# 打包
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests

# 运行
java -jar target/bazi-0.0.1-SNAPSHOT.jar
```

### 前端构建
```bash
# 开发模式
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview
```

---

## 🐛 调试

### 后端调试
- 使用 IDE 断点调试
- 查看日志：`logs/application.log`
- 日志级别：`src/main/resources/application.yml`

### 前端调试
- 使用浏览器开发者工具
- React DevTools 扩展
- 查看控制台日志

---

## 📚 参考资源

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [React 文档](https://react.dev)
- [Vite 文档](https://vitejs.dev)
- [Capacitor 文档](https://capacitorjs.com)

---

**最后更新**：2025-01-08

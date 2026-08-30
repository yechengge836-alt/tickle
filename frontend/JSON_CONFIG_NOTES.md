# 前端 JSON 配置逐项说明

JSON 标准不允许写注释；为了保持 package.json 与 tsconfig 文件能被 Vite、TypeScript 正常读取，逐项说明放在本文件。

## package.json

- name：前端包名称。
- private：阻止误发布到 npm。
- version：包版本。
- type：采用 ES Module 语法。
- scripts.dev：启动 Vite 开发服务器。
- scripts.build：先做 TypeScript 项目检查，再构建静态资源。
- scripts.preview：预览构建后的静态资源。
- dependencies：声明 React、Vite 和图标库等运行依赖。
- devDependencies：声明 React 类型与 TypeScript 编译器等开发依赖。

## tsconfig.json

- files：不直接指定单个入口文件。
- references：引用浏览器端和 Node/Vite 配置两套 TypeScript 项目。

## tsconfig.app.json

- tsBuildInfoFile：增量构建缓存文件位置。
- target、lib：浏览器端代码采用的 JavaScript 与 Web API 标准。
- strict：开启严格类型检查。
- module、moduleResolution：使用 Vite 推荐的 ESNext/Bundler 模式。
- noEmit：只做类型检查，构建产物交给 Vite。
- jsx：使用 React 自动 JSX 运行时。
- include：只检查 src 目录源码。

## tsconfig.node.json

- tsBuildInfoFile：Vite 配置项目的增量缓存。
- target、lib：配置文件运行在 ES2023 环境。
- module、moduleResolution：使用 ESNext 和 Bundler 解析规则。
- noEmit：只类型检查，不生成 JS。
- include：只检查 vite.config.ts。

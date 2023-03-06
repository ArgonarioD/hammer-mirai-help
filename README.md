<div align="center">

<img src="https://docs.hammer-hfut.tk:233/logo.svg" alt="logo" height="200">

# Hammer Mirai Help
一个用以组织 Mirai Console 装载的插件们的帮助信息的 Mirai Console 插件。

为 Mirai Console 的用户提供统一的、简单的、可高度自定义的帮助信息展示方式。

为 Mirai Console 的插件开发者提供简单且统一的帮助信息展示方式和接口。

<a href="https://raw.githubusercontent.com/ArgonarioD/hammer-mirai-help/main/LICENSE">
   <img src="https://img.shields.io/badge/license-AGPL--3.0-orange" alt="license">
</a>
<img src="https://img.shields.io/badge/JVM-11%2B-blue" alt="JVM 11+">
</div>

## 功能
 - 安装新插件后，用户只需要执行一行指令即可自动配置该插件的帮助信息：
   - 对于接入本插件接口的插件，本插件会按照其配置的帮助信息进行配置；
   - 对于未接入本插件接口的插件，本插件会依据其注册的指令自动生成帮助信息。
 - 对于用户来说，只需要通过修改配置文件就可以修改如下的信息：
   - 触发帮助的指令前缀（可以使用正则表达式匹配）；
   - 指定插件的帮助信息是否启用；
   - 指定插件的名称以及别名；
   - 指定插件的详细帮助信息内容（可以设置为图片）；
   - 用户想要看到指定插件的帮助信息所需要的权限（可以为空）。
 - 对于插件开发者来说，接入本插件十分简单：
   - 对于 JVM 插件，通过本插件提供的 SDK 在插件代码添加几行代码即可进行配置；
   - 对于其他插件，只需要在指定目录生成一个配置文件即可。
 - 无插件依赖要求，接入本插件的接口后不安装本插件无法使用 `help` 功能，但可以正常运行。

## 快速开始
### 安装
#### 手动安装
在右侧的 `Release` 处下载最新的 `.jar` 文件，将其放入 `plugins` 目录下，重启 Mirai Console 。
#### 通过 Mirai Console Loader 安装
暂未接入，敬请期待。
### 第一次启动
使用指令
```
/h-help loadDefaults
```
看到成功提示后，在 Bot 所在的任意一个QQ群聊中输入
```
help
```
即可看到帮助信息。

## 详细文档
 - [GitHub Wiki](https://github.com/ArgonarioD/hammer-mirai-help/wiki/%E4%B8%BB%E9%A1%B5)（包含用户手册和开发者手册）

## 鸣谢
 - [Mirai](https://github.com/mamoe/mirai)
 - [nonebot-plugin-help](https://github.com/XZhouQD/nonebot-plugin-help): 本插件的灵感来源
---
   ~~*如果觉得有用的话求点个Star啵QwQ*~~
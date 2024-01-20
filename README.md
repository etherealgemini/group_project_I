# AutoGen - Ai extension of Evosuite

## 简介

通过用户的输入——项目（assignment）介绍文档PDF与Java源代码，即可自动化生成单测，并自动地通过ChatGPT进行测试加强。

实现了端到端的操作，内部对用户是黑箱状态——用户不需要知道里面发生了什么。

本项目运行于**Java 11**

## 使用技术

调用了JaCoCo Api用于覆盖率测试；

使用了ChatGPT-Java框架用于GPT对话。

项目中实现了：

1. 简单的PDF转录。
2. 字符串的运行时编译。
3. 自定义的类加载器，用于JaCoCo的调用。
4. 异步接收子进程输出流，防止子进程因输出流缓存限制异常阻塞。

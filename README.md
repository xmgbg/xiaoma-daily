<div align="center">
  <img src="app/src/main/res/drawable-nodpi/launcher_art.png" alt="小马日报 Logo：小马头像与日历" width="220" />

  <h1>小马日报</h1>
  <p><strong>把每一天，认真记下来。</strong></p>
  <p>一款离线使用的原生 Android 日报应用 · 自由文本记录 · 月历回顾</p>

  <p>
    <img src="https://img.shields.io/badge/Java-17-416D5A?style=flat-square&amp;logo=openjdk&amp;logoColor=white" alt="Java 17" />
    <img src="https://img.shields.io/badge/Android-Views%20%2B%20XML-416D5A?style=flat-square&amp;logo=android&amp;logoColor=white" alt="Android Views + XML" />
    <img src="https://img.shields.io/badge/Room-2.7.2-416D5A?style=flat-square&amp;logo=sqlite&amp;logoColor=white" alt="Room 2.7.2" />
    <img src="https://img.shields.io/badge/minSdk-24-416D5A?style=flat-square&amp;logo=android&amp;logoColor=white" alt="最低 Android API 24" />
  </p>
</div>

---

按日期记录自由文本，一天一篇；在月历中查看已写日期，点击日期阅读、编辑或删除当天的日报。数据保存在设备本地，无需账号或网络。

## 项目架构

```text
app/src/main/
├── java/com/xiaoma/daily/
│   ├── ui/       首页月历、日报详情、编辑页、字号设置
│   ├── data/     Room 实体、DAO、数据库
│   └── util/     日期解析与月份计算
└── res/          XML 布局、颜色、文案、启动图标
```

页面由 `MainActivity`、`DailyDetailActivity`、`EditDailyActivity` 负责展示和导航。日报通过 `AppDatabase` 的单线程执行器在后台读写 Room；页面返回前台后重新查询，更新正文和月历标记。每个日期最多保存一篇日报，日期在数据库中有唯一约束。字号分小、标准、大、特大四档，设置保存在本机。

目前保持轻量的 Activity + Room 结构，尚未引入 ViewModel 或 Repository。

## 当前进度

- [x] 首页月历、月份切换、点击日期进入详情；点击月份标题返回今天。
- [x] 自由文本日报的新建、阅读和编辑；数据写入本地 Room 数据库。
- [x] 已写日期标记和本月记录天数。
- [x] 四档字号调节及本地记忆。
- [x] 删除日报前二次确认。
- [x] 小马日历图案的应用启动图标，支持浅色/深色页面配色。
- [ ] 全量 JSON 导出与导入。
- [ ] 编辑未保存时的离开提示，以及真机/模拟器完整流程验收。

当前 Debug APK 已通过构建、单元测试和 Lint；Room 的安卓端测试已编译，但尚未连接设备运行。应用仍处于开发阶段。

## 本地构建

用 Android Studio 打开项目，配置 JDK 17 和 Android SDK Platform 35；或设置 `ANDROID_HOME` 后在 Windows PowerShell 执行：

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Debug APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

`react-native-admobile` 是 ADSuyi 广告 SDK 的 React Native 桥接库，而不是可独立运行的应用。它同时提供命令式原生模块方法和原生视图管理器，支持开屏、插屏、激励视频、Banner 以及信息流广告。Android 使用 Java 实现，iOS 使用 Objective-C 实现；两个平台的行为和事件名称并不完全一致。

## 常用命令

```bash
# 安装开发依赖（typescript 当前通过传递依赖提供）。
yarn install

# 原地编译 index.ts；生成的 index.js 是包的入口文件。
yarn build
# 当 node_modules/.bin 不在 PATH 中时，可使用：
npx tsc

# 基于 conventional commits 生成变更日志、版本提交和标签。
yarn release:fix       # 修订版本（patch）
yarn release:feat      # 次版本（minor）
yarn release:major     # 主版本（major）
yarn release:fix_beta  # 使用 beta 标识的修订预发布版本
```

- `index.ts` 是 TypeScript 源文件，`index.js` 是被 Git 跟踪并发布的构建产物。修改 `index.ts` 后，需要执行构建，并检查、提交重新生成的 `index.js`。`dist/index.js` 是旧模块配置留下的过期产物；`package.json` 发布的是根目录的 `index.js`，而不是 `dist/` 中的文件。
- `yarn test` 只是一个会主动失败的占位命令。项目没有配置 lint 命令或测试运行器，因此也没有运行单个测试的命令。
- 仓库中没有 Gradle Wrapper、宿主项目的 `settings.gradle`、CocoaPods `Podfile` 或完整可运行的示例应用。原生改动应在引入此包的 React Native 宿主应用中编译并进行行为验证。`example/App.js` 只是过时的代码片段，不应视为可运行的示例。
- 如需将 Android 库单独发布为 Maven 依赖，请按照 `android/README.md` 操作：在 `android/local.properties` 中配置 SDK/NDK 路径，然后在 `android/` 目录执行 `./gradlew installArchives`；该流程要求此目录中存在 Gradle Wrapper。

## 代码架构

### JavaScript 与原生的接口约定

`index.ts` 封装了 `NativeModules.ReactNativeAdmobile`。其公开名称属于兼容性 API，尤其是 `intertitialAd` 的拼写错误在 JS、Android 和 iOS 中被一致保留。`initAd` 返回原生 Promise，而开屏、激励和插屏方法使用成功/失败回调。

修改此 API 时，需要同步维护各端实现：

- Android：`ReactNativeAdmobileModule` 实现 `@ReactMethod` 方法；`ReactNativeAdmobilePackage` 注册该模块及全部三个视图管理器。
- iOS：`ReactNativeAdmobile.m` 导出桥接方法，`ReactNativeAdmobile+{SplashVod,ReWardVod,IntertitialAd,NativeVod}.m` 分类实现 SDK 加载和代理回调。分类中的状态通过 Objective-C associated objects 保存。

### 基于视图的广告

包内直接注册原生组件名称；由于 `index.ts` 没有导出 React 组件，使用方需要通过 `requireNativeComponent` 获取组件：

| JS 组件名称 | Android 实现 | iOS 实现 |
| --- | --- | --- |
| `RNBannerAD` | `RNBannerAd` + `views/RNBannerView` | `RNBannerADManager` + `RNAdmobileBanner` |
| `RNNativeAD` | `RNFlowExpressAd` + `views/RNFlowExpressAdView` | `RNNativeADManager`，展示由命令式 `nativeAd` 流程加载的视图 |
| `RNNativeFeedAD` | `RNNativeFeedAd` + `views/RNNativeFeedAdView` | `RNNativeFeedADManager`（在视图内部完成加载和渲染） |

设置 `posId` 会触发广告加载，因此属性设置顺序和重新加载行为很重要。Android 视图管理器会将 `adWidth`、`adHeight` 和 `maxMediaHeight` 从 dp 转换为 px。信息流视图同时支持自渲染和模板广告，并在渲染完成后将测量尺寸返回给 JS；布局调整通常需要同时检查管理器、视图实现以及使用方基于事件更新尺寸的逻辑。

事件采用两种不同的传递机制：

- Android 信息流视图发送针对单个视图的直接事件（`topNativeRenderSuccess`、`topNativeFail`、`topNativeClose`），对 JS 分别导出为 `onNativeRenderSuccess`、`onNativeFail` 和 `onNativeClose`。
- Android Banner 和激励广告使用全局 `DeviceEventManagerModule` 事件；激励事件名为 `RewardAdEvent`，并携带 `eventType` 字段。
- iOS 使用单例 `RCTEventEmitter` 子类发送 Banner/信息流事件。`RNNativeFeedADEmitter` 广播 `nativeFeedAdRenderSuccess`、`nativeFeedAdFail` 和 `nativeFeedAdClose`，这与 Android 的单视图事件机制不同。

旧的 iOS `nativeAd` 流程跨越多个文件：`ReactNativeAdmobile+NativeVod.m` 负责加载和渲染广告，将生成的视图保存到 `RNNativeADShareInstance`，再由 `RNNativeADManager` 把共享视图返回给 React Native。修改此流程时，需要保留先加载、后挂载的顺序。较新的 `RNNativeFeedAD` 流程则直接在自身视图中加载广告。

Android 开屏广告会启动 `SplashAdActivity`；广告完成或失败后，通过 `initAd` 期间安装的静态 `AdCallbackUtils` 回调返回到模块。`views/` 中针对广告 SDK 的布局和滚动处理是有意保留的集成逻辑，包括 `Utils.setupLayoutHack` 和触摸拦截处理，不应将其当作普通冗余代码删除。

## 原生依赖与集成

- iOS 依赖声明在 `react-native-admobile.podspec` 中；podspec 会包含全部 `ios/` 源文件及已启用的 ADSuyi 平台适配器。Android 依赖和适配器位于 `android/build.gradle`。修改 SDK 或适配器版本时，通常需要同时检查这两个文件，即使两端的具体版本号不同。
- `android/libsAdmobile/oaid_sdk_1.0.25.aar` 是被 Git 跟踪的二进制文件，通过 `:libsAdmobile` 子项目提供。宿主 Android 项目必须包含该子项目，否则 `implementation project(path: ':libsAdmobile')` 无法解析。
- 宿主 Android 项目需要配置 ADSuyi Maven 仓库，以及 `README.md` 中列出的已启用第三方适配器仓库。OAID 还要求宿主应用提供 `assets/supplierconfig.json`；README 还包含必要的 ProGuard 规则、权限和 ABI 限制（支持 `armeabi-v7a` 与 `arm64-v8a`，不支持 x86）。
- iOS 开屏广告加载要求宿主应用提供 `SharedResources.bundle`（iPad 上为 `SharedResources_pad.bundle`），其中需要包含 `launch` 图片。
- ADSuyi 平台文档链接见 `README.md`：https://doc.admobile.top/ssp/pages/suyisdkand/
- 升级原生 SDK 或检查桥接兼容性时，以以下官方 ADSuyi Demo 为准，不要使用其他同名旧仓库：
  - Android：https://gitee.com/admobile_top/ADSuyiSdkDemo-Android
  - iOS Objective-C：https://gitee.com/admobile_top/ADSuyiDemo-iOS-OC
  - 天目单 SDK Demo（仅供核对天目底层 API，不代表本库要切换为单 SDK）：https://gitee.com/admobile_top/tianmudemo_android 和 https://gitee.com/admobile_top/tianmudemo-ios-oc

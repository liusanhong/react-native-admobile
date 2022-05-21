# react-native-admobile

## Getting started

```

$ npm install react-native-admobile --save
或
$ yarn add react-native-admobile
```
```
Android:
设置oaid 
将Demo中assets文件夹下的supplierconfig.json文件复制到自己的assets目录下并按照supplierconfig.json文件中的说明进行OAID的 AppId 配置，supplierconfig.json文件名不可修改。需要设置 appid 的部分需要去对应厂商的应用商店的应用信息中查看。；

添加以下混淆配置：
-keep class com.bun.miitmdid.core.** {*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}

修改AndroidManifest.xml，OAID SDK minSdkVersion为21，如果应用的minSdkVersion小于21，则添加：
// 如果导入后有冲突可以不添加，suyi中已经添加过了
<uses-sdk tools:overrideLibrary="com.bun.miitmdid"/>

```

```
 权限：
<!-- 广告必须的权限，允许网络访问 -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- 广告必须的权限，允许安装未知来源权限（如下载类广告下载完成后唤起安卓） -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<!-- 广告必须的权限，地理位置权限，获取位置信息，用于广告投放。精准广告投放及反作弊 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 如果有视频相关的广告播放请务必添加，屏幕保持唤醒不锁屏（部分渠道未添加该权限时会出现视频类广告黑屏）-->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- 如果接入了广点通渠道，必须加入以下权限，不然会导致广点通填充失败 -->
<!-- 允许应用获取 MAC 地址。广告投放及广告监测归因、反作弊 -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<!-- 允许应用检测网络状态，SDK 会根据网络状态选择是否发送数据 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 影响广告填充，强烈建议的权限，获取设备信息，允许应用获取手机状态（包括手机号码、IMEI、IMSI权限等）。广告投放及广告监测归因、反作弊 -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

<!-- 为了提高广告收益，建议设置的权限，写入权限，用于下载类广告数据写入 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!-- 为了提高广告收益，建议设置的权限，读取权限，用于下载类广告数据读取（如判断是否已下载过该APK，避免重复下载）-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- 为了提高广告收益，建议设置的权限，获取粗略位置信息。精准广告投放及反作弊 -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### Mostly automatic installation

`$ react-native link react-native-admobile`

## Usage
```javascript
import ReactNativeAdmobile from 'react-native-admobile';

// TODO: What to do with the module?


ReactNativeAdmobile;
```

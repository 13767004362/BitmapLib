# BitmapLib
一个异步图片库，支持加载网络，磁盘，Resource，assets资源，适配滚动列表(RecyclerView)。

目前，本项目中已经支持的功能：
- **异步加载**
- **计算合适比率，适屏加载**
- **多级缓，内存，磁盘，原始资源（resource,file,asserts,后台服务器）**
- **滑动列表，并发问题**
- **重试机制**
- **网络图片过期问题**


#### **前期准备**

**添加依赖**

在项目build.gradle中添加依赖:
```java
 compile 'com.xingen:bitmapLib:1.0.1'
```
**添加权限**

添加联网权限
```java
    <uses-permission android:name="android.permission.INTERNET"></uses-permission>
```
若是考虑图片缓存到sdcard中，需要添加读写权限：
```java
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
```

#### **使用介绍**


**1.初始化**：
```java
   ImageLoader.getInstance().init(this);
```
2. 调用loadXXX()，加载网络图片，图片文件，assets中图片，resource中图片




**后期考虑的问题**：

- 支持切换传输层
- 重选设计模式，采用Builder模式，简化调用API.

License
-------

    Copyright 2018 HeXinGen.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

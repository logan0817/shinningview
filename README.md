# 英文文档 [English Document](./README_EN.md)

# 中文

### 说明

本项目是在Android实现光影移动效果【流光效果】

- 背景闪烁（流动光）
- 文字闪烁（高光扫掠）

### 效果如下

<img src="img%2FShimmerTextView.gif" width="288" height="640"/> <img src="img%2FShimmerView.gif" width="288" height="640"/>

### DEMO

1. Demo.apk [点击下载](apk/app-debug.apk)

### 如果网络访问github加载不出来图片效果，国内用户可以访问该地址： [https://blog.csdn.net/notwalnut/article/details/136044650](https://blog.csdn.net/notwalnut/article/details/136044650)

## 引入

### Gradle:

1. 在Project的 **build.gradle** 或 **setting.gradle** 中添加远程仓库

    ```gradle
    repositories {
        //...
        mavenCentral()
    }
    ```

2. 在Module的 **build.gradle** 中添加依赖项
   [![Maven Central](https://img.shields.io/maven-central/v/io.github.logan0817/shinningview.svg?label=Latest%20Release)](https://central.sonatype.com/artifact/io.github.logan0817/shinningview)

    ```gradle
   implementation 'io.github.logan0817:shinningview:1.0.2' // 替换为上方徽章显示的最新版本
    ```

## 控件参数及含义

| *参数名*       |    *参数取值* |                         *参数含义*                         |
|-------------|----------:|:------------------------------------------------------:|
| svAnimMode  |      enum |                  自动还是手动【auto、manual】                   |
| svWidth     | dimension |                          光影宽度                          |
| svSlope     |     float |                    光影斜率 范围【-1 ~ 1】                     |
| svRepeat    |   integer |                   -1:无限循环，其他代表重复执行几次                   |
| svDuration  |   integer |                       动画时长 单位ms                        |
| svColors    |    string |      颜色值 举例：{0x00FFFFFF, 0x88FFFFFF, 0x00FFFFFF}       |
| svPositions |    string | 颜色值对应的位置数组  (值范围0~1) 举例：[0f,0.5f,1f]  与csAngle数组大小必须一致 |
| svRadius    | dimension |                控件的圆角大小,仅仅ShimmerView支持                 |

## 背景流光效果效果使用：ShimmerView

    <com.logan.shinningview.ShimmerView
        android:id = "@+id/view1"
        android:layout_width = "match_parent"
        android:layout_height = "200dp"
        android:background = "@color/black" />

    <com.logan.shinningview.ShimmerView
        android:id = "@+id/view2"
        android:layout_width = "match_parent"
        android:layout_height = "200dp"
        android:background = "@color/black"
        app:svAnimMode = "auto"
        app:svColors = "#00FFFFFF,#5AFFFFFF,#00FFFFFF"
        app:svDuration = "2000"
        app:svPositions = "0,0.5,1"
        app:svRadius = "20dp"
        app:svRepeat = "-1"
        app:svSlope = "-1"/>

## 文字流光效果效果使用：ShimmerTextView

    <com.logan.shinningview.ShimmerTextView
        android:id = "@+id/view1"
        android:layout_width = "wrap_content"
        android:layout_height = "wrap_content"
        android:text = "@string/app_name"
        android:textSize = "37sp" />
    
    <com.logan.shinningview.ShimmerTextView
        android:id = "@+id/view2"
        android:layout_width = "wrap_content"
        android:layout_height = "wrap_content"
        android:text = "@string/app_name"
        android:textSize = "37sp"
        app:svAnimMode = "auto"
        app:svColors = "#FF00FF,#116600,#FF00FF"
        app:svDuration = "2000"
        app:svPositions = "0,0.5,1"
        app:svRepeat = "-1"
        app:svSlope = "-1" />

## xml配置不能满足你的需求，可以使用代码控制这些参数和动画，View都封装了一些对应的公开方法可以调用。



### 如果你有任何疑问可以留言。

### 如果这篇文章对你有帮助，可以赏个star支持一下作者。

### License

```
MIT License

Copyright (c) 2025 Logan Gan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

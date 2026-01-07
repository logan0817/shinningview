# English Document

# Android Shimmer Effect Library

## Introduction

This project implements **shimmer / flowing light effects** on Android, including:

- Background shimmer (flowing light)
- Text shimmer (highlight sweep)

## Preview

<img src="img%2FShimmerTextView.gif" width="288" height="640"/> <img src="img%2FShimmerView.gif" width="288" height="640"/>

## Demo

1. Demo.apk — [Download](apk/app-debug.apk)

> If images fail to load on GitHub due to network restrictions, users in mainland China can view the preview here:  
> https://blog.csdn.net/notwalnut/article/details/136044650

---

## Dependency

### Gradle

1. Add Maven Central to your **project-level** build.gradle or settings.gradle

    ```gradle
    repositories {
        //...
        mavenCentral()
    }

2. Add the dependency in your **module-level** build.gradle    [![Maven Central](https://img.shields.io/maven-central/v/io.github.logan0817/shinningview.svg?label=Latest%20Release)](https://central.sonatype.com/artifact/io.github.logan0817/shinningview)


    ```gradle
   implementation 'io.github.logan0817:shinningview:1.0.2'   // Replace with the latest version shown on Maven Central
    ```

## Attributes

| *Attribute*  | *Type*      |                                  *Description*                                  |
|-------------|-----------:|:-------------------------------------------------------------------------------:|
| svAnimMode  | enum      |                          Animation mode: 【auto、manual】                          |
| svWidth     | dimension |                         Width of the shimmer highlight                          |
| svSlope     | float     |                          Shimmer slope, range 【-1 ~ 1】                          |
| svRepeat    | integer   |                  -1 for infinite loop, otherwise repeat count                   |
| svDuration  | integer   |                       Animation duration in milliseconds                        |
| svColors    | string    |             Color values, e.g. {0x00FFFFFF, 0x88FFFFFF, 0x00FFFFFF}             |
| svPositions | string    |  Positions for each color (0~1), must match csColors length, e.g. [0f,0.5f,1f]  |
| svRadius    | dimension |               Corner radius of the view ,only support ShimmerView               |



## Background Shimmer Effect: ShimmerView

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


## Text Shimmer Effect: ShimmerTextView

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


## If XML configuration cannot meet your needs, you can use code to control these parameters and animations. View encapsulates some corresponding public methods that can be called.

## Support

- If you have any questions, feel free to leave a comment.
- If this project helps you, please consider giving it a ⭐ Star to support the author.

---

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

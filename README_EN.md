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

2. Add the dependency in your **module-level** build.gradle

    ```gradle
   implementation 'io.github.logan0817:shinningview:1.0.0'  
   // Replace with the latest version shown on Maven Central
    ```
---

## Background Shimmer Effect: ShimmerView

    <com.logan.shinningview.ShimmerView
        android:id="@+id/view1"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_marginTop="50dp"
        android:background="@color/black"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/button_first" />

    <com.logan.shinningview.ShimmerView
        android:id="@+id/view2"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_marginTop="16dp"
        android:background="@color/black"
        app:csColors="#00FFFFFF,#5AFFFFFF,#00FFFFFF"
        app:csPositions="0,0.5,1"
        app:csRepeat="-1"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/view1" />

### Attributes

| Attribute | Type | Description                                                 |
|----------|------|-------------------------------------------------------------|
| csAnimMode | enum | Animation mode: 【auto、manual】                               |
| csWidth | dimension | Width of the shimmer highlight                              |
| csSlope | float | Shimmer slope, range 【-1 ~ 1】                               |
| csRadius | dimension | Corner radius of the view                                   |
| csRepeat | integer | -1 for infinite loop, otherwise repeat count                |
| csDuration | integer | Animation duration in milliseconds                          |
| csColors | string | Color values, e.g. {0x00FFFFFF, 0x88FFFFFF, 0x00FFFFFF}     |
| csPositions | string | Positions for each color (0~1), must match csColors length, e.g. [0f,0.5f,1f] |

---

## Text Shimmer Effect: ShimmerTextView

    <com.logan.shinningview.ShimmerTextView
        android:id="@+id/view1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="50dp"
        android:text="@string/app_name"
        android:textSize="37sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/button_Second" />

    <com.logan.shinningview.ShimmerTextView
        android:id="@+id/view2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="@string/app_name"
        android:textSize="37sp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/view1"
        app:stvShimmerColor="#FF00FF"
        app:stvTextColor="#116600" />

### Attributes

| Attribute | Type | Description |
|----------|------|-------------|
| stvTextColor | color | Base / primary text color. Also used as the start & end color of the shimmer gradient |
| stvShimmerColor | color | Highlight color in the center of the shimmer effect |

---

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

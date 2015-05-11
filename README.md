# ProgressHUD #

[![Build Status](https://travis-ci.org/seespace/progresshud.svg?branch=develop)](https://travis-ci.org/seespace/progresshud)

ProgressHUD is a lightweight and easy-to-use HUD for InAiR.

### Download ###

Download the latest [AAR](https://raw.githubusercontent.com/seespace/maven/master/co/seespace/extra/progresshud/1.0.1/progresshud-1.0.1.aar) or grab via Gradle:

```groovy
repositories {
  maven {
    url "https://raw.githubusercontent.com/seespace/maven/master/"
  }
}

dependencies {
  compile 'co.seespace.extra:progresshud:1.0.1'
}
```

or Maven:

```xml
<!-- repository -->
<repositories>
  <repository>
    <id>seespace-mvn-repo</id>
    <url>https://raw.githubusercontent.com/seespace/maven/master/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<!-- dependency -->
<dependency>
  <groupId>co.seespace.extra</groupId>
  <artifactId>progresshud</artifactId>
  <version>1.0.1</version>
</dependency>
```

### Usage ###

#### Show a single loader indicator ####

```java
UIProgressHUD.with(context)
  .show();
```

#### Show loader indicator with a message ####

```java
UIProgressHUD.with(context)
  .show("Logging In…");
```

Alternatively, you can use [Android String Resources](http://developer.android.com/guide/topics/resources/string-resource.html):

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="logging_in">Logging In…</string>
</resources>
```

```java
UIProgressHUD.with(context)
  .show(R.string.logging_in);
```

#### Show custom Drawable ####

```java
Drawable customIndicator;
UIProgressHUD.with(context)
  .show(customIndicator);
```

Use [Android Drawable Resources](http://developer.android.com/guide/topics/resources/drawable-resource.html) instead:

```java
UIProgressHUD.with(context)
  .show(R.drawable.loadingIndicator);
```

#### Show custom Drawable with custom message ####

Easy, as you might guess:

```java
UIProgressHUD.with(context)
  .show(R.drawable.loadingIndicator, R.string.logging_in);
```

#### Show success or error ####

```java
UIProgressHUD.with(context)
  .showSuccess("Done");
```

```java
UIProgressHUD.with(context)
  .showError(R.string.error_message);
```

#### Dissmissing ####

Simply call `dissmiss()`

```java
UIProgressHUD.with(context)
  .dismiss();
```

**Note**: Unless you set a timeout, the HUD never get dismissed. You have to explicitly call `dissmiss()`.

### Advanced Usage ###

#### HUD timeout ####

This snippet code will show "Hello" in 3 seconds then automatically dismiss.

```java
UIProgressHUD.with(context)
  .show("Hello")
  .in(3000);
```

#### Chain HUDs together ####

This will show `"Hello"` in 3 seconds then show `"I'm ProgressHUD"` in 1 second then finally dismiss.

```java
UIProgressHUD.with(context)
  .show("Hello")
  .in(3000)
  .then()
  .show("I'm ProgressHUD")
  .in(1000);
```

**Note**: You can use as many `show()`, `in()`, `then()` as possible.

### Supports ###

* Bug reporting: file an issue in this repo
* Discussions/questions: create new topic at [InAiR Developer Forums](https://developer.inair.tv)

### License ###

The MIT License

Copyright (c) 2014, SeeSpace. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
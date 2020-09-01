# AnimatedPullToRefresh
***
An simple general purpose UI library to add pull to refresh functionality with cool header text animation, which provides different customizations which allows to change header text, animation, color animation etc. Choose combination of your choice and rock your app screen.
***

## Feature
 * Easy integration
 * Easy customisation
 * Allows to customise text color, background color, animations etc of your choice.
***

## Demo
* Simple demo 1

![Simple demo 1](http://i.imgur.com/9VZF1p8.gif)

* Simple demo 2

![Simple demo 2](http://i.imgur.com/339TOr9.gif)

* Custom fonts

![Custom fonts](http://i.imgur.com/6hKRJSu.gif)

* Customize animation

![Customize animation](http://i.imgur.com/TRaE2Dn.gif)

* Customize animation iteration

![Customize animation iteration](http://i.imgur.com/h3pI43s.gif)


***
## How to integrate

Add it in your root build.gradle at the end of repositories:

````
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
````
Step 2. Add the dependency

````
	dependencies {
	        implementation 'com.github.HarinTrivedi:AnimatedPullToRefresh-master:1.0.4'
	}
````

***
## How to use
_**1. Inside layout xml**_

Add namespace in layout like:

    xmlns:app="http://schemas.android.com/apk/res-auto"

Use AnimatedPullToRefresh in xml layout like:

    <com.hlab.animatedPullToRefresh.AnimatedPullToRefreshLayout
        android:id="@+id/pullToRefreshLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:animationSpeed="fast"
        app:headerBackgroundColor="@color/colorWhite"
        app:headerLoopAnimIteration="1"
        app:headerLoopAnimation="zoom"
        app:headerText="@string/str_loading"
        app:headerTextAnimIteration="1"
        app:headerTextAnimation="rotateCW"
        app:headerTextColor="@color/colorLabelDark"
        app:headerTextColorAnimationEnabled="true"
        app:headerTextFontFamily="@font/lobster"> // new font support
    
    <!-- Your view -->

    </com.hlab.animatedPullToRefresh.AnimatedPullToRefreshLayout>

**Attributes**

* headerText : Text to display in header
* animationSpeed : Slow / Fast
* headerBackgroundColor : Background color of the header view 
* headerLoopAnimation : zoom / fade
* headerTextAnimation : rotateCW(clockwise) / rotateACW(anti-clockwise) / fade / zoom
* headerTextColor : Color of the text inside the header
* headerTextFontPath : Custom font path from assets

* headerTextColorAnimationEnabled : To enable / disable color changing animation of text
* headerTextAnimIteration : No of iteration to perform on single character animation (default 1)
* headerLoopAnimIteration : No of iteration to perform on whole text animation (default11)
* headerTextFontFamily: Custom font family from font resource // New feature

_**2. Inside Activity/Fragment**_

````
... implements AnimatedPullToRefreshLayout.OnRefreshListener {
...
AnimatedPullToRefreshLayout mPullToRefreshLayout = (AnimatedPullToRefreshLayout) findViewById(R.id.pullToRefreshLayout);
// Provide array of colors to add color animation of your choice 
mPullToRefreshLayout.setColorAnimationArray(new int[]{Color.CYAN, Color.RED, Color.YELLOW, Color.MAGENTA});
// Set refresh listener
mPullToRefreshLayout.setOnRefreshListener(this);
...
}

````

## More is coming, Enjoy and post issues/suggestion if you love to use this 👍 

***

## LICENSE
````
Copyright 2020 Harry's Lab

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````

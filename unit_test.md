
## Installation

**Step 1.**  Add the JitPack repository to your build file.
Add it in your root build.gradle at the end of repositories:
```css
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
	
**Step 2.**  Add the dependency (latest version from [jitpack](https://jitpack.io/#sabzo/ayanda/))
```css
	dependencies {
	        implementation 'com.github.sabzo:ayanda:-a964437e9a-1'
	}
```

## Run automated tests (Roboelectric)
[Robolectric](http://robolectric.org/) is a framework that allows you to write unit tests and run them on a desktop JVM while still using Android API.
### Create the run configuration :
Select _Run_ and then  _Edit Configuration_ Use the green _+_ sign at the top left corner to add a new run configuration and select _Android JUnit_.
 At _Use classpath of module_ select the ayanda-ayanda module of your Android application. As _Working directory_ use the directory of your app module. _Class_ is the file which contains your unit tests
![run configuration](https://github.com/atulnair/GSOC-2019/blob/master/gs2.png)

To run a test right click on the class file then select _run_
![unit tests](https://github.com/atulnair/GSOC-2019/blob/master/gs4.png)
## Sample apps build using Ayanda library

####   Chat app : It uses Ayanda library for offline communication.
- repository : [Chat app using Ayanda](https://github.com/atulnair/Chat-app-using-ayanda)

   ![initial screen](https://github.com/atulnair/GSOC-2019/blob/master/Screenshot_20190831-231952.png)
![chat](https://github.com/atulnair/GSOC-2019/blob/master/Screenshot_20190831-233758.png)

####  Tic-tac-toe : A multiplayer game using Ayanda library.
-   repository : [ Game ](https://github.com/atulnair/Tic-tac-toe-using-Ayanda)
![game here](https://github.com/atulnair/GSOC-2019/blob/master/Screenshot_20190831-231944.png)`
## Compatible Android versions 
Tested on :
```
| Android version   |
--------------------   
| Android 5.1       | 
| Android 5.1.1     |
| Android 6.0       |
| Android 6.0.1     |
| Android 6.1       |
| Android 7.0       |
| Android 8.0       |
| Android 8.0.1     |
| Android 8.1       |
| Android 9.0       |
```

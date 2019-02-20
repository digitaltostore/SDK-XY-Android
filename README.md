# DTS Android SDK Data XY

DTSxy is a framework to collect relevant information about user's location to better fit its needs.


## Requirements

* android sdk >= 15 (android 4.0.1)
* DTSxy need to be granted with `ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`


## Import DTS Data XY library

To access DTS Repository add the following configuration in your project build.gradle
``` 
  repositories {
      maven { url "https://packagecloud.io/DigitalToStore/SDK-Android-XY/maven2" }
  }
```

In your gradle file dependencies section add : 
```
  dependencies {
      implementation 'com.dts:dtsxy:6.1910.0'
  }
```


## Init framework

First of all, initialize the sdk
```
      DataXY.initialize(context);
```

Add the following piece of code `DataXY.onRequestPermissionsResult(context)` in order to init the framework as soon as the permission is granted
```
 @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      DataXY.onRequestPermissionsResult(this)) 
  }
```
           
Refer to samples in `Data XY samples` to learn how to use the data XY sdk.


## GDPR

With GDPR (General Data Protection Regulation) it is important to be able to enable or disable the data XY sdk. To do so, on user choice change, just call
```
      DataXY.enable(context, enable);
```
By default, the sdk is disabled.


### Contact
* **Technical support:** support.sdk.android@adhslx.com
* **Commercial support:** vosdonnees@adhslx.com

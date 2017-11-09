# DTS Android SDK Data XY


DTSxy is a framework to collect relevant information about user's location to better fit its needs.

## Requirements

* android sdk >= 15 (android 4.0.1)
* DTSxy need to be granted with 'ACCESS_FINE_LOCATION` or 'ACCESS_COARSE_LOCATION'
* an authentication ID (contact support if you don't have one)


## Access DTS Maven Repository

To access DTS Repository add the following configuration in your project build.gradle
``` 
repositories {
    maven { url "https://packagecloud.io/DigitalToStore/SDK-Android-XY/maven2" }
}
```

* In your gradle file dependencies section add : 
```
dependencies {
    compile 'com.dts:dtsxy:+'
}
```

## Init framework

* You must provide the authentication ID at startup (contact support if you don't have one)
        ` DataXY.initialize(context, DATA_XY_ID);`
        
* You need to grant the framework with location permission. If permission are not granted at startup you need to request permission as usual  
In your method `onRequestPermissionsResult` add the following piece of code `DataXY.onRequestPermissionsResult(context)` in order to init the framewok since the permission is granted
        
```
 @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      DataXY.onRequestPermissionsResult(this)) 
  }
```
           
* Refer to samples in `Data XY samples` to learn how to use the data XY sdk.

### Contact
**Technical support:** support.sdk.android@adhslx.com
**Commercial support:** vosdonnees@adhslx.com

# Project setup
## Files with sensitive keys
In order not to store sensitive information on github, all such an information was placed in special files and should be added only on the local developer's machines.
In order to do setup the project each developer need two files: google-services.json and keys.properties. Request this files from the appropriate person.
* google-services.json - place this file to the ./app folder
* keys.properties - place this file to the root folder of the project

## Signing configs
In the `local.properties` file create info about signing certificate. For debug build it should be like this(with usage of standard debug certificate):  
    test.storeFile=C\:\\Users\\<YOUR USER>\\.android\\debug.keystore  
    test.keyAlias=androiddebugkey  
    test.storePassword=android  
    test.keyPassword=android  

If release build is needed, the same 4 parameters should be added to the `local.properties` file, but with the prefix `release` instead of `test`

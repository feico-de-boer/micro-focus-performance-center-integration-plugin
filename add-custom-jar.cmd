@rem Custom library version
@rem ----------------------
set LIB=c:/LocalData/performance-center-plugins-common-utils/target/plugins-common-custom-1.2.1.1.jar

@rem Standard library version
@rem ------------------------
@rem set LIB=C:/Users/fdboer/Downloads/plugins-common-1.2.0.jar
@rem set LIB=C:/Users/fdboer/Downloads/plugins-common-1.2.1.jar

"C:/Opt/apache-maven-3.9.9/bin/mvn.cmd" install:install-file ^
  -Dfile=%LIB% ^
  -DgroupId=com.microfocus.adm.performancecenter ^
  -DartifactId=plugins-common-custom ^
  -Dversion=1.2.1.1 ^
  -Dpackaging=jar

exit

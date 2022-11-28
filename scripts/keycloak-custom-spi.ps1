$maven = "C:\Users\jmcap\.m2\wrapper\dists\apache-maven-3.6.3-bin\1iopthnavndlasol9gbrbg6bf2\apache-maven-3.6.3\bin\mvn.cmd"
# $projPath = "d:\dev\joelcaples\keycloak-spi-demo\projects\keycloak-sms-spi";
# $distPath = "d:\dev\joelcaples\keycloak-spi-demo\dist";
$projPath = "..\projects\keycloak-sms-spi";
$distPath = "providers";


Start-Process -FilePath $maven -Wait -ArgumentList "clean -f `"${projPath}\pom.xml`"" -NoNewWindow
Start-Process -FilePath $maven -Wait -ArgumentList "package -f `"${projPath}\pom.xml`"" -NoNewWindow

New-Item -Path $distPath -ItemType Directory -force
Copy-Item "${projPath}\target\*.jar" -Destination $distPath -Recurse -force

.\keycloak-custom-spi-build.ps1
.\keycloak-custom-spi-run.ps1

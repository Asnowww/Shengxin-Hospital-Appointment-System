@echo off
echo Generating local development SSL certificate...

keytool -genkeypair ^
  -alias server ^
  -keyalg RSA ^
  -keysize 2048 ^
  -storetype PKCS12 ^
  -keystore server.p12 ^
  -validity 3650 ^
  -storepass changeit ^
  -keypass changeit ^
  -dname "CN=localhost, OU=Dev, O=Project, L=City, ST=State, C=CN" ^
  -ext "SAN=dns:localhost,ip:127.0.0.1"

move server.p12 ..\src\main\resources\
echo Done! The keystore is generated in src/main/resources/server.p12
pause

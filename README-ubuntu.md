sudo apt update
sudo apt install git
sudo apt install zip unzip

https://sdkman.io/

curl -s "https://get.sdkman.io" | bash
source "/home/payper/.sdkman/bin/sdkman-init.sh"

sdk install java 10.0.2-open
sdk install maven 3.5.4

ssh-keygen
cat ~/.ssh/id_rsa.pub

git clone git@github.com:hashgraph/hedera-sdk-java.git

cd hedera-sdk-java
mvn install

cd ..

git clone git@github.com:payperws/payper-gateway.git

cd payper-gateway

mvn package

cp src/main/resources/application-price-list.yaml application-price-list.yaml

keytool -import -alias google.com -keystore ~/.sdkman/candidates/java/10.0.2-open/lib/security/cacerts -file _.google.com.cer

java -jar target/gateway-0.0.1-SNAPSHOT.jar
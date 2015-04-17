#!/bin/bash
echo "Deploying to bintray"

shopt -s nullglob
for f in artifacts/baron-greenback-$BUILD_NUMBER*
do
	curl -T $f -usns-deployer:$BINTRAY_KEY -H "X-Bintray-Package:baron-greenback" -H "X-Bintray-Version:$BUILD_NUMBER" https://api.bintray.com/content/sns/baron-greenback/sky/sns/baron-greenback/baron-greenback/$BUILD_NUMBER/$(basename $f)
done

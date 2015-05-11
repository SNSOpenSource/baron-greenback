#!/bin/bash
echo "Deploying '$BUILD_NUMBER' to bintray"

shopt -s nullglob
for f in ./build/artifacts/baron-greenback-$BUILD_NUMBER*
do
	echo $(basename $f)
	curl -T $f -usns-deployer:$BINTRAY_KEY -H "X-Bintray-Package:baron-greenback" -H "X-Bintray-Version:$BUILD_NUMBER" https://api.bintray.com/content/sns/baron-greenback/com/sky/sns/baron-greenback/$BUILD_NUMBER/$(basename $f)
	echo
done

#!/bin/bash
if [ -z ${BINTRAY_KEY+x} ];
then
	echo "Not uploading artifacts to bintray as BINTRAY_KEY is not set"
else 
	echo "Deploying '$BUILD_NUMBER' to bintray"

	shopt -s nullglob
	for f in ./build/artifacts/baron-greenback-$BUILD_NUMBER*
	do
		echo $(basename $f)
		curl -T $f -usns-deployer:$BINTRAY_KEY -H "X-Bintray-Package:baron-greenback" -H "X-Bintray-Version:$BUILD_NUMBER" -H "X-Bintray-Publish: 1" https://api.bintray.com/content/sns/baron-greenback/com/sky/sns/baron-greenback/$BUILD_NUMBER/$(basename $f)
		echo
	done

fi



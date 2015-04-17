shopt -s nullglob
for f in artifacts/baron-greenback-$BUILD_NUMBER*
do
	curl -T artifacts/$f -usns-deployer:$BINTRAY_KEY -H "X-Bintray-Package:baron-greenback" -H "X-Bintray-Version:$BUILD_NUMBER" https://api.bintray.com/content/sns/baron-greenback/sky/sns/baron-greenback/$(basename $f)
done

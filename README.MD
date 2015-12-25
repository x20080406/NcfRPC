#NcfRPC project

###windows
gradlew.bat clean :rpc-impl:app_copyProperties :rpc-impl:test --tests "BenchmarkTestCase" --debug

###linux
gradlew clean :rpc-impl:app_copyProperties :rpc-impl:test --tests "BenchmarkTestCase" --debug


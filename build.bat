set MODULES=(common/config common/utils common/io common/gui common/pipeline common/opencl)

for %%i in %MODULES% do (
	cd %%i
	call mvn clean install
	cd ../..
)

cd graspj
call mvn clean install %*
cd ..
SET script_dir=%~dp0
SET user_dir=%CD%
SET solutions=%~dp0..\..\..\..\..
SET java=%script_dir%..\..\..\..\..\..\..\java-advanced-2020
SET libs=%java%\lib\hamcrest-core-1.3.jar;%java%\lib\jsoup-1.8.1.jar;%java%\lib\junit-4.11.jar;%java%\lib\quickcheck-0.6.jar;%java%\artifacts\info.kgeorgiy.java.advanced.base.jar;%java%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET modules=%java%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor

md "%solutions%\_build"

javac -d "%solutions%\_build" -classpath "%libs%" "%script_dir%Implementor.java" "%script_dir%JarImplementor.java" "%modules%\Impler.java" "%modules%\JarImpler.java" "%modules%\ImplerException.java"

cd "%script_dir%"

echo Main-Class: ru.ifmo.rain.shaldin.implementor.Implementor > Manifest.mf
echo Implementation-Vendor: Vsevolod Shaldin >> Manifest.mf

SET kgeorgiy=info\kgeorgiy\java\advanced\implementor

cd "%solutions%\_build"

jar cfm "..\ru\ifmo\rain\shaldin\implementor\_implementor.jar" "..\ru\ifmo\rain\shaldin\implementor\Manifest.mf" ru info

cd "%script_dir%"

erase Manifest.mf
rd /s/q "%solutions%\_build"
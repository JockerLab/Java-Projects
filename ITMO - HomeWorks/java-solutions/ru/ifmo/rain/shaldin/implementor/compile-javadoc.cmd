SET script_dir=%~dp0
SET user_dir=%CD%
SET solutions=%~dp0..\..\..\..\..
SET java=%script_dir%..\..\..\..\..\..\..\java-advanced-2020
SET libs=%java%\lib\hamcrest-core-1.3.jar;%java%\lib\jsoup-1.8.1.jar;%java%\lib\junit-4.11.jar;%java%\lib\quickcheck-0.6.jar;%java%\artifacts\info.kgeorgiy.java.advanced.base.jar;%java%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET modules=%java%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\
SET doc_link=https://docs.oracle.com/en/java/javase/11/docs/api

javadoc %script_dir%Implementor.java %script_dir%JarImplementor.java %modules%Impler.java %modules%JarImpler.java %modules%ImplerException.java -d %user_dir%\_javadoc -link %doc_link% -private
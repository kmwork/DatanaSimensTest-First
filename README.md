# Тестовое ПО на проверку  дачтиков сименс

## cборка Maven 3 + JDK 8
`mvn clean compile assembly:single`

## Как пользоваться
после сборки мавеном, из папки etc для bat и sh командые файлы
примерный синтаксис команды
`java -jar target/Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar -Dapp.dir=<путь к папке config>`
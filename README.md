# Тестовое ПО на проверку  дачтиков сименс
для контролера Siemens S7 1200 (https://new.siemens.com/ru/ru/produkty/avtomatizacia/sistemy-avtomatizacii/promyshlennye-sistemy-simatic/kontroller-simatic/s7-1200.html)

## cборка Maven 3 + JDK 8
`mvn clean compile assembly:single`

## Как пользоваться
после сборки мавеном, из папки <this project>/etc для bat и sh командые файлы

### примерный синтаксис команды
`java -Dapp.dir=<путь к папке config> -jar target/Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar`

## настройка
файл `datana_siemens.properties` c настройками

## техническая документация 
в папке <this project>/doc-manual
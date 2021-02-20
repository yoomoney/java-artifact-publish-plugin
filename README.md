# java-artifact-publish-plugin
Плагин реализует и настраивает функционал публикации артефакта.

## Подключение
Для подключения добавьте в build.gradle:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'ru.yoomoney.gradle.plugins:java-artifact-publish-plugin:4.+'
    }
}
apply plugin: 'ru.yoomoney.gradle.plugins.java-artifact-publish-plugin'

```

## Конфигурация

Плагин конфигурируется следующим образом:
```groovy
javaArtifactPublishSettings {
    // Имя пользователя для отгрузки в Nexus, обязательный параметр.
    nexusUser = System.getenv("NEXUS_USER")
    // Пароль пользователя для отгрузки в Nexus, обязательный параметр.
    nexusPassword = System.getenv("NEXUS_PASSWORD")
    // Группа отгружаемого артефакта, обязательный параметр.
    groupId = 'ru.yoomoney.common'
    // Имя отгружаемого артефакта, обязательный параметр.
    artifactId = 'artifact'
    // Вид публикуемого артефакта, необязательный параметр.
    publishingComponent = components.java
    // Репозиторий, в который загружать snapshot версии, обязательный параметр.
    snapshotRepository = "https://yoomoney/repository/snapshots/"
    // Репозиторий, в который загружать release версии, обязательный параметр.
    releaseRepository = "https://yoomoney/repository/release/"
}
```

# Использование

Плагин публикует заданный артефакт при помощи таски `publish`.
Результатом вызовом вышеназванной таски будет отгрузка артефакта в Nexus в репозиторий
* snapshots - для версий с постфиксом `-SNAPSHOT`
* releases - для всех остальных версий.
Дополнительно в сборочной директории будет создан файл `version.txt`, 
содержащий полное имя отгруженного артефакта.
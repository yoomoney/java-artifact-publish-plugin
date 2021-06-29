[![Build Status](https://travis-ci.com/yoomoney-gradle-plugins/java-artifact-publish-plugin.svg?branch=master)](https://travis-ci.com/yoomoney-gradle-plugins/java-artifact-publish-plugin)
[![codecov](https://codecov.io/gh/yoomoney-gradle-plugins/java-artifact-publish-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/yoomoney-gradle-plugins/java-artifact-publish-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

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

# Использование

Плагин создает publication с именем "mainArtifact".
Плагин публикует заданный артефакт при помощи таски `publishMainArtifactPublicationToMavenRepository`.
Также публикация будет осуществлена при вызове общей таски `publish`, наряду с другими существующими в проекте публикациями.
Результатом вызовом вышеназванной таски будет отгрузка артефакта в Nexus в репозиторий
* snapshots - для версий с постфиксом `-SNAPSHOT`
* releases - для всех остальных версий.
Дополнительно в сборочной директории будет создан файл `version.txt`, 
содержащий полное имя отгруженного артефакта.

## Подпись артефактов
Если выгружаемый артефакт необходимо подписать (обязательное требование для, например, выгрузки в MavenCentral), укажите в настройках  
```groovy
javaArtifactPublishSettings {
    signing = true //по умолчанию подпись отключена
}
```  
А также добавьте системные переменные:
* `ORG_GRADLE_PROJECT_signingKey` - значение ключа в ascii-armored формате  
* `ORG_GRADLE_PROJECT_signingPassword` - passphrase ключа  
  
Для подписи артефакта используется стандартный плагин [signing](https://docs.gradle.org/current/userguide/signing_plugin.html).  
Подписаны будут только релизные версии артефактов, т.к. для загрузки snapshot версий подпись не нужна.

## Публикация артефакта в staging репозиторий
Если выгружаемый артефакт необходимо публиковать в staging репозиторий, укажите в настройках:
```groovy
    javaArtifactPublishSettings {
        staging = true // по умолчанию публикация в staging отключена
    }
```
Задача `publishMainArtifactPublicationToMavenRepository` публикует артефакт в созданный staging репозиторий.
Для выпуска (release) опубликованного артефакта необходимо вызвать задачу `closeAndReleaseMavenStagingRepository`.

Задачи создания и закрытия staging репозитория должны быть выполнены в едином gradle процессе,
иначе информация о созданном staging репозитория потеряется ([связанная задача](https://github.com/gradle-nexus/publish-plugin/issues/19)).

Для публикации артефака в staging репозиторий используется плагин [io.github.gradle-nexus.publish-plugin](https://github.com/gradle-nexus/publish-plugin)

## Конфигурация

Плагин конфигурируется следующим образом:
```groovy
javaArtifactPublishSettings {
    // URL nexus сервера для управления staging репозиториями
    nexusUrl = 'https://oss.sonatype.org/service/local/' 
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
    // Репозиторий, в который загружать release версии, обязательный параметр если не используется публикация в staging 
    releaseRepository = "https://yoomoney/repository/release/"
    // Нужно ли подписывать артефакт при публикации, необязательный параметр.
    signing = false //значение по умолчанию
    // Нужно ли публиковать release артефакт в staging репозиторий
    staging = false // значение по умолчанию

    //добавление дополнительной информации в pom проекта.
    //по умолчанию дополнительная информация не добавляется (addInfo=false).
    publicationAdditionalInfo {
        addInfo = true
        organizationUrl = "https://github.com/yoomoney-gradle-plugins"
        license {
            name = "MIT License"
            url = "http://www.opensource.org/licenses/mit-license.php"
        }
        developers {
            developer {
                name = 'Ivan'
                email = 'ivan@test.ru'
                organization = 'test'
                organizationUrl = 'https://test.ru'
            }
        }
        description = "description"
    }
}
```

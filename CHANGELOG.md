### NEXT_VERSION_TYPE=MAJOR|MINOR|PATCH
### NEXT_VERSION_DESCRIPTION_BEGIN
### NEXT_VERSION_DESCRIPTION_END
## [4.0.1](https://github.com/yoomoney/java-artifact-publish-plugin/pull/11) (07-04-2022)

Перевыпуск релиза из-за проблем с синхронизацией maven-central

## [4.0.0](https://github.com/yoomoney/java-artifact-publish-plugin/pull/10) (06-04-2022)

* Удалено использование `deprecated` `gradle api`;
* **breaking changes** Сборка плагина для gradle `7.4.1`;

## [3.4.0](https://github.com/yoomoney/java-artifact-publish-plugin/pull/9) (14-03-2022)

* Добавлено определение зависимостей по runtime зависимостям для возможности не указывать версии в dependencies при использовании dependencyManagement.

## [3.3.0](https://github.com/yoomoney/java-artifact-publish-plugin/pull/8) (26-08-2021)

* Переезд организации yoomoney-gradle-plugins -> yoomoney

## [3.2.1](https://github.com/yoomoney/java-artifact-publish-plugin/pull/7) (03-08-2021)

* Исправлено конфигурирование плагина для gradle подпроектов с публикацией в staging

## [3.2.0](https://github.com/yoomoney/java-artifact-publish-plugin/pull/6) (29-06-2021)

* Добавлена поддержка публикации release артефакта в staging репозиторий

## [3.1.1](https://github.com/yoomoney/java-artifact-publish-plugin/pull/5) (19-05-2021)

* Добавлена информация о сборке, покрытии, лицензии в README.md.

## [3.1.0](https://github.com/yoomoney/java-artifact-publish-plugin/pull/4) (19-03-2021)

* Сборка проекта переведена на gradle-project-plugin.

## [3.0.2](https://api.github.com/repos/yoomoney/java-artifact-publish-plugin/pulls/3) (11-03-2021)

* Имя публикации сделано публичным для возможности использования в местах подключения.

## [3.0.1](https://api.github.com/repos/yoomoney/java-artifact-publish-plugin/pulls/2) (02-03-2021)

* Теперь подпись артефактов осуществляется только для релизных версий, т.к. для выгрузки snapshot подпись не нужна.

## [3.0.0](https://api.github.com/repos/yoomoney/java-artifact-publish-plugin/pulls/1) (02-03-2021)

* Внесены изменения в связи с переходом на git-hub:
* Переименованы пакеты
* Плагин собирается без использования project-plugin, сборка полностью описывается в build.gradle
* Подключен artifact-release-plugin для автоматического выпуска релиза.
* Сборка переведена на travis (ранее использовался jenkins)
* ***breaking_changes*** параметры snapshotRepository и releaseRepository больше не содержат значений по-умолчанию,
теперь они обязательны для заполнения.
* Добавлена возможность подписи артефакта для публикации в maven-central.

## [2.4.0]() (12-02-2021)

* Переименование yamoney-kotlin-module-plugin в ru.yoomoney.gradle.plugins.kotlin-plugin

## [2.3.2]() (30-11-2020)

* Обновлена версия kotlin 1.3.71 -> 1.3.50

## [2.3.1]() (23-11-2020)

* Поправлен url для `releaseRepository` в `JavaArtifactPublishExtension`

## [2.3.0]() (06-08-2020)

* Добавлены настройки snapshotRepository и releaseRepository - для указания адресов репозиториев.

## [2.2.0]() (03-07-2020)

* Поднята версия gradle: 6.0.1 -> 6.4.1.

## [2.1.0]() (05-02-2020)

* Сборка на java 11

## [2.0.1]() (30-01-2020)

* Удален snapshots репозиторий.

## [2.0.0]() (29-01-2020)

* Обновлена версия gradle `4.10.2` -> `6.0.1`
* Обновлены версии зависимостей
* Исправлены warnings и checkstyle проблемы

## [1.0.0]() (22-10-2019)

Функционал публикации артефакта перенесён в отдельный плагин
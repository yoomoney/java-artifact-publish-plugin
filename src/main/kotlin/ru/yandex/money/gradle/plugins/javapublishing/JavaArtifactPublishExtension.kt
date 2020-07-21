package ru.yandex.money.gradle.plugins.javapublishing

import org.gradle.api.component.SoftwareComponent

/**
 * Конфигурация плагина публикации
 *
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
open class JavaArtifactPublishExtension {

    /**
     * Имя пользователя для отгрузки в Nexus
     */
    var nexusUser: String? = null
    /**
     * Пароль пользователя для отгрузки в Nexus
     */
    var nexusPassword: String? = null
    /**
     * Имя отгружаемого артефакта
     */
    var artifactId: String? = null
    /**
     * Группа отгружаемого артефакта
     */
    var groupId: String? = null
    /**
     * Вид отгружаемого артефакта
     */
    var publishingComponent: SoftwareComponent? = null
    /**
     * Репозиторий, в который загружать snapshot версии
     */
    var snapshotRepository: String = "https://nexus.yamoney.ru/repository/snapshots/"
    /**
     * Репозиторий, в который загружать release версии
     */
    var releaseRepository: String = "https://nexus.yamoney.ru/repository/release/"
}

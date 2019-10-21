package ru.yandex.money.gradle.plugins.publishing

import org.gradle.api.component.SoftwareComponent

/**
 * Конфигурация плагина публикации
 *
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
open class ArtifactPublishExtension {

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
}

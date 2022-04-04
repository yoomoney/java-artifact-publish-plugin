package ru.yoomoney.gradle.plugins.javapublishing

/**
 * Настройки публикации release артефактов в staging репозиторий
 *
 * @author Petr Zinin pgzinin@yoomoney.ru
 * @since 29.06.2021
 */
open class StagingPublicationSettings {
    /**
     * Нужно ли публиковать артефакт в staging репозиторий
     */
    var enabled: Boolean = false

    /**
     * URL адрес nexus сервиса.
     * Используется для управления staging репозиториями при публикации артефакта
     */
    var nexusUrl: String? = null
}
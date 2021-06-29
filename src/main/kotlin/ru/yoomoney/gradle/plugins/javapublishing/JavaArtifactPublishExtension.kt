package ru.yoomoney.gradle.plugins.javapublishing

import groovy.lang.Closure
import org.gradle.api.component.SoftwareComponent
import org.gradle.util.ConfigureUtil

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
    var snapshotRepository: String? = null
    /**
     * Репозиторий, в который загружать release версии
     */
    var releaseRepository: String? = null
    /**
     * Нужно ли подписывать артефакт при публикации
     */
    var signing: Boolean = false
    /**
     * Настройки публикации release артефактов в staging репозиторий
     */
    var staging: StagingPublicationSettings = StagingPublicationSettings()
    /**
     * Настройки дополнительной информацией о публикуемом артефакте. Информация добавляется в pom.
     */
    var publicationAdditionalInfo = PublicationAdditionalInfo()

    fun publicationAdditionalInfo(closure: Closure<*>) {
        val action = ConfigureUtil.configureUsing<PublicationAdditionalInfo>(closure)
        val publicationInfo = PublicationAdditionalInfo()
        action.execute(publicationInfo)

        publicationAdditionalInfo(publicationInfo)
    }

    fun publicationAdditionalInfo(publicationInfo: PublicationAdditionalInfo) {
        publicationAdditionalInfo = publicationInfo
    }

    fun staging(closure: Closure<*>) {
        val action = ConfigureUtil.configureUsing<StagingPublicationSettings>(closure)
        val staging = StagingPublicationSettings()
        action.execute(staging)

        staging(staging)
    }

    fun staging(staging: StagingPublicationSettings) {
        this.staging = staging
    }
}

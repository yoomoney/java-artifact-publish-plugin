package ru.yoomoney.gradle.plugins.javapublishing

import org.gradle.api.Action
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Конфигурация плагина публикации
 *
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
open class JavaArtifactPublishExtension @Inject constructor(private val objects: ObjectFactory) {
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
    var staging: StagingPublicationSettings = objects.newInstance(StagingPublicationSettings::class.java)
    /**
     * Настройки дополнительной информацией о публикуемом артефакте. Информация добавляется в pom.
     */
    var publicationAdditionalInfo = objects.newInstance(PublicationAdditionalInfo::class.java)

    fun publicationAdditionalInfo(action: Action<PublicationAdditionalInfo>) {
        val publicationInfo = objects.newInstance(PublicationAdditionalInfo::class.java)
        action.execute(publicationInfo)
        publicationAdditionalInfo(publicationInfo)
    }

    fun publicationAdditionalInfo(publicationInfo: PublicationAdditionalInfo) {
        publicationAdditionalInfo = publicationInfo
    }

    fun staging(action: Action<StagingPublicationSettings>) {
        val staging = objects.newInstance(StagingPublicationSettings::class.java)
        action.execute(staging)

        staging(staging)
    }

    fun staging(staging: StagingPublicationSettings) {
        this.staging = staging
    }
}

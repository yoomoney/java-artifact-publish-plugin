package ru.yoomoney.gradle.plugins.javapublishing

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * Настройки дополнительной информацией о публикуемом артефакте.
 * Данные используются при формировании pom проекта.
 *
 * @author horyukova
 * @since 26.02.2021
 */
open class PublicationAdditionalInfo @Inject constructor(private val objects: ObjectFactory) {
    /**
     * Нужно ли добавлять дополнительную информацию при публикации
     */
    var addInfo: Boolean = false

    /**
     * url организации, из которого будут создаваться url-ы для секции scm
     * Пример: https://github.com/yoomoney
     */
    var organizationUrl: String? = null

    /**
     * Список разработчиков проекта
     */
    var developers: ArrayList<Developer>? = ArrayList()

    /**
     * Информация о лицензии проекта
     */
    var license: License? = null

    /**
     * Описание проекта
     */
    var description: String? = null

    /**
     * Методы для удобного конструирования developers из build.gradle
     */
    fun developers(dev: ArrayList<Developer>) {
        developers!!.addAll(dev)
    }

    fun developers(closure: Closure<*>) {
        closure.call()
    }

    fun developer(action: Action<in Developer>) {
        val dev = objects.newInstance(Developer::class.java)
        action.execute(dev)

        developers!!.add(dev)
    }

    /**
     * Методы для удобного конструирования license из build.gradle
     */
    fun license(action: Action<in License>) {
        val newLicense = objects.newInstance(License::class.java)
        action.execute(newLicense)

        license(newLicense)
    }

    fun license(newLicense: License) {
        license = newLicense
    }

    /**
     * Информация о разработчике проекта
     */
    open class Developer {
        var name: String? = null
        var email: String? = null
        var organization: String? = null
        var organizationUrl: String? = null
    }

    /**
     * Информация о лицензии
     */
    open class License {
        var name: String? = null
        var url: String? = null
    }
}
package ru.yandex.money.gradle.plugins.publishing

import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import java.io.IOException
import java.net.URI
import java.nio.file.Paths

/**
 * Плагин, конфигурирующий публикацию артефакта
 *
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
class ArtifactPublishPlugin : Plugin<Project> {

    companion object {
        private val log: Logger = Logging.getLogger(ArtifactPublishPlugin::class.java)
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        val extension = project.extensions.findByType(ArtifactPublishExtension::class.java)
        if (extension == null) {
            project.extensions.create("artifactPublish", ArtifactPublishExtension::class.java)
        }
        project.afterEvaluate { target ->
            val artifactPublishExtension = project.extensions.getByType(ArtifactPublishExtension::class.java)
            configureJavadoc(target)
            configurePublishing(target, artifactPublishExtension)
            configureStoreVersion(target, artifactPublishExtension)
        }
    }

    private fun configureJavadoc(project: Project) {
        val javadoc = project.tasks.findByName("javadoc") as Javadoc?
        javadoc!!.options.encoding = "UTF-8"
        javadoc.isFailOnError = false

        project.tasks.create("sourcesJar", Jar::class.java) { sourcesJar ->
            val sourceSet = project
                    .convention
                    .getPlugin(JavaPluginConvention::class.java)
                    .sourceSets
                    .getByName("main")
                    .allSource
            sourceSet.filter.include("**/*.java", "**/*.kt", "**/*.kts")

            sourcesJar.from(sourceSet)
            sourcesJar.classifier = "sources"
        }

        project.tasks.create("javadocJar", Jar::class.java) { javadocJar ->
            javadocJar.dependsOn("javadoc")
            javadocJar.classifier = "javadoc"
            javadocJar.from(javadoc.destinationDir!!)
        }
    }

    private fun configurePublishing(project: Project, artifactPublishExtension: ArtifactPublishExtension) {
        val publishingExtension = project.extensions.getByType(PublishingExtension::class.java)

        publishingExtension.publications { publicationContainer ->
            val mavenJava = publicationContainer.maybeCreate("mavenJava", MavenPublication::class.java)
            mavenJava.groupId = artifactPublishExtension.groupId
            mavenJava.artifactId = artifactPublishExtension.artifactId
            mavenJava.from(getPublishingComponent(project, artifactPublishExtension))
            mavenJava.artifact(project.tasks.getByName("sourcesJar"))
            mavenJava.artifact(project.tasks.getByName("javadocJar"))
        }
        publishingExtension.repositories { artifactRepositories ->
            artifactRepositories.maven { repository ->
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    repository.url = URI.create("https://nexus.yamoney.ru/repository/snapshots/")
                } else {
                    repository.url = URI.create("https://nexus.yamoney.ru/repository/releases/")
                }

                repository.credentials { passwordCredentials ->
                    passwordCredentials.username = artifactPublishExtension.nexusUser
                    passwordCredentials.password = artifactPublishExtension.nexusPassword
                }
            }
        }
        project.tasks.withType(PublishToMavenRepository::class.java).forEach { task -> task.dependsOn("jar", "test") }
    }

    private fun configureStoreVersion(project: Project, artifactPublishExtension: ArtifactPublishExtension) {
        storeVersion(project, artifactPublishExtension)
        project.tasks.getByName("publish").finalizedBy("storeVersion")
    }

    private fun storeVersion(project: Project, artifactPublishExtension: ArtifactPublishExtension) {
        val storeVersion = project.tasks.create("storeVersion")
        storeVersion.description = "Generates file, which contains information about build version"
        storeVersion.doLast { task ->
            val version = String.format("%s:%s:%s",
                    artifactPublishExtension.groupId,
                    artifactPublishExtension.artifactId,
                    project.version)
            storeVersionToFile(project.buildDir.absolutePath, version)
        }
    }

    private fun storeVersionToFile(versionDir: String, content: String) {
        try {
            val versionFile = Paths.get(versionDir, "version.txt")
            ResourceGroovyMethods.write(versionFile.toFile(), content)
            log.lifecycle("File with version generated successfully into $versionFile")
        } catch (e: IOException) {
            log.lifecycle("Error occurred during storing version", e)
        }
    }

    private fun getPublishingComponent(
        project: Project,
        artifactPublishExtension: ArtifactPublishExtension
    ): SoftwareComponent? {
        return if (artifactPublishExtension.publishingComponent == null) {
            project.components.getByName("java")
        } else artifactPublishExtension.publishingComponent
    }
}

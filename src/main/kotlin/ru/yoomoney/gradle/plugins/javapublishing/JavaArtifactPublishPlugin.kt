package ru.yoomoney.gradle.plugins.javapublishing

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import java.io.IOException
import java.net.URI
import java.nio.file.Paths

/**
 * Плагин, конфигурирующий публикацию артефакта
 *
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
class JavaArtifactPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        project.pluginManager.apply(NexusPublishPlugin::class.java)
        // Пытаемся получить зарегистрированный extension для случая когда настройки
        // данного плагина необходимо выставить в соседнем блоке afterEvaluate
        val extension = project.extensions.findByType(JavaArtifactPublishExtension::class.java)
        if (extension == null) {
            project.extensions.create(extensionName, JavaArtifactPublishExtension::class.java)
        }
        val publishingExtension = project.extensions.getByType(PublishingExtension::class.java)
        publishingExtension.publications.create(publicationName, MavenPublication::class.java)

        project.afterEvaluate { target ->
            val artifactPublishExtension = project.extensions.getByType(JavaArtifactPublishExtension::class.java)
            configureJavadoc(target)
            configurePublishing(target, artifactPublishExtension)
            configureStaging(target)
            if (artifactPublishExtension.signing && !project.isSnapshot()) {
                signing(target)
            }
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
            sourcesJar.archiveClassifier.set("sources")
        }

        project.tasks.create("javadocJar", Jar::class.java) { javadocJar ->
            javadocJar.dependsOn("javadoc")
            javadocJar.archiveClassifier.set("javadoc")
            javadocJar.from(javadoc.destinationDir!!)
        }
    }

    private fun configureStaging(project: Project) {
        val publishingExtension = project.extensions.getByType(NexusPublishExtension::class.java)

        publishingExtension.repositories.sonatype()
    }

    private fun configurePublishing(project: Project, javaArtifactPublishExtension: JavaArtifactPublishExtension) {
        val publishingExtension = project.extensions.getByType(PublishingExtension::class.java)
        val publicationAdditionalInfo = javaArtifactPublishExtension.publicationAdditionalInfo

        publishingExtension.publications { publicationContainer ->
            val mavenPublication = publicationContainer.maybeCreate(publicationName, MavenPublication::class.java)
            mavenPublication.groupId = javaArtifactPublishExtension.groupId
            mavenPublication.artifactId = javaArtifactPublishExtension.artifactId
            mavenPublication.from(getPublishingComponent(project, javaArtifactPublishExtension))
            mavenPublication.artifact(project.tasks.getByName("sourcesJar"))
            mavenPublication.artifact(project.tasks.getByName("javadocJar"))

            if (publicationAdditionalInfo.addInfo) {
                addAdditionalInfo(javaArtifactPublishExtension.artifactId!!, publicationAdditionalInfo, mavenPublication)
            }
        }

        publishingExtension.repositories { artifactRepositories ->
            artifactRepositories.maven { repository ->
                if (project.isSnapshot()) {
                    repository.url = URI.create(javaArtifactPublishExtension.snapshotRepository!!)
                } else {
                    repository.url = URI.create(javaArtifactPublishExtension.releaseRepository!!)
                }

                repository.credentials { passwordCredentials ->
                    passwordCredentials.username = javaArtifactPublishExtension.nexusUser
                    passwordCredentials.password = javaArtifactPublishExtension.nexusPassword
                }
            }
        }
        project.tasks.withType(PublishToMavenRepository::class.java).forEach { task -> task.dependsOn("jar", "test") }
    }

    private fun Project.isSnapshot() = project.version.toString().endsWith("-SNAPSHOT")

    private fun signing(target: Project) {
        target.pluginManager.apply(SigningPlugin::class.java)

        val signingExtension = target.extensions.getByType(SigningExtension::class.java)

        // стандартные properties, которые создает signing плагин.
        // значения берутся из ORG_GRADLE_PROJECT_signingKey и ORG_GRADLE_PROJECT_signingPassword
        val signingKey = target.property("signingKey") as String?
        val signingPassword = target.property("signingPassword") as String?
        signingExtension.useInMemoryPgpKeys(signingKey, signingPassword)

        val publishingExtension = target.extensions.getByType(PublishingExtension::class.java)
        val mavenPublication = publishingExtension.publications.getByName(publicationName) as MavenPublication
        signingExtension.sign(mavenPublication)
    }

    private fun configureStoreVersion(project: Project, javaArtifactPublishExtension: JavaArtifactPublishExtension) {
        storeVersion(project, javaArtifactPublishExtension)
        project.tasks.getByName("publish").finalizedBy("storeVersion")
    }

    private fun storeVersion(project: Project, javaArtifactPublishExtension: JavaArtifactPublishExtension) {
        val storeVersion = project.tasks.create("storeVersion")
        storeVersion.description = "Generates file, which contains information about build version"
        storeVersion.doLast { _ ->
            val version = String.format("%s:%s:%s",
                    javaArtifactPublishExtension.groupId,
                    javaArtifactPublishExtension.artifactId,
                    project.version)
            storeVersionToFile(project, project.buildDir.absolutePath, version)
        }
    }

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    private fun storeVersionToFile(project: Project, versionDir: String, content: String) {
        try {
            val versionFile = Paths.get(versionDir, "version.txt")
            ResourceGroovyMethods.write(versionFile.toFile(), content)
            project.logger.lifecycle("File with version generated successfully into $versionFile")
        } catch (e: IOException) {
            project.logger.lifecycle("Error occurred during storing version", e)
        }
    }

    private fun getPublishingComponent(
        project: Project,
        javaArtifactPublishExtension: JavaArtifactPublishExtension
    ): SoftwareComponent? {
        return if (javaArtifactPublishExtension.publishingComponent == null) {
            project.components.getByName("java")
        } else javaArtifactPublishExtension.publishingComponent
    }

    private fun addAdditionalInfo(
        artifactId: String,
        additionalInfo: PublicationAdditionalInfo,
        mavenPublication: MavenPublication
    ) {
        val host = URI(additionalInfo.organizationUrl!!).host
        val organizationId = URI(additionalInfo.organizationUrl!!).path.replace("/", "")

        mavenPublication.pom { pomInfo ->
            pomInfo.description.set(additionalInfo.description!!)
            pomInfo.packaging = "jar"
            pomInfo.name.set(artifactId)
            pomInfo.url.set(getPublicationUrl(host, organizationId, artifactId))
            pomInfo.licenses { pomLicenseSpec ->
                pomLicenseSpec.license { license ->
                    additionalInfo.license?.also {
                        it.name?.also {
                            license.name.set(it)
                        }
                        it.url?.also {
                            license.url.set(it)
                        }
                    }
                }
            }

            pomInfo.developers { pomDeveloperSpec ->
                additionalInfo.developers!!.forEach {
                    pomDeveloperSpec.developer { developer ->
                        it.email?.also {
                            developer.email.set(it)
                        }
                        it.name?.also {
                            developer.name.set(it)
                        }
                        it.organization?.also {
                            developer.organization.set(it)
                        }
                        it.organizationUrl?.also {
                            developer.organizationUrl.set(it)
                        }
                    }
                }
            }

            pomInfo.scm { mavenPomScm ->
                mavenPomScm.connection.set(getScmConnectionUrl(host, organizationId, artifactId))
                mavenPomScm.developerConnection.set(getScmDeveloperConnectionUrl(host, organizationId, artifactId))
                mavenPomScm.url.set((getScmUrl(host, organizationId, artifactId)))
            }

            pomInfo.description
        }
    }

    private fun getScmConnectionUrl(baseUrl: String, organizationId: String, artifactId: String): String {
        return "scm:git:git://$baseUrl/$organizationId/$artifactId.git"
    }

    private fun getScmDeveloperConnectionUrl(baseUrl: String, organizationId: String, artifactId: String): String {
        return "scm:git:ssh://$baseUrl:$organizationId/$artifactId.git"
    }

    private fun getScmUrl(baseUrl: String, organizationId: String, artifactId: String): String {
        return "https://$baseUrl/$organizationId/$artifactId/tree/master"
    }

    private fun getPublicationUrl(baseUrl: String, organizationId: String, artifactId: String): String {
        return "https://$baseUrl/$organizationId/$artifactId"
    }

    companion object {
        /**
         * Имя блока с настройками
         */
        const val extensionName: String = "javaArtifactPublishSettings"

        /**
         * Имя создаваемой публикации
         */
        const val publicationName: String = "mainArtifact"
    }
}

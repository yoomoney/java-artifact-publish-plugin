package ru.yoomoney.gradle.plugins.javapublishing

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.File
import java.nio.file.Paths

/**
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
class PublishingTest : AbstractReleaseTest() {

    @Test
    fun `should create nexus-public-plugin tasks when staging enabled`() {
        // given
        gradleProperties.writeText("version=1.0.0")
        buildFile.appendText("""
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://oss.sonatype.org/content/repositories/snapshots/"
            staging {
                enabled = true
                nexusUrl = "https://oss.sonatype.org/service/local/"
            }
        }
        """)

        // when
        val result = runTasksSuccessfully("tasks", "--all")

        // then
        // задачи публикации артефакта
        assertThat(result.output.lines().count { it.startsWith("publish") }, `is`(6))
        assertThat(result.output, containsString("publish"))
        assertThat(result.output, containsString("publishToMaven"))
        assertThat(result.output, containsString("publishToMavenLocal"))
        assertThat(result.output, containsString("publishAllPublicationsToMavenRepository"))
        assertThat(result.output, containsString("publishMainArtifactPublicationToMavenLocal"))
        assertThat(result.output, containsString("publishMainArtifactPublicationToMavenRepository"))

        // задачи управления staging репозиториями
        assertThat(result.output, containsString("initializeMavenStagingRepository"))
        assertThat(result.output, containsString("closeMavenStagingRepository"))
        assertThat(result.output, containsString("releaseMavenStagingRepository"))
        assertThat(result.output, containsString("closeAndReleaseMavenStagingRepository"))
    }

    @Test
    fun `should create publish tasks for multi-module projects when staging enabled`() {
        // given
        val key = javaClass.getResource("test_gpg_key.txt")?.readText()
        gradleProperties.writeText("version=1.0.0\n" +
                "signingPassword=123456\n" +
                "signingKey=$key")

        projectDir.newFile("settings.gradle").appendText("""
            include 'submodule1'
            include 'submodule2'
            include 'submodule3'
        """.trimIndent())

        buildFile.appendText("""
            
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://oss.sonatype.org/content/repositories/snapshots/"
            signing = true
            staging {
                enabled = true
                nexusUrl = "https://oss.sonatype.org/service/local/"
            }
        }
        
        subprojects {
            apply plugin: 'java'
            apply plugin: 'ru.yoomoney.gradle.plugins.java-artifact-publish-plugin'
            
            javaArtifactPublishSettings {
                artifactId = "test_artifact_id"
                groupId = "test_group_id"
                snapshotRepository = "https://oss.sonatype.org/content/repositories/snapshots/"
                signing = true
                staging {
                    enabled = true
                    nexusUrl = "https://oss.sonatype.org/service/local/"
                }
            }
        }
        """)

        // when
        val result = runTasksSuccessfully("tasks", "--all")

        // then
        assertThat(result.output, containsString("publishMainArtifactPublicationToMavenRepository"))
        assertThat(result.output, containsString("submodule1:publishMainArtifactPublicationToMavenRepository"))
        assertThat(result.output, containsString("submodule2:publishMainArtifactPublicationToMavenRepository"))
        assertThat(result.output, containsString("submodule3:publishMainArtifactPublicationToMavenRepository"))

        assertThat(result.output, containsString("signMainArtifactPublication"))
        assertThat(result.output, containsString("submodule1:signMainArtifactPublication"))
        assertThat(result.output, containsString("submodule2:signMainArtifactPublication"))
        assertThat(result.output, containsString("submodule3:signMainArtifactPublication"))
    }

    @Test
    fun `should create only maven-publish-plugin tasks when staging disabled`() {
        // given
        buildFile.appendText("""
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://yoomoney.ru/repository/snapshots/"
            releaseRepository = "https://yoomoney.ru/repository/releases/"
        }
        """)

        // when
        val result = runTasksSuccessfully("tasks", "--all")

        // then
        // задачи публикации артефакта
        assertThat(result.output.lines().count { it.startsWith("publish") }, `is`(5))
        assertThat(result.output, containsString("publish "))
        assertThat(result.output, containsString("publishToMavenLocal"))
        assertThat(result.output, containsString("publishAllPublicationsToMavenRepository"))
        assertThat(result.output, containsString("publishMainArtifactPublicationToMavenLocal"))
        assertThat(result.output, containsString("publishMainArtifactPublicationToMavenRepository"))

        // задачи управления staging репозиториями
        assertThat(result.output, not(containsString("initializeMavenStagingRepository")))
        assertThat(result.output, not(containsString("closeMavenStagingRepository")))
        assertThat(result.output, not(containsString("releaseMavenStagingRepository")))
        assertThat(result.output, not(containsString("closeAndReleaseMavenStagingRepository")))
    }

    @Test
    fun `should publish snapshot artefacts to snapshot repository when staging enabled`() {
        // given
        gradleProperties.writeText("version=1.0.0-SNAPSHOT")
        buildFile.appendText("""
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://oss.sonatype.org/content/repositories/snapshots/"
            staging {
                enabled = true
                nexusUrl = "https://oss.sonatype.org/service/local/"
            }
        }
        
        task printPublishingRepository {
            def publishing = project.extensions.getByName("publishing")
            doLast { println publishing.repositories.maven.url }
        }
        """)

        // when
        val result = runTasksSuccessfully("printPublishingRepository")

        // then
        assertThat(result.output, containsString("https://oss.sonatype.org/content/repositories/snapshots/"))
    }

    @Test
    fun `should publish`() {
        buildFile.appendText("""
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://yoomoney.ru/repository/snapshots/"
            releaseRepository = "https://yoomoney.ru/repository/releases/"
        }
        """)
        val result = runTasksSuccessfully("build", "pTML", "--info")
        assertThat(result.output, not(containsString("signMainArtifactPublication")))
    }

    @Test
    fun `should not signing snapshot version`() {
        buildFile.appendText("""
            
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://yoomoney.ru/repository/snapshots/"
            releaseRepository = "https://yoomoney.ru/repository/releases/"
            signing = true
        }
        """)
        val result = runTasksSuccessfully("build", "pTML", "--info", "--stacktrace")

        assertThat(result.output, not(containsString("signMainArtifactPublication")))
    }

    @Test
    fun `should signing`() {
        val key = File(javaClass.getResource("test_gpg_key.txt").toURI()).readText()
        gradleProperties.writeText("version=1.0.0\n" +
                "signingPassword=123456\n" +
                "signingKey=$key")

        buildFile.appendText("""
            
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://yoomoney.ru/repository/snapshots/"
            releaseRepository = "https://yoomoney.ru/repository/releases/"
            signing = true
            publicationAdditionalInfo {
                addInfo = true
                organizationUrl = "https://github.com/yoomoney"
                
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
                    developer {
                        name = 'Petr'
                        email = 'petr@test.ru'
                        organization = 'test'
                        organizationUrl = 'https://test.ru'
                    }
                }
                description = "description"
            }
        }
        """)
        val result = runTasksSuccessfully("build", "pTML", "--info", "--stacktrace")

        val pom = Paths.get(buildFile.parentFile.absolutePath, "/build/publications/mainArtifact/pom-default.xml").toFile()
                .readText()

        assertThat(pom, containsString("<url>https://github.com/yoomoney/test_artifact_id</url>"))
        assertThat(pom, containsString("<email>ivan@test.ru</email>"))
        assertThat(pom, containsString("<name>Petr</name>"))
        assertThat(pom, containsString("<connection>scm:git:git://github.com/yoomoney/test_artifact_id.git</connection>"))
        assertThat(pom, containsString("<developerConnection>scm:git:ssh://github.com:yoomoney/test_artifact_id.git</developerConnection>"))
        assertThat(pom, containsString("<name>MIT License</name>"))

        assertThat(result.output, containsString("signMainArtifactPublication"))
    }
}
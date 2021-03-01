package ru.yoomoney.gradle.plugins.javapublishing

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import java.nio.file.Paths

/**
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
class PublishingTest : AbstractReleaseTest() {

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
        MatcherAssert.assertThat(result.output, CoreMatchers.not(CoreMatchers.containsString("signMainArtifactPublication")))

    }

    @Test
    fun `should signing`() {
        buildFile.appendText("""
            
        javaArtifactPublishSettings {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
            snapshotRepository = "https://yoomoney.ru/repository/snapshots/"
            releaseRepository = "https://yoomoney.ru/repository/releases/"
            signing = true
            publicationAdditionalInfo {
                addInfo = true
                organizationUrl = "https://github.com/yoomoney-gradle-plugins"
                
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

        MatcherAssert.assertThat(pom, CoreMatchers
                .containsString("<url>https://github.com/yoomoney-gradle-plugins/test_artifact_id</url>"))
        MatcherAssert.assertThat(pom, CoreMatchers.containsString("<email>ivan@test.ru</email>"))
        MatcherAssert.assertThat(pom, CoreMatchers.containsString("<name>Petr</name>"))
        MatcherAssert.assertThat(pom, CoreMatchers.
        containsString("<connection>scm:git:git://github.com/yoomoney-gradle-plugins/test_artifact_id.git</connection>"))
        MatcherAssert.assertThat(pom, CoreMatchers
                .containsString("<developerConnection>scm:git:ssh://github.com:yoomoney-gradle-plugins/test_artifact_id.git</developerConnection>"))
        MatcherAssert.assertThat(pom, CoreMatchers.containsString("<name>MIT License</name>"))

        MatcherAssert.assertThat(result.output, CoreMatchers.containsString("signMainArtifactPublication"))
    }
}
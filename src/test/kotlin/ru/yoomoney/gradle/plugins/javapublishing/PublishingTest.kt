package ru.yoomoney.gradle.plugins.javapublishing

import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

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
        }
        """)
        val result = runTasksSuccessfully("build", "pTML", "--info", "--stacktrace")

        MatcherAssert.assertThat(result.output, CoreMatchers.containsString("signMainArtifactPublication"))
    }
}
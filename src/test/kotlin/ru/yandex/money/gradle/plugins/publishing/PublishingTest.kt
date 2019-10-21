package ru.yandex.money.gradle.plugins.publishing

import org.junit.Test

/**
 * @author Oleg Kandaurov
 * @since 21.10.2019
 */
class PublishingTest : AbstractReleaseTest() {

    @Test
    fun `should publish`() {
        buildFile.appendText("""
        artifactPublish {
            artifactId = "test_artifact_id"
            groupId = "test_group_id"
        }
        """)
        runTasksSuccessfully("build", "pTML", "--info")
    }
}
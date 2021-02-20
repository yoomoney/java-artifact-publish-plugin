package ru.yoomoney.gradle.plugins.javapublishing

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Paths

abstract class AbstractReleaseTest {

    @get:Rule
    val projectDir = TemporaryFolder()

    lateinit var buildFile: File
    lateinit var gradleProperties: File

    @Before
    fun setup() {
        buildFile = projectDir.newFile("build.gradle")

        buildFile.writeText("""
            plugins {
                id 'java'
                id 'ru.yoomoney.gradle.plugins.java-artifact-publish-plugin'
            }

        """.trimIndent())

        val srcFolder = projectDir.newFolder("src", "main", "java")
        val classFile = Paths.get(srcFolder.absolutePath, "HelloWorld.java")
        classFile.toFile().writeText("""
            public class HelloWorld {}
        """.trimIndent())

        gradleProperties = projectDir.newFile("gradle.properties")
        gradleProperties.writeText("version=1.0.0-SNAPSHOT")
    }

    fun runTasksSuccessfully(vararg tasks: String): BuildResult {
        return GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments(tasks.toList())
                .withPluginClasspath()
                .forwardOutput()
                .withDebug(true)
                .build()
    }

    fun runTasksFail(vararg tasks: String): BuildResult {
        return GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments(tasks.toList())
                .withPluginClasspath()
                .forwardOutput()
                .withDebug(true)
                .buildAndFail()
    }
}
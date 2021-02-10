package com.evergreen.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import com.evergreen.android.util.Logger
import com.evergreen.android.util.ReflectUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.get

class SoSourcePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val android = project.extensions["android"] as AppExtension
    android.applicationVariants.all {
      val soSourceTaskName = "soSourceFor${name.capitalize()}"
      if (!isTargetTask(project, soSourceTaskName)) {
        Logger.info("soSource is not target task, ignore!")
        return@all
      }

      val transformTaskName = "${TRANSFORM_NATIVE_LIB_TASK}For${name.capitalize()}"
      val transformTask = project.tasks.getByName(transformTaskName) as? TransformTask

      if (transformTask == null) {
        Logger.info("$transformTask is not found")
        return@all
      }

      ReflectUtils.setField(
        transformTask, "transform",
        SoSourceTransform(transformTask.transform)
      )

      val soSourceTask = project.tasks.create(
        soSourceTaskName,
        Task::class.java
      )

      soSourceTask.dependsOn(transformTask)

      // make task always run
      transformTask.outputs.upToDateWhen { false }
    }
  }

  private fun isTargetTask(project: Project, taskName: String): Boolean {
    return project.gradle.startParameter.taskNames.any {
      it.contains(taskName)
    }
  }

  companion object {
    private const val TRANSFORM_NATIVE_LIB_TASK = "transformNativeLibsWithMergeJniLibs"
  }
}
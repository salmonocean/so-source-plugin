package com.evergreen.android.util

import org.gradle.api.Project
import org.gradle.api.logging.Logging

internal object Logger {

  private const val PREFIX_TAG = "[soSource] "

  private val mLogger = Logging.getLogger(Project::class.java)

  fun lifecycle(msg: String) {
    mLogger.lifecycle(fillMsg(msg))
  }

  fun info(msg: String) {
    mLogger.info(fillMsg(msg))
  }

  private fun fillMsg(msg: String): String {
    return "$PREFIX_TAG $msg"
  }
}
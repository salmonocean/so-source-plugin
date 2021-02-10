package com.evergreen.android

import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.SecondaryFile
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.internal.pipeline.TransformTask
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.evergreen.android.util.Logger
import com.evergreen.android.util.ReflectUtils
import java.io.File
import java.util.regex.Pattern

class SoSourceTransform(
  private val mDelegateTransform: Transform

) : Transform() {

  override fun getName(): String {
    return mDelegateTransform.name
  }

  override fun getInputTypes(): MutableSet<ContentType> {
    return mDelegateTransform.inputTypes
  }

  override fun isIncremental(): Boolean {
    return mDelegateTransform.isIncremental
  }

  override fun getScopes(): MutableSet<in Scope> {
    return mDelegateTransform.scopes
  }

  override fun applyToVariant(variant: VariantInfo?): Boolean {
    return mDelegateTransform.applyToVariant(variant)
  }

  override fun getSecondaryFiles(): MutableCollection<SecondaryFile> {
    return mDelegateTransform.secondaryFiles
  }

  override fun isCacheable(): Boolean {
    return mDelegateTransform.isCacheable
  }

  override fun getOutputTypes(): MutableSet<ContentType> {
    return mDelegateTransform.outputTypes
  }

  override fun getReferencedScopes(): MutableSet<in Scope> {
    return mDelegateTransform.referencedScopes
  }

  override fun getSecondaryDirectoryOutputs(): MutableCollection<File> {
    return mDelegateTransform.secondaryDirectoryOutputs
  }

  override fun getParameterInputs(): MutableMap<String, Any> {
    return mDelegateTransform.parameterInputs
  }

  override fun getSecondaryFileOutputs(): MutableCollection<File> {
    return mDelegateTransform.secondaryFileOutputs
  }

  override fun transform(transformInvocation: TransformInvocation?) {
    mDelegateTransform.transform(transformInvocation)

    if (transformInvocation == null) {
      return
    }

    val inputMap = mutableMapOf<String, MutableList<String>>()
    val soMap = mutableMapOf<String, MutableList<String>>()

    generateSoMap(transformInvocation, inputMap, soMap)
    printSoSource(transformInvocation, inputMap, soMap)
  }

  override fun transform(
    context: Context?, inputs: MutableCollection<TransformInput>?,
    referencedInputs: MutableCollection<TransformInput>?, outputProvider: TransformOutputProvider?,
    isIncremental: Boolean
  ) {
    mDelegateTransform.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
  }

  override fun getSecondaryFileInputs(): MutableCollection<File> {
    return mDelegateTransform.secondaryFileInputs
  }

  private fun generateSoMap(
    transformInvocation: TransformInvocation,
    inputMap: MutableMap<String, MutableList<String>>,
    soMap: MutableMap<String, MutableList<String>>
  ) {
    transformInvocation.inputs.forEach {
      it.jarInputs.forEach { input ->
        inputMap.getOrPut(input.file.absolutePath) {
          mutableListOf()
        }
          .add(input.name)
      }

      it.directoryInputs.forEach { input ->
        inputMap.getOrPut(input.file.absolutePath) {
          mutableListOf()
        }
          .add(input.name)

        input.file.walk()
          .filter { file ->
            file.isFile
          }
          .forEach { file ->
            val relativePath = "lib/" +
                com.android.builder.files.RelativeFile(input.file, file).getRelativePath()

            val matcher = JAR_ABI_PATTERN.matcher(relativePath)
            if (matcher.matches()) {
              soMap.getOrPut(relativePath) {
                mutableListOf()
              }
                .add(input.name)
            }
          }
      }
    }
  }

  private fun printSoSource(
    transformInvocation: TransformInvocation,
    inputMap: MutableMap<String, MutableList<String>>,
    soMap: MutableMap<String, MutableList<String>>
  ) {
    val transformTask = transformInvocation.context as TransformTask
    val resultFile = File(
      "${transformTask.project.buildDir.absolutePath}/output/so_source_${transformTask.variantName}.txt"
    )
    resultFile.delete()
    resultFile.parentFile?.mkdirs()

    print(resultFile, "****** origin so: ******")
    soMap.forEach { (soName, jarName) ->
      print(resultFile, "$soName ======> $jarName")
    }
    print(resultFile, "****** ****** ******")

    print(resultFile, "****** final so pick: ******")
    val state = ReflectUtils.callMethod<com.android.builder.merge.IncrementalFileMergerState>(
      mDelegateTransform, "loadMergeState"
    )
    val origin: ImmutableMap<String, ImmutableList<String>> = ReflectUtils.getField(state, "origin")
    origin.toSortedMap().forEach {
      val soName = it.key

      it.value.forEach { soPath ->
        print(resultFile, "$soName ======> ${inputMap[soPath]}")
      }
    }
    print(resultFile, "****** ****** ******")
  }

  private fun print(file: File, content: String) {
    file.appendText("$content\n")

    Logger.info(content)
  }

  companion object {
    private val JAR_ABI_PATTERN =
      Pattern.compile("lib/([^/]+)/[^/]+")
  }
}
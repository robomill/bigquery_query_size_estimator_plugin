package net.chakmidlot.jetbrains.bigquery.plugin

import com.google.cloud.bigquery.*
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction


class BigQueryDryRun : DumbAwareAction() {
  override fun actionPerformed(event: AnActionEvent) {
    val editor = event.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      val queryText = if (editor.selectionModel.selectedText != null) {
        editor.selectionModel.selectedText
      }
      else {
        editor.document.text
      }

      try {
        val size = queryDryRun(queryText!!)
        Notifications.Bus.notify(
          Notification(
            Notifications.SYSTEM_MESSAGES_GROUP_ID, "Query estimation",
            "Data to read: $size", NotificationType.INFORMATION
          )
        )
      }
      catch (e: BigQueryException) {
        Notifications.Bus.notify(Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Query estimation failed",
          e.message!!, NotificationType.ERROR))
      }
    }
  }

  private fun queryDryRun(query: String): String {
    val bigquery: BigQuery = BigQueryOptions.getDefaultInstance().service
    val queryConfig: QueryJobConfiguration =
      QueryJobConfiguration.newBuilder(query).setDryRun(true).setUseQueryCache(false).build()
    val job: Job = bigquery.create(JobInfo.of(queryConfig))
    val statistics: JobStatistics.QueryStatistics = job.getStatistics()

    val size = statistics.totalBytesProcessed * 1.0

    return when {
      size < 1 shl 10 -> "%f B".format(size)
      size < 1 shl 20 -> "%.1f KB".format(size / (1 shl 10))
      size < 1 shl 30 -> "%.1f MB".format(size / (1 shl 20))
      else -> "%,.1f GB".format(size / (1 shl 30))
    }
  }
}

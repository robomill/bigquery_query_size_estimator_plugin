package net.chakmidlot.jetbrains.bigquery.plugin

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.bigquery.*
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import java.io.File


class BigQueryDryRun : DumbAwareAction() {

    private val price = 6.0 / (1L shl 40);

    override fun actionPerformed(event: AnActionEvent) {
        try {
            val query = getQuery(event);
            val size = queryDryRun(query)

            Notifications.Bus.notify(
                Notification(
                    Notifications.SYSTEM_MESSAGES_GROUP_ID, "Query estimation",
                    size, NotificationType.INFORMATION
                )
            )
        } catch (e: Exception) {
            Notifications.Bus.notify(
                Notification(
                    Notifications.SYSTEM_MESSAGES_GROUP_ID, "Query estimation failed",
                    e.message!!, NotificationType.ERROR
                )
            )
        }
    }

    private fun getQuery(event: AnActionEvent): String {
        val editor = event.getData(CommonDataKeys.EDITOR)

        if (editor != null) {
            return if (editor.selectionModel.selectedText != null) {
                editor.selectionModel.selectedText!!
            } else {
                editor.document.text
            }
        }
        throw Exception("Open a query in the editor")
    }

    private fun queryDryRun(query: String): String {
        val keyPath = SettingsState.getInstance().state?.keyPath
        val applicationDefault = SettingsState.getInstance().state?.applicationDefaultAuthentication
        val bigquery: BigQuery = if (applicationDefault == false) {
            if (keyPath == null || keyPath == "") {
                throw Exception("Set GCP service key path in settings:\nFile | Settings | Tools | BigQuery GCP key")
            }

            val credentialsText = File(keyPath).inputStream()
            val credentials = ServiceAccountCredentials.fromStream(credentialsText)

            BigQueryOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(credentials.projectId)
                .build().service;
        } else {
            BigQueryOptions.getDefaultInstance().service
        }
        val queryConfig: QueryJobConfiguration =
            QueryJobConfiguration.newBuilder(query).setDryRun(true).setUseQueryCache(false).build()
        val job: Job = bigquery.create(JobInfo.of(queryConfig))
        val statistics: JobStatistics.QueryStatistics = job.getStatistics()

        val size = statistics.totalBytesProcessed * 1.0

        val formattedSize = when {
            size < 1 shl 10 -> "%f B".format(size)
            size < 1 shl 20 -> "%.1f KB".format(size / (1 shl 10))
            size < 1 shl 30 -> "%.1f MB".format(size / (1 shl 20))
            else -> "%,.1f GB".format(size / (1 shl 30))
        }

        val cost = price * size

        val formattedCost = when {
            cost < 0.01 -> "less than 1 cent"
            else -> "€ %,.2f".format(cost)
        }
        return "Data to read: $formattedSize <br /> Cost: $formattedCost"
    }
}

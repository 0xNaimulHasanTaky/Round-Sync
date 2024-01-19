package ca.pkay.rcloneexplorer.notifications.prototypes

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncRestartAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification
import ca.pkay.rcloneexplorer.util.FLog
import ca.pkay.rcloneexplorer.util.NotificationUtils
import ca.pkay.rcloneexplorer.workmanager.SyncWorker
import ca.pkay.rcloneexplorer.workmanager.SyncWorker.Companion.EXTRA_TASK_ID
import de.felixnuesse.extract.extensions.tag
import de.felixnuesse.extract.notifications.DiscardChannels
import java.util.UUID

abstract class WorkerNotification(var mContext: Context) {

    abstract val CHANNEL_ID: String


    val CHANNEL_SUCCESS_ID = this.CHANNEL_ID +"_success"
    open val CHANNEL_FAIL_ID = this.CHANNEL_ID+"_fail"

    val GROUP_ID = "de.felixnuesse.extract.taskworker.group"
    val GROUP_DESCRIPTION = mContext.getString(R.string.workernotification_group_description)



    // Todo: Either make all resources, or all strings. Not both.
    abstract val titleStartingSync: Int
    abstract val serviceOngoingTitle: Int
    abstract val serviceFailed: Int
    abstract val serviceCancelled: Int
    abstract val serviceSuccess: Int


    abstract val channel_ongoing_title: String
    abstract val channel_ongoing_description: String
    abstract val channel_success_title: String
    abstract val channel_success_description: String
    abstract val channel_failed_title: String
    abstract val channel_failed_description: String


    abstract val PERSISTENT_NOTIFICATION_ID: Int

    companion object {
        const val CANCEL_ID_NOTSET = "CANCEL_ID_NOTSET"
    }

    // Check if those can stay the same when channels are muted
    private val OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP"
    private val OPERATION_SUCCESS_GROUP = "ca.pkay.rcexplorer.OPERATION_SUCCESS_GROUP"


    private var mCancelUnsetId: UUID = UUID.randomUUID()
    private var mCancelId: UUID = mCancelUnsetId


    fun generateChannels() {
        DiscardChannels.discard(mContext)
        GenericSyncNotification(mContext).setNotificationChannel(
                CHANNEL_ID,
                channel_ongoing_title,
                channel_ongoing_description,
                GROUP_ID,
                GROUP_DESCRIPTION
        )
        GenericSyncNotification(mContext).setNotificationChannel(
                CHANNEL_SUCCESS_ID,
                channel_success_title,
                channel_success_description,
                GROUP_ID,
                GROUP_DESCRIPTION
        )
        GenericSyncNotification(mContext).setNotificationChannel(
                CHANNEL_FAIL_ID,
                channel_failed_title,
                channel_failed_description,
                GROUP_ID,
                GROUP_DESCRIPTION
        )
    }


    fun setCancelId(id: UUID) {
        mCancelId = id
    }

    fun showFailedNotification(
        title: String,
        content: String,
        notificationId: Int,
        taskid: Long
    ) {
        val i = Intent(mContext, SyncRestartAction::class.java)
        i.putExtra(EXTRA_TASK_ID, taskid)

        val retryPendingIntent = PendingIntent.getService(mContext, taskid.toInt(), i,
            GenericSyncNotification.getFlags()
        )
        val builder = NotificationCompat.Builder(mContext, CHANNEL_FAIL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(serviceFailed))
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            .setGroup(OPERATION_FAILED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_refresh,
                mContext.getString(R.string.retry_failed_sync),
                retryPendingIntent
            )
        NotificationUtils.createNotification(mContext, notificationId, builder.build())
    }

    fun showCancelledNotification(
        title: String,
        content: String,
        notificationId: Int,
        taskid: Long
    ) {
        val i = Intent(mContext, SyncRestartAction::class.java)
        i.putExtra(EXTRA_TASK_ID, taskid)

        val retryPendingIntent = PendingIntent.getService(mContext, taskid.toInt(), i,
            GenericSyncNotification.getFlags()
        )
        val builder = NotificationCompat.Builder(mContext, CHANNEL_FAIL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(serviceCancelled))
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            .setGroup(OPERATION_FAILED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_refresh,
                mContext.getString(R.string.retry_failed_sync),
                retryPendingIntent
            )
        NotificationUtils.createNotification(mContext, notificationId, builder.build())
    }

    fun showSuccessNotification(title: String, content: String, notificationId: Int) {
        val builder = NotificationCompat.Builder(mContext, CHANNEL_SUCCESS_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setContentTitle(mContext.getString(serviceSuccess, title))
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    content
                )
            )
            .setGroup(OPERATION_SUCCESS_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        NotificationUtils.createNotification(mContext, notificationId, builder.build())
    }

    fun updateNotification(
        title: String,
        content: String,
        bigTextArray: ArrayList<String>,
        percent: Int,
        notificationId: Int
    ): Notification? {
        if(content.isBlank()){
            FLog.e(tag(), "Missing notification content!")
            return null
        }

        val builder = GenericSyncNotification(mContext).updateGenericNotification(
            mContext.getString(serviceOngoingTitle, title),
            content,
            R.drawable.ic_twotone_rounded_cloud_sync_24,
            bigTextArray,
            percent,
            SyncWorker::class.java,
            null,
            CHANNEL_ID
        )

        if(mCancelId != mCancelUnsetId) {

            val intent = WorkManager.getInstance(mContext)
                .createCancelPendingIntent(mCancelId)

            builder.clearActions()
            builder.addAction(
                R.drawable.ic_cancel_download,
                mContext.getString(R.string.cancel),
                intent
            )
        }

        return builder.build()
    }

    fun cancelSyncNotification(notificationId: Int) {
        val notificationManagerCompat = NotificationManagerCompat.from(mContext)
        notificationManagerCompat.cancel(notificationId)
    }
}

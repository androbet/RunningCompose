package com.nullpointer.runningcompose.core.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.annotation.PluralsRes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import com.nullpointer.runningcompose.models.types.MetricType
import com.nullpointer.runningcompose.models.types.MetricType.Kilo
import com.nullpointer.runningcompose.models.types.MetricType.Meters
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Long.toFullFormat(context: Context): String {
    val base = "dd/MM/yyyy hh:mm"
    val newPattern = if (DateFormat.is24HourFormat(context)) base else "$base a"
    val sdf = SimpleDateFormat(newPattern, Locale.getDefault())
    return sdf.format(this)
}

fun Long.toDateOnlyTime(context: Context): String {
    val base = "hh:mm"
    val newPattern = if (DateFormat.is24HourFormat(context)) base else "$base a"
    val sdf = SimpleDateFormat(newPattern, Locale.getDefault())
    return sdf.format(this)
}

fun Long.toDateFormat(): String {
    val base = "dd/MM/yyyy"
    val sdf = SimpleDateFormat(base, Locale.getDefault())
    return sdf.format(this)
}

fun Float.toMeters(metricType: MetricType, precision: Int = 2) =
    when (metricType) {
        Meters -> "%.${precision}f m".format(this)
        Kilo -> "%.${precision}f km".format(this / 1000f)
    }


fun Float.toAVGSpeed(metricType: MetricType, precision: Int = 2): String =
    when (metricType) {
        Meters -> "%.${precision}f m/s".format(this)
        Kilo -> "%.${precision}f km/h".format(this * 3.6)
    }


fun Float.toCaloriesBurned(metricType: MetricType, precision: Int = 2): String =
    when (metricType) {
        Meters -> "%.${precision}f cal".format(this)
        Kilo -> "%.${precision}f kcal".format(this / 1000)
    }

fun Long.toFullFormatTime(includeMillis: Boolean): String {
    var milliseconds = this
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    milliseconds -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
    if (!includeMillis) {
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }
    milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
    milliseconds /= 10
    return "${if (hours < 10) "0" else ""}$hours:" +
            "${if (minutes < 10) "0" else ""}$minutes:" +
            "${if (seconds < 10) "0" else ""}$seconds:" +
            "${if (milliseconds < 10) "0" else ""}$milliseconds"
}
fun Context.getPlural(@PluralsRes stringQuality: Int,quality:Int): String {
    return resources.getQuantityString(stringQuality,quality,quality)
}

//get fields actions and remove all using empty array
fun NotificationCompat.Builder.clearActionsNotification() {
    javaClass.getDeclaredField("mActions").apply {
        isAccessible = true
        set(this@clearActionsNotification, ArrayList<NotificationCompat.Action>())
    }
}

fun Context.getNotifyManager(): NotificationManager{
   return  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}

val Context.correctFlag:Int get() {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}


@Composable
inline fun <reified VM : ViewModel> shareViewModel(): VM {
    val activity = LocalContext.current as ComponentActivity
    return hiltViewModel(activity)
}


fun ViewModel.launchSafeIO(
    blockBefore: suspend CoroutineScope.() -> Unit = {},
    blockAfter: suspend CoroutineScope.(Boolean) -> Unit = {},
    blockException: suspend CoroutineScope.(Exception) -> Unit = {},
    blockIO: suspend CoroutineScope.() -> Unit,
): Job {
    var isForCancelled = false
    return viewModelScope.launch {
        try {
            blockBefore()
            withContext(Dispatchers.IO) { blockIO() }
        } catch (e: Exception) {
            when (e) {
                is CancellationException -> {
                    isForCancelled = true
                    throw e
                }
                else -> blockException(e)
            }
        } finally {
            blockAfter(isForCancelled)
        }
    }
}

val AsyncImagePainter.isSuccess get() = state is AsyncImagePainter.State.Success

@Composable
fun getGrayColor(): Color {
    return (if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray)
}

fun Modifier.myShimmer(
    shimmer: Shimmer,
): Modifier = composed {
    shimmer(shimmer).background(getGrayColor())
}
package com.nullpointer.runningcompose.ui.screens.runs

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.nullpointer.runningcompose.R
import com.nullpointer.runningcompose.ui.screens.empty.LottieContainer


@Composable
fun DialogExplainPermission(
    actionHidden: () -> Unit,
    actionAccept: () -> Unit,
    changeFirstRequest: () -> Unit,
    isFirstRequestPermission: Boolean,
) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = actionHidden,
        confirmButton = {
            Button(onClick = {
                if (isFirstRequestPermission) {
                    actionAccept()
                    actionHidden()
                    changeFirstRequest()
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                    actionHidden()
                }
            }) {
                Text(stringResource(id = R.string.action_accept))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = actionHidden) {
                Text(stringResource(id = R.string.action_cancel))
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val idText =
                    if (isFirstRequestPermission) R.string.need_permissions_tracking else R.string.setting_permissions_tracking
                Text(stringResource(id = idText),
                    style = MaterialTheme.typography.h6)
                LottieContainer(modifier = Modifier.size(250.dp),
                    animation = if (isFirstRequestPermission) R.raw.location else R.raw.setting)
            }
        }
    )
}

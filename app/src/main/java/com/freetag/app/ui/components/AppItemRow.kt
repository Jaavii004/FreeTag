package com.freetag.app.ui.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.freetag.app.data.AppCategory
import com.freetag.app.data.AppInfo
import com.freetag.app.ui.theme.AlertRed
import com.freetag.app.ui.theme.EmeraldDark
import com.freetag.app.ui.theme.EmeraldPrimary
import com.freetag.app.ui.theme.PureWhite
import com.freetag.app.ui.theme.SurfaceBorder
import com.freetag.app.ui.theme.SurfaceDark
import com.freetag.app.ui.theme.SurfaceElevated
import com.freetag.app.ui.theme.TextPrimary
import com.freetag.app.ui.theme.TextSecondary
import com.freetag.app.ui.theme.TextTertiary

/**
 * List row representing an installed application for blocking.
 */
@Composable
fun AppItemRow(
    app: AppInfo,
    isAppLockedMode: Boolean,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(
                width = 1.dp,
                color = if (app.isBlocked) EmeraldDark else SurfaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !isAppLockedMode) {
                onToggle(app.packageName)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (app.icon != null) {
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setImageDrawable(app.icon)
                        }
                    },
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles and Category
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 15.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.category != AppCategory.OTHER && app.category != AppCategory.ALL) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = app.category.title,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (isAppLockedMode) {
            // Anti-cheat indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (app.isBlocked) AlertRed.copy(alpha = 0.2f) else SurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Protegido",
                    tint = if (app.isBlocked) AlertRed else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // Interactive toggle
            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle(app.packageName) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = EmeraldPrimary,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = SurfaceElevated
                )
            )
        }
    }
}

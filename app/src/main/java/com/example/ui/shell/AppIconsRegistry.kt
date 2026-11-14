package com.example.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.VirtualAppId

fun VirtualAppId.iconVector(): ImageVector = when (this) {
    VirtualAppId.PHONE -> Icons.Rounded.Phone
    VirtualAppId.CONTACTS -> Icons.Rounded.Contacts
    VirtualAppId.MESSAGES -> Icons.AutoMirrored.Rounded.Message
    VirtualAppId.BROWSER -> Icons.Rounded.Language
    VirtualAppId.CAMERA -> Icons.Rounded.CameraAlt
    VirtualAppId.GALLERY -> Icons.Rounded.PhotoLibrary
    VirtualAppId.MAPS -> Icons.Rounded.Map
    VirtualAppId.CLOCK -> Icons.Rounded.AccessTimeFilled
    VirtualAppId.CALENDAR -> Icons.Rounded.CalendarMonth
    VirtualAppId.NOTES -> Icons.Rounded.Description
    VirtualAppId.CALCULATOR -> Icons.Rounded.Calculate
    VirtualAppId.FILES -> Icons.Rounded.Folder
    VirtualAppId.DASHBOARD -> Icons.Rounded.PhoneAndroid
    VirtualAppId.NETWORK -> Icons.Rounded.Wifi
    VirtualAppId.SETTINGS -> Icons.Rounded.Settings
}

@Composable
fun VirtualAppIconTile(
    appId: VirtualAppId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
    showLabel: Boolean = true,
    labelColor: Color = Color.White,
    showRemoveBadge: Boolean = false,
    onRemoveClick: (() -> Unit)? = null
) {
    val baseColor = appId.accentColor
    val topHighlight = Color(
        red = (baseColor.red + 0.16f).coerceAtMost(1f),
        green = (baseColor.green + 0.16f).coerceAtMost(1f),
        blue = (baseColor.blue + 0.16f).coerceAtMost(1f),
        alpha = 1f
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("app_icon_${appId.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .shadow(6.dp, RoundedCornerShape(18.dp), clip = false)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(topHighlight, baseColor)
                        )
                    )
                    .border(
                        width = 0.8.dp,
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = appId.iconVector(),
                    contentDescription = appId.title,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize * 0.52f)
                )
            }
            if (showRemoveBadge && onRemoveClick != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable(onClick = onRemoveClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Unpin ${appId.title}",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = appId.title,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

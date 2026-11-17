package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CloudRegion
import com.example.data.GamerProfile
import com.example.data.GamingPlatform
import com.example.data.StreamQuality
import com.example.data.UiState
import com.example.ui.components.FreePassBadge
import com.example.ui.components.GhostGradientButton
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostDiagonalBrush
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceElevated
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxGreenDark

@Composable
fun ProfileScreen(
    profileState: UiState<GamerProfile?>,
    pinnedCount: Int,
    savesCount: Int,
    onSaveProfile: (String, GamingPlatform, StreamQuality, CloudRegion) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = (profileState as? UiState.Success)?.data

    var gamertag by remember { mutableStateOf(profile?.gamertag ?: "GhostPlayer") }
    var platform by remember { mutableStateOf(GamingPlatform.fromCode(profile?.preferredPlatform)) }
    var quality by remember { mutableStateOf(StreamQuality.fromCode(profile?.preferredQuality)) }
    var region by remember { mutableStateOf(CloudRegion.fromCode(profile?.preferredRegion)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile) {
        if (profile != null) {
            gamertag = profile.gamertag
            platform = GamingPlatform.fromCode(profile.preferredPlatform)
            quality = StreamQuality.fromCode(profile.preferredQuality)
            region = CloudRegion.fromCode(profile.preferredRegion)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GhostBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "GHOST GAMER PROFILE",
                style = MaterialTheme.typography.headlineLarge,
                color = GhostTextPrimary
            )

            // Free Unlimited Cloud Pass Membership Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GhostSurface,
                border = BorderStroke(1.5.dp, GhostDiagonalBrush),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = XboxGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "GHOST FREE PASS",
                                style = MaterialTheme.typography.titleLarge,
                                color = GhostTextPrimary
                            )
                        }
                        FreePassBadge(label = "ACTIVE • $0.00")
                    }

                    Text(
                        text = "Unlimited free Xbox Cloud Gaming including Premium Showcase titles like Forza Horizon 6, cross-platform squads, and instant cloud save sync — no payment required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            label = "PLAYTIME",
                            value = "${profile?.totalPlayMinutes ?: 0L}m",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "CLOUD SAVES",
                            value = "$savesCount",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "PINNED",
                            value = "$pinnedCount",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (profileState is UiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GhostBlue)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GhostSurface,
                    border = BorderStroke(1.dp, GhostBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "CLOUD RIG PREFERENCES",
                            style = MaterialTheme.typography.titleMedium,
                            color = GhostCyan
                        )

                        OutlinedTextField(
                            value = gamertag,
                            onValueChange = {
                                gamertag = it.take(30)
                                errorMsg = null
                            },
                            label = { Text("Gamertag (2–30 chars)") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = GhostCyan)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GhostBlue,
                                unfocusedBorderColor = GhostBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_gamertag_input")
                        )

                        Text("Preferred Cross-Play Input", style = MaterialTheme.typography.labelLarge, color = GhostTextPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GamingPlatform.entries) { item ->
                                FilterChip(
                                    selected = platform == item,
                                    onClick = { platform = item },
                                    label = { Text(item.label) }
                                )
                            }
                        }

                        Text("Cloud Stream Quality", style = MaterialTheme.typography.labelLarge, color = GhostTextPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(StreamQuality.entries) { q ->
                                FilterChip(
                                    selected = quality == q,
                                    onClick = { quality = q },
                                    label = { Text(q.label) }
                                )
                            }
                        }

                        Text("Ghost Edge Server Region", style = MaterialTheme.typography.labelLarge, color = GhostTextPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CloudRegion.entries.filter { it != CloudRegion.GLOBAL }) { r ->
                                FilterChip(
                                    selected = region == r,
                                    onClick = { region = r },
                                    label = { Text(r.label) }
                                )
                            }
                        }

                        if (errorMsg != null) {
                            Text(text = errorMsg!!, style = MaterialTheme.typography.bodySmall, color = GhostRed)
                        }

                        GhostGradientButton(
                            text = "SAVE & SYNC PROFILE",
                            onClick = {
                                val trimmed = gamertag.trim()
                                if (trimmed.length < 2) {
                                    errorMsg = "Gamertag must be at least 2 characters."
                                } else {
                                    onSaveProfile(trimmed, platform, quality, region)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CloudDone,
                                    contentDescription = null,
                                    tint = GhostTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onSignOut,
                border = BorderStroke(1.dp, GhostRed.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = GhostRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.action_sign_out),
                    style = MaterialTheme.typography.labelLarge,
                    color = GhostRed
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GhostSurfaceElevated,
        border = BorderStroke(1.dp, GhostBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = GhostCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GhostTextSecondary
            )
        }
    }
}

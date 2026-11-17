package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.attemptAutoSignIn
import com.example.data.onGoogleSignInClicked
import com.example.ui.components.FreePassBadge
import com.example.ui.components.GhostBrandMark
import com.example.ui.components.GhostGradientButton
import com.example.ui.theme.GhostBackground
import com.example.ui.theme.GhostBlue
import com.example.ui.theme.GhostBorder
import com.example.ui.theme.GhostCyan
import com.example.ui.theme.GhostRed
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostTextMuted
import com.example.ui.theme.GhostTextPrimary
import com.example.ui.theme.GhostTextSecondary
import com.example.ui.theme.GhostVoid
import com.example.ui.theme.XboxGreen

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = { /* Wait for interactive Google Sign-In */ },
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GhostVoid)
    ) {
        // Atmospheric full-bleed Forza Horizon 6 neon backdrop
        Image(
            painter = painterResource(id = R.drawable.img_hero_forza_1794934113027),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to GhostVoid.copy(alpha = 0.25f),
                        0.28f to GhostBackground.copy(alpha = 0.82f),
                        0.52f to GhostBackground,
                        1.0f to GhostVoid
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostBrandMark()
                    FreePassBadge()
                }

                Spacer(Modifier.height(110.dp))

                Text(
                    text = stringResource(R.string.auth_headline),
                    style = MaterialTheme.typography.displayLarge,
                    color = GhostTextPrimary
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.auth_subhead),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GhostTextSecondary
                )

                Spacer(Modifier.height(24.dp))

                // Feature highlight cards
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GhostSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, GhostBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AuthFeatureRow(
                            icon = Icons.Filled.Verified,
                            tint = XboxGreen,
                            title = "100% Free — No Payment Required",
                            detail = "Includes Forza Horizon 6, Watch Dogs, Blur, Warzone, GTA V & Need for Speed."
                        )
                        AuthFeatureRow(
                            icon = Icons.Filled.Groups,
                            tint = GhostCyan,
                            title = "Cross-Platform Multiplayer",
                            detail = "Create or join live Xbox, PC, PlayStation & Cloud squads in real time."
                        )
                        AuthFeatureRow(
                            icon = Icons.Filled.CloudSync,
                            tint = GhostBlue,
                            title = "Instant Cloud Save Sync",
                            detail = "Sync your Need for Speed garage & campaign progress across every device."
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GhostRed.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, GhostRed.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostTextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                GhostGradientButton(
                    text = if (isLoading) {
                        stringResource(R.string.auth_signing_in)
                    } else {
                        stringResource(R.string.auth_sign_in_google)
                    },
                    enabled = !isLoading,
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button"),
                    leadingIcon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = GhostTextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = GhostTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.auth_footer_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostTextMuted
                )
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    detail: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = GhostTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = GhostTextSecondary
            )
        }
    }
}

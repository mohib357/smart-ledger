package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val titleRes: Int,
    val bodyRes: Int?,
    val icon: ImageVector?,
    val emoji: String?,
    val chips: List<String>
)

private val onboardingPages = listOf(
    OnboardingPageData(
        titleRes = R.string.onboarding_welcome_title,
        bodyRes = R.string.onboarding_welcome_body,
        icon = null,
        emoji = "📊",
        chips = emptyList()
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_dashboard_title,
        bodyRes = R.string.onboarding_dashboard_body,
        icon = Icons.Default.Dashboard,
        emoji = null,
        chips = listOf("মোট পাবো", "মোট দেবো", "দ্রুত আয়/ব্যয়")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_transactions_title,
        bodyRes = R.string.onboarding_transactions_body,
        icon = Icons.Default.ReceiptLong,
        emoji = null,
        chips = listOf("ক্যাটাগরি", "ওয়ালেট", "ক্যালেন্ডার")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_khata_title,
        bodyRes = R.string.onboarding_khata_body,
        icon = Icons.Default.People,
        emoji = null,
        chips = listOf("কাস্টমার", "সাপ্লায়ার", "তাগাদা SMS")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_cashbox_title,
        bodyRes = R.string.onboarding_cashbox_body,
        icon = Icons.Default.AccountBalanceWallet,
        emoji = null,
        chips = listOf("ক্যাশ ইন/আউট", "খরচ ট্র্যাক", "দিনশেষ মিলান")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_reports_title,
        bodyRes = R.string.onboarding_reports_body,
        icon = Icons.Default.Assessment,
        emoji = null,
        chips = listOf("PDF রিপোর্ট", "Excel এক্সপোর্ট", "ইনভয়েস")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_security_title,
        bodyRes = R.string.onboarding_security_body,
        icon = Icons.Default.Security,
        emoji = null,
        chips = listOf("ফিঙ্গারপ্রিন্ট লক", "PIN", "ক্লাউড ব্যাকআপ")
    ),
    OnboardingPageData(
        titleRes = R.string.onboarding_getstarted_title,
        bodyRes = null,
        icon = Icons.Default.CheckCircle,
        emoji = null,
        chips = emptyList()
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 8 })
    val coroutineScope = rememberCoroutineScope()

    fun completeOnboarding() {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_v2_done", true)
            .apply()
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F5E9), Color(0xFFF1F8E9))
                )
            )
    ) {
        // Skip button — top right, hidden on page 7
        if (pagerState.currentPage < 7) {
            TextButton(
                onClick = { completeOnboarding() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 8.dp)
            ) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon or emoji
                if (page.emoji != null) {
                    Text(
                        text = page.emoji,
                        fontSize = 72.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (page.icon != null) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (page.bodyRes != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(page.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (page.chips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        page.chips.forEach { chip ->
                            AssistChip(
                                onClick = {},
                                label = { Text(chip) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Bottom area: dot indicators + Next / শুরু করুন button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot indicators — 8 dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(8) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isLastPage = pagerState.currentPage == 7

            if (isLastPage) {
                // Page 7 — full-width শুরু করুন button
                Button(
                    onClick = { completeOnboarding() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.onboarding_start))
                }
            } else {
                // Pages 0-6 — পরবর্তী button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}

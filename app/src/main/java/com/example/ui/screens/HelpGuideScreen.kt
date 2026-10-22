package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_guide_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section 1 — Getting Started
            item {
                HelpSection(
                    titleRes = R.string.help_getting_started,
                    icon = Icons.Default.PlayArrow,
                    mockBoxColor = Color(0xFFE8F5E9),
                    mockBoxIconTint = Color(0xFF2E7D32),
                    steps = listOf(
                        "📥 Google Play থেকে অ্যাপটি ইনস্টল করুন।",
                        "🚀 অ্যাপ খুললে স্বয়ংক্রিয়ভাবে অনবোর্ডিং শুরু হবে।",
                        "📖 ধাপে ধাপে অ্যাপের ফিচারগুলো দেখুন।",
                        "✅ 'শুরু করুন' বোতামে ট্যাপ করে মূল স্ক্রিনে প্রবেশ করুন।",
                        "💡 টিপ: প্রথমবার নমুনা ডেটা লোড করে ফিচারগুলো দেখে নিন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 2 — Dashboard
            item {
                HelpSection(
                    titleRes = R.string.help_dashboard,
                    icon = Icons.Default.Dashboard,
                    mockBoxColor = Color(0xFFE3F2FD),
                    mockBoxIconTint = Color(0xFF1565C0),
                    steps = listOf(
                        "📊 ব্যালেন্স কার্ডে মোট পাবো ও মোট দেবো দেখুন।",
                        "⚡ দ্রুত কার্যক্রম থেকে আয়, ব্যয়, খাতা সরাসরি যোগ করুন।",
                        "🤖 AI পরামর্শ কার্ড থেকে স্মার্ট পরামর্শ পান।",
                        "💰 ক্যাশবাক্স লিঙ্কে ট্যাপ করে দৈনিক নগদ দেখুন।",
                        "💡 টিপ: ড্যাশবোর্ড কাস্টমাইজ করতে মেনু থেকে 'ড্যাশবোর্ড কাস্টমাইজ' বেছে নিন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 3 — Transactions
            item {
                HelpSection(
                    titleRes = R.string.help_transactions,
                    icon = Icons.Default.ReceiptLong,
                    mockBoxColor = Color(0xFFFFF3E0),
                    mockBoxIconTint = Color(0xFFE65100),
                    steps = listOf(
                        "➕ '+আয়' বোতামে ট্যাপ করে নতুন আয় যোগ করুন।",
                        "➖ '-ব্যয়' বোতামে ট্যাপ করে নতুন ব্যয় যোগ করুন।",
                        "🗂️ ক্যাটাগরি বা তারিখ দিয়ে ফিল্টার করুন।",
                        "🔍 সার্চ বার থেকে নির্দিষ্ট লেনদেন খুঁজুন।",
                        "🗑️ লেনদেনে দীর্ঘক্ষণ চাপ দিয়ে মুছে ফেলুন।",
                        "💡 টিপ: ভয়েস ইনপুট দিয়ে দ্রুত লেনদেন যোগ করতে পারেন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 4 — Khata
            item {
                HelpSection(
                    titleRes = R.string.help_khata,
                    icon = Icons.Default.People,
                    mockBoxColor = Color(0xFFF3E5F5),
                    mockBoxIconTint = Color(0xFF6A1B9A),
                    steps = listOf(
                        "👤 '+' বোতামে ট্যাপ করে নতুন কাস্টমার বা সাপ্লায়ার যোগ করুন।",
                        "📋 খাতায় ট্যাপ করে সেই পার্টির সব লেনদেন দেখুন।",
                        "📨 'তাগাদা পাঠাও' বোতামে ট্যাপ করে রিমাইন্ডার SMS পাঠান।",
                        "🔍 নাম বা নোট দিয়ে খাতা খুঁজুন।",
                        "💡 টিপ: ট্যাব ফিল্টার দিয়ে কাস্টমার ও সাপ্লায়ার আলাদা দেখুন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 5 — Cashbox
            item {
                HelpSection(
                    titleRes = R.string.help_cashbox,
                    icon = Icons.Default.AccountBalanceWallet,
                    mockBoxColor = Color(0xFFE0F2F1),
                    mockBoxIconTint = Color(0xFF00695C),
                    steps = listOf(
                        "💵 'আজ পেলাম' বোতামে ট্যাপ করে নগদ ইন রেকর্ড করুন।",
                        "💸 'আজ দিলাম' বোতামে ট্যাপ করে নগদ আউট রেকর্ড করুন।",
                        "⚖️ 'ক্যাশবাক্স মিলাই' বোতামে দিনশেষে নগদ মিলিয়ে নিন।",
                        "📜 নিচে স্ক্রল করে আজকের সব ক্যাশ এন্ট্রি দেখুন।",
                        "💡 টিপ: প্রতিদিন দিনশেষে ক্যাশবাক্স মিলালে হিসাব নির্ভুল থাকে।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 6 — Reports
            item {
                HelpSection(
                    titleRes = R.string.help_reports,
                    icon = Icons.Default.Assessment,
                    mockBoxColor = Color(0xFFE8EAF6),
                    mockBoxIconTint = Color(0xFF283593),
                    steps = listOf(
                        "📄 তারিখ পরিসর বেছে PDF রিপোর্ট তৈরি করুন।",
                        "📊 Excel (.xlsx) ফরম্যাটে ডেটা এক্সপোর্ট করুন।",
                        "📈 মাসিক আয়-ব্যয় চার্ট দেখুন।",
                        "🧾 পণ্য লাভ-ক্ষতি বিশ্লেষণ দেখুন।",
                        "📤 রিপোর্ট সরাসরি শেয়ার বা ডাউনলোড করুন।",
                        "💡 টিপ: প্রিমিয়াম সদস্যরা সীমাহীন রিপোর্ট তৈরি করতে পারেন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 7 — Products
            item {
                HelpSection(
                    titleRes = R.string.help_products,
                    icon = Icons.Default.Inventory,
                    mockBoxColor = Color(0xFFFCE4EC),
                    mockBoxIconTint = Color(0xFFC62828),
                    steps = listOf(
                        "📦 'পণ্য যোগ করুন' বোতামে ট্যাপ করে নতুন পণ্য যোগ করুন।",
                        "💲 ক্রয়মূল্য ও বিক্রয়মূল্য সেট করুন।",
                        "📋 স্টক পরিমাণ আপডেট রাখুন।",
                        "⏰ মেয়াদ উত্তীর্ণের তারিখ দিলে অ্যাপ সতর্কতা দেবে।",
                        "💡 টিপ: পণ্যের মেয়াদ সতর্কতা বিজ্ঞপ্তি চালু রাখুন।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 8 — Security
            item {
                HelpSection(
                    titleRes = R.string.help_security,
                    icon = Icons.Default.Security,
                    mockBoxColor = Color(0xFFF9FBE7),
                    mockBoxIconTint = Color(0xFF558B2F),
                    steps = listOf(
                        "🔒 সেটিংস থেকে 'অ্যাপ লক' চালু করুন।",
                        "🖐️ ফিঙ্গারপ্রিন্ট বা PIN দিয়ে অ্যাপ লক করুন।",
                        "☁️ 'ক্লাউড ব্যাকআপ' চালু করে ডেটা সুরক্ষিত রাখুন।",
                        "🔑 লেনদেন PIN চালু করলে প্রতিটি লেনদেনে PIN দিতে হবে।",
                        "💡 টিপ: নিয়মিত ক্লাউড ব্যাকআপ নিন যাতে ডেটা হারিয়ে না যায়।"
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Section 9 — Menu & Settings
            item {
                HelpSection(
                    titleRes = R.string.help_menu_settings,
                    icon = Icons.Default.Settings,
                    mockBoxColor = Color(0xFFF5F5F5),
                    mockBoxIconTint = Color(0xFF424242),
                    steps = listOf(
                        "🌐 মেনু > 'ভাষা ও প্রদর্শন' থেকে ভাষা পরিবর্তন করুন।",
                        "🌙 সেটিংস থেকে হালকা বা গাঢ় থিম বেছে নিন।",
                        "🛡️ মেনু > 'উত্তরাধিকার' থেকে প্রতিনিধি যোগ করুন।",
                        "⭐ মেনু > 'প্রিমিয়াম' থেকে সদস্যতা নিন।",
                        "💡 টিপ: প্রিমিয়াম সদস্যরা বিজ্ঞাপনমুক্ত অভিজ্ঞতা পান।"
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HelpSection(
    titleRes: Int,
    icon: ImageVector,
    mockBoxColor: Color,
    mockBoxIconTint: Color,
    steps: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Clickable header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable body
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    // Mock illustration box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp)
                            .background(
                                color = mockBoxColor,
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = mockBoxIconTint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(titleRes),
                                style = MaterialTheme.typography.labelMedium,
                                color = mockBoxIconTint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step-by-step list items
                    steps.forEach { step ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.cloud.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    isLoading: Boolean,
    onUpdateProfile: (name: String, phone: String, photoUrl: String, fatherName: String, address: String, businessName: String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDismiss: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var editPhone by remember(userProfile.phone) { mutableStateOf(userProfile.phone) }
    var editPhotoUrl by remember(userProfile.photoUrl) { mutableStateOf(userProfile.photoUrl) }
    var editFatherName by remember(userProfile.fatherName) { mutableStateOf(userProfile.fatherName) }
    var editAddress by remember(userProfile.address) { mutableStateOf(userProfile.address) }
    var editBusinessName by remember(userProfile.businessName) { mutableStateOf(userProfile.businessName) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Photo picker launcher — stores local URI as string, no Firebase Storage upload
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editPhotoUrl = uri.toString()
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_button)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(stringResource(R.string.profile_logout_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Delete account confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.profile_delete_account)) },
            text = { Text(stringResource(R.string.profile_delete_account_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.profile_delete_account))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Avatar — photo if available, else initials
                val initials = userProfile.name
                    .trim()
                    .split("\\s+".toRegex())
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
                    .ifEmpty { "?" }

                val displayPhotoUrl = if (isEditMode) editPhotoUrl else userProfile.photoUrl

                Box(contentAlignment = Alignment.BottomEnd) {
                    if (displayPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = Uri.parse(displayPhotoUrl),
                            contentDescription = stringResource(R.string.profile_title),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isEditMode) {
                        IconButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "ছবি পরিবর্তন করুন",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Photo note in edit mode
                if (isEditMode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.profile_photo_todo_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name field
                if (isEditMode) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.profile_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_name_label),
                        value = userProfile.name.ifEmpty { "—" }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Email field — always read-only
                ProfileInfoRow(
                    label = stringResource(R.string.profile_email_label),
                    value = userProfile.email.ifEmpty { "—" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone field
                if (isEditMode) {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(stringResource(R.string.profile_phone_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_phone_label),
                        value = userProfile.phone.ifEmpty { "—" }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Father's name field
                if (isEditMode) {
                    OutlinedTextField(
                        value = editFatherName,
                        onValueChange = { editFatherName = it },
                        label = { Text(stringResource(R.string.profile_father_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (userProfile.fatherName.isNotEmpty()) {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_father_name_label),
                        value = userProfile.fatherName
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isEditMode) Spacer(modifier = Modifier.height(12.dp))

                // Address field
                if (isEditMode) {
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text(stringResource(R.string.profile_address_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (userProfile.address.isNotEmpty()) {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_address_label),
                        value = userProfile.address
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isEditMode) Spacer(modifier = Modifier.height(12.dp))

                // Business name field
                if (isEditMode) {
                    OutlinedTextField(
                        value = editBusinessName,
                        onValueChange = { editBusinessName = it },
                        label = { Text(stringResource(R.string.profile_business_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (userProfile.businessName.isNotEmpty()) {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_business_name_label),
                        value = userProfile.businessName
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Edit / Save button
                if (isEditMode) {
                    Button(
                        onClick = {
                            onUpdateProfile(
                                editName.trim(),
                                editPhone.trim(),
                                editPhotoUrl.trim(),
                                editFatherName.trim(),
                                editAddress.trim(),
                                editBusinessName.trim()
                            )
                            isEditMode = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.profile_save_button))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            editName = userProfile.name
                            editPhone = userProfile.phone
                            editPhotoUrl = userProfile.photoUrl
                            editFatherName = userProfile.fatherName
                            editAddress = userProfile.address
                            editBusinessName = userProfile.businessName
                            isEditMode = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                } else {
                    OutlinedButton(
                        onClick = { isEditMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_edit_button))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(stringResource(R.string.profile_logout_button))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delete account button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.profile_delete_account))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

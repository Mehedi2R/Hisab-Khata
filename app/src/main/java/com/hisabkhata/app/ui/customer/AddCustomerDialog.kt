package com.hisabkhata.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.hisabkhata.app.data.local.AppLanguageManager
import com.hisabkhata.app.data.local.AppLanguageManager.str
import com.hisabkhata.app.data.local.entity.CustomerEntity
import com.hisabkhata.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDialog(
    initialType: String = CustomerEntity.TYPE_CUSTOMER,
    initialName: String = "",
    initialPhone: String = "",
    initialAddress: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, address: String, type: String) -> Unit
) {
    val currentLang by AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }
    var selectedType by remember { mutableStateOf(initialType) }
    var nameError by remember { mutableStateOf(false) }

    val isReceivable = selectedType == CustomerEntity.TYPE_CUSTOMER

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.setWindowAnimations(com.hisabkhata.app.R.style.DialogSlideAnimation)
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SurfaceCanvas
        ) {
            Scaffold(
                containerColor = SurfaceCanvas,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isEditing) str("প্রোফাইল সম্পাদনা", "Edit Profile") else str("+ নতুন প্রোফাইল যোগ", "+ Add Profile"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextHead
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = str("বন্ধ করুন", "Close"),
                                    tint = TextHead,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard),
                        windowInsets = WindowInsets.statusBars
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Modern Segmented Selector: [ দেনা / ঋণ | পাওনা ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfacePill)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // দেনা / ঋণ Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isReceivable) PrimaryRed else Color.Transparent)
                                .clickable { selectedType = CustomerEntity.TYPE_CUSTOMER }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = str("দেনা / ঋণ", "Debt / Payable"),
                                fontSize = 12.sp,
                                fontWeight = if (isReceivable) FontWeight.Bold else FontWeight.Medium,
                                color = if (isReceivable) Color.White else TextMuted
                            )
                        }

                        // পাওনা Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isReceivable) PrimaryGreen else Color.Transparent)
                                .clickable { selectedType = CustomerEntity.TYPE_SUPPLIER }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = str("পাওনা", "Due / Receivable"),
                                fontSize = 12.sp,
                                fontWeight = if (!isReceivable) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isReceivable) Color.White else TextMuted
                            )
                        }
                    }

                    // Card with Inputs - FinTech Snug Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderHairline))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Person / Business Name
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    if (it.isNotBlank()) nameError = false
                                },
                                label = { Text(str("ব্যক্তি / প্রতিষ্ঠানের নাম *", "Person / Business Name *"), fontSize = 11.sp) },
                                placeholder = { Text(str("যেমন: মোঃ রফিকুল ইসলাম", "e.g. John Doe"), fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                },
                                isError = nameError,
                                supportingText = if (nameError) {
                                    { Text(str("নাম অবশ্যই পূরণ করতে হবে", "Name is required"), color = PrimaryRed, fontSize = 9.sp) }
                                } else null,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Phone Number
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(str("মোবাইল নম্বর (ঐচ্ছিক)", "Phone Number (Optional)"), fontSize = 11.sp) },
                                placeholder = { Text("017xxxxxxxx", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Address
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text(str("ঠিকানা / এলাকা (ঐচ্ছিক)", "Address / Area (Optional)"), fontSize = 11.sp) },
                                placeholder = { Text(str("যেমন: ঢাকা, বাংলাদেশ", "e.g. Dhaka, Bangladesh"), fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 🌟 Save Profile Button placed directly underneath input options
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                onSave(name.trim(), phone.trim(), address.trim(), selectedType)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReceivable) PrimaryRed else PrimaryGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isEditing) str("✓ আপডেট সংরক্ষণ করুন", "✓ Update Profile") else str("✓ প্রোফাইল সংরক্ষণ করুন", "✓ Save Profile"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

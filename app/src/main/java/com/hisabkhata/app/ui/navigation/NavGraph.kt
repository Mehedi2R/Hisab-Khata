package com.hisabkhata.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hisabkhata.app.ui.backup.BackupScreen
import com.hisabkhata.app.ui.cashbook.IncomeExpenseScreen
import com.hisabkhata.app.ui.customer.CustomerDetailScreen
import com.hisabkhata.app.ui.family.FamilyExpenseScreen
import com.hisabkhata.app.ui.home.HomeScreen
import com.hisabkhata.app.ui.settings.SettingsScreen
import com.hisabkhata.app.ui.theme.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FamilyExpense : Screen("family_expense")
    object IncomeExpense : Screen("income_expense")
    object Settings : Screen("settings")

    object CustomerDetail : Screen("customer_detail/{customerId}") {
        fun createRoute(customerId: String) = "customer_detail/$customerId"
    }
    object Backup : Screen("backup")
}

data class BottomNavItem(
    val screen: Screen,
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector,
    val customIconRes: Int? = null
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "হোম", "Home", Icons.Default.Home),
    BottomNavItem(Screen.FamilyExpense, "ফ্যামিলি খরচ", "Family Expense", Icons.Default.HomeWork),
    BottomNavItem(Screen.IncomeExpense, "আয়-ব্যয় হিসাব", "Cash Book", Icons.Default.AccountBalanceWallet),
    BottomNavItem(Screen.Settings, "সেটিংস", "Settings", Icons.Default.Settings, com.hisabkhata.app.R.drawable.ic_custom_settings)
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val currentLang by com.hisabkhata.app.data.local.AppLanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be shown
    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeckBackground,
                    shadowElevation = 8.dp,
                    border = BorderStroke(0.5.dp, BorderHairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Inner content is padded above nav bar, while Surface background extends to edge
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val isSelected = currentRoute == item.screen.route

                                val pillBgColor by animateColorAsState(
                                    targetValue = if (isSelected) PrimaryRed.copy(alpha = 0.12f) else Color.Transparent,
                                    animationSpec = tween(200),
                                    label = "nav_pill_bg"
                                )

                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected) PrimaryRed else TextMuted,
                                    animationSpec = tween(200),
                                    label = "nav_item_color"
                                )

                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.08f else 1.0f,
                                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                                    label = "nav_icon_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = PrimaryRed.copy(alpha = 0.18f))
                                        ) {
                                            if (currentRoute != item.screen.route) {
                                                navController.navigate(item.screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val itemTitle = if (currentLang == com.hisabkhata.app.data.local.AppLanguageManager.LANG_EN) item.titleEn else item.titleBn
                                        
                                        // Sleek indicator pill for icon
                                        Box(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .height(26.dp)
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(pillBgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (item.customIconRes != null) {
                                                Icon(
                                                    painter = painterResource(id = item.customIconRes),
                                                    contentDescription = itemTitle,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .graphicsLayer {
                                                            scaleX = iconScale
                                                            scaleY = iconScale
                                                            alpha = if (isSelected) 1.0f else 0.7f
                                                        }
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = itemTitle,
                                                    tint = contentColor,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .graphicsLayer {
                                                            scaleX = iconScale
                                                            scaleY = iconScale
                                                        }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = itemTitle,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = contentColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 180, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
            },
            exitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 180, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 180, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 180, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
            }
        ) {
            // 1. Home
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCustomer = { customerId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                    },
                    onNavigateToBackup = {
                        navController.navigate(Screen.Backup.route)
                    }
                )
            }

            // 2. Family Expense (ফ্যামিলি খরচ)
            composable(Screen.FamilyExpense.route) {
                FamilyExpenseScreen()
            }

            // 3. Cash Book (আয়-ব্যয় হিসাব)
            composable(Screen.IncomeExpense.route) {
                IncomeExpenseScreen()
            }

            // 4. Settings (Settings)
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // Sub Screens
            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(
                    navArgument("customerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                CustomerDetailScreen(
                    customerId = customerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Backup.route) {
                BackupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

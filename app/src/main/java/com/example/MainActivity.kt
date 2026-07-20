package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SecondaryBackground
import com.example.ui.viewmodel.BusinessViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // States for splash and security flow
            var splashScreenActive by remember { mutableStateOf(true) }
            var pinVerified by remember { mutableStateOf(false) }

            val isDarkTheme by remember { derivedStateOf { viewModel.isDarkTheme.value } }
            val currencySymbol by remember { derivedStateOf { viewModel.currencySymbol.value } }
            val pinRequired by remember { derivedStateOf { viewModel.securityPin.value } }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (splashScreenActive) {
                    SplashScreen(
                        onSplashComplete = { splashScreenActive = false }
                    )
                } else if (pinRequired.isNotEmpty() && !pinVerified) {
                    SecurityPinScreen(
                        correctPin = pinRequired,
                        onAccessGranted = { pinVerified = true }
                    )
                } else {
                    // Core App Navigation Scaffold Container
                    MainAppScaffold(
                        viewModel = viewModel,
                        currencySymbol = currencySymbol,
                        onResetLock = { pinVerified = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: BusinessViewModel,
    currencySymbol: String,
    onResetLock: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val products by viewModel.products.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardState.collectAsStateWithLifecycle()
    val reportsData by viewModel.reportsState.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsList.collectAsStateWithLifecycle()

    val tabs = listOf("Dashboard", "Products", "Orders", "Reports", "Profile")
    val icons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.Inventory,
        Icons.Default.Receipt,
        Icons.Default.BarChart,
        Icons.Default.Person
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                        }
                        Column {
                            Text(
                                text = "DROP SOCIETY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = tabs[selectedTab].uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SecondaryBackground
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp)
                NavigationBar(
                    containerColor = SecondaryBackground,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav")
                ) {
                    tabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { 
                                Text(
                                    text = title.uppercase(), 
                                    color = if (selectedTab == index) PrimaryRed else Color.White.copy(alpha = 0.4f), 
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = title,
                                    tint = if (selectedTab == index) PrimaryRed else Color.White.copy(alpha = 0.4f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    dashboardData = dashboardData,
                    currencySymbol = currencySymbol,
                    onNavigateToTab = { selectedTab = it }
                )
                1 -> ProductsScreen(
                    products = products,
                    currencySymbol = currencySymbol,
                    onSaveProduct = { viewModel.saveProduct(it) { _, _ -> } },
                    onDeleteProduct = { viewModel.deleteProduct(it) {} }
                )
                2 -> OrdersScreen(
                    orders = orders,
                    products = products,
                    currencySymbol = currencySymbol,
                    onSaveOrder = { viewModel.saveOrder(it) { _, _ -> } },
                    onUpdateStatus = { id, stat -> viewModel.updateOrderStatus(id, stat) { _, _ -> } },
                    onDeleteOrder = { viewModel.deleteOrder(it) {} }
                )
                3 -> ReportsScreen(
                    reportsData = reportsData,
                    expenses = expenses,
                    currencySymbol = currencySymbol,
                    onAddExpense = { viewModel.saveExpense(it) {} },
                    onDeleteExpense = { viewModel.deleteExpense(it) {} }
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    notifications = notifications,
                    currencySymbol = currencySymbol,
                    onResetData = {
                        onResetLock()
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

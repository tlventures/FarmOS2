package com.agriedge.presentation.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MarketDetailScreen(
    listingId: String,
    navController: NavController,
    viewModel: MarketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var quantity by remember { mutableStateOf(1) }
    var showContactDialog by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        viewModel.loadListing(listingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.listing != null -> {
                val listing = uiState.listing!!
                
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                ) {
                    if (listing.imageUrls.isNotEmpty()) {
                        val pagerState = rememberPagerState()
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            HorizontalPager(
                                count = listing.imageUrls.size,
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = listing.imageUrls[page],
                                    contentDescription = "Product image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                                activeColor = MaterialTheme.colorScheme.primary,
                                inactiveColor = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = listing.productName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = listing.category, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = listing.description)
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Price")
                                    Text(
                                        text = "${listing.currency} ${listing.price}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Available")
                                    Text(
                                        text = "${listing.quantity} ${listing.unit}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (listing.videoUrl != null) {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Product Video", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(180.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayCircle, "Play", modifier = Modifier.size(64.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (listing.locationCoordinates != null) {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Farm Location", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(listing.locationCoordinates.address)
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Map, "Map", modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Seller Information", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(listing.sellerName, fontWeight = FontWeight.Medium)
                            Text(listing.location)
                            if (listing.sellerRating != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${listing.sellerRating}/5.0 (${listing.sellerReviewCount} reviews)")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quantity")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                    Icon(Icons.Default.Remove, "Decrease")
                                }
                                Text(quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp))
                                IconButton(onClick = { quantity++ }) {
                                    Icon(Icons.Default.Add, "Increase")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showContactDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Contact")
                        }
                        Button(
                            onClick = { viewModel.initiateTransaction(listing, quantity) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Buy Now")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Seller") },
            text = { Text("Phone: ${uiState.listing?.sellerContact ?: "N/A"}") },
            confirmButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (uiState.transactionInitiated) {
        AlertDialog(
            onDismissRequest = { viewModel.resetTransactionState() },
            title = { Text("Order Placed") },
            text = { Text("Your purchase request has been sent to the seller.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetTransactionState()
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

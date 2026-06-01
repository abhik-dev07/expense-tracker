package com.abhik.paisatrack.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Icon mapper helper
fun getIconByName(iconName: String): ImageVector {
    return when (iconName.lowercase().trim()) {
        "restaurant", "coffee", "utensils", "food", "fast-food", "food and drinks", "food-and-drinks", "hamburger" -> Icons.Default.Restaurant
        "directions_car", "car", "bus", "transport", "automobile", "carfront", "car-front" -> Icons.Default.DirectionsCar
        "movie", "film", "clapperboard", "play", "tv", "entertainment" -> Icons.Default.Movie
        "account_balance_wallet", "wallet", "dollar-sign", "trending-up", "savings", "cash", "bills", "receiptindianrupee", "receipt-indian-rupee" -> Icons.Default.AccountBalanceWallet
        "local_hospital", "heart", "activity", "stethoscope", "health", "medical", "health care", "health-care", "hospital" -> Icons.Default.LocalHospital
        "flight", "plane", "travel", "airplane" -> Icons.Default.Flight
        "school", "book", "book-open", "graduation-cap", "graduationcap", "education", "study" -> Icons.Default.School
        "shopping_cart", "shopping-cart", "shopping-bag", "shopping", "gift", "gifts", "groceries", "shoppingbasket", "shopping-basket" -> Icons.Default.ShoppingCart
        "home", "home-bills", "house", "rent" -> Icons.Default.Home
        "fitness_center", "dumbbell", "sports", "gym", "workout" -> Icons.Default.FitnessCenter
        "work", "briefcase", "job", "business" -> Icons.Default.Work
        "category", "general", "pet", "pawprint", "paw-print", "others", "ellipsis" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Available custom colors for Collections
val CollectionColors = listOf(
    "#3F51B5" to "Indigo",
    "#009688" to "Teal",
    "#4CAF50" to "Green",
    "#FF9800" to "Orange",
    "#E91E63" to "Pink",
    "#9C27B0" to "Purple",
    "#FFEB3B" to "Yellow",
    "#00BCD4" to "Cyan",
    "#F44336" to "Red"
)

// Available custom icons for Collections
val CollectionIcons = listOf(
    "category" to "General",
    "restaurant" to "Food & Dining",
    "directions_car" to "Transport",
    "movie" to "Entertainment",
    "account_balance_wallet" to "Wallet",
    "local_hospital" to "Health",
    "flight" to "Travel",
    "school" to "Education",
    "shopping_cart" to "Shopping",
    "home" to "Home Bills",
    "fitness_center" to "Sports & Fitness",
    "work" to "Work/Office"
)

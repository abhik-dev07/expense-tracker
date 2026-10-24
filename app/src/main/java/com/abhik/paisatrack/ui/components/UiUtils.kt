package com.abhik.paisatrack.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.abhik.paisatrack.R
import androidx.compose.material.icons.filled.*


// Icon mapper helper
@Composable
fun getIconByName(iconName: String): ImageVector {
    return when (iconName.lowercase().trim()) {
        "restaurant", "coffee", "utensils", "food", "fast-food", "food and drinks", "food-and-drinks", "hamburger" -> ImageVector.vectorResource(id = R.drawable.ic_category_restaurant)
        "directions_car", "car", "bus", "transport", "automobile", "carfront", "car-front" -> ImageVector.vectorResource(id = R.drawable.ic_category_bus)
        "movie", "film", "clapperboard", "play", "tv", "entertainment" -> ImageVector.vectorResource(id = R.drawable.ic_category_movie)
        "account_balance_wallet", "wallet", "dollar-sign", "trending-up", "savings", "cash", "bills", "receiptindianrupee", "receipt-indian-rupee" -> ImageVector.vectorResource(id = R.drawable.ic_category_wallet)
        "local_hospital", "heart", "activity", "stethoscope", "health", "medical", "health care", "health-care", "hospital" -> ImageVector.vectorResource(id = R.drawable.ic_category_hospital)
        "flight", "plane", "travel", "airplane" -> ImageVector.vectorResource(id = R.drawable.ic_category_flight)
        "school", "book", "book-open", "graduation-cap", "graduationcap", "education", "study" -> ImageVector.vectorResource(id = R.drawable.ic_category_school)
        "shopping_cart", "shopping-cart", "shopping-bag", "shopping", "gift", "gifts", "groceries", "shoppingbasket", "shopping-basket" -> ImageVector.vectorResource(id = R.drawable.ic_category_shopping)
        "home", "home-bills", "house", "rent", "bill" -> ImageVector.vectorResource(id = R.drawable.ic_category_bill)
        "fitness_center", "dumbbell", "sports", "gym", "workout" -> ImageVector.vectorResource(id = R.drawable.ic_category_gym)
        "work", "briefcase", "job", "business" -> ImageVector.vectorResource(id = R.drawable.ic_category_work)
        "person", "personal", "user", "profile" -> ImageVector.vectorResource(id = R.drawable.ic_category_person)
        "category", "general", "pet", "pawprint", "paw-print", "others", "ellipsis" -> ImageVector.vectorResource(id = R.drawable.ic_category_general)
        else -> ImageVector.vectorResource(id = R.drawable.ic_category_general)
    }
}

// Available custom colors for Collections
val CollectionColors = listOf(
"#5C6BC0" to "Indigo",
"#4DB6AC" to "Teal",
"#66BB6A" to "Green",
"#FFB74D" to "Orange",
"#F06292" to "Pink",
"#AB7BC6" to "Purple",
"#D9C84A" to "Yellow",
"#4DD0E1" to "Cyan",
"#E57373" to "Red"
)

// Available custom icons for Collections
val CollectionIcons = listOf(
    "category" to "General",
    "restaurant" to "Food & Dining",
    "bus" to "Transport",
    "movie" to "Entertainment",
    "wallet" to "Wallet",
    "hospital" to "Health",
    "flight" to "Travel",
    "school" to "Education",
    "shopping" to "Shopping",
    "bill" to "Home Bills",
    "gym" to "Sports & Fitness",
    "work" to "Work/Office",
    "person" to "Personal"
)


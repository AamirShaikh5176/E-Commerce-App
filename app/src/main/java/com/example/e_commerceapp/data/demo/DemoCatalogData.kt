package com.example.e_commerceapp.data.demo

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

/**
 * High-quality, curated catalog of realistic Indian e-commerce marketplace products & categories.
 * Used for idempotent Firestore seeding and offline/fallback demo data.
 */
object DemoCatalogData {

    val demoCategories: List<Category> = listOf(
        Category(name = "Mobiles", slug = "smartphones", url = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=300"),
        Category(name = "Electronics", slug = "electronics", url = "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=300"),
        Category(name = "Fashion", slug = "fashion", url = "https://images.unsplash.com/photo-1445205170230-053b83016050?w=300"),
        Category(name = "Beauty", slug = "beauty", url = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=300"),
        Category(name = "Home", slug = "home", url = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=300"),
        Category(name = "Groceries", slug = "groceries", url = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=300"),
        Category(name = "Sports", slug = "sports", url = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=300"),
        Category(name = "Accessories", slug = "accessories", url = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=300")
    )

    val demoCategoryDtos: List<FirestoreCategoryDto> = demoCategories.map {
        FirestoreCategoryDto(
            id = it.slug,
            name = it.name,
            slug = it.slug,
            url = it.url
        )
    }

    val demoProducts: List<FirestoreProductDto> = listOf(
        // --- MOBILES ---
        FirestoreProductDto(
            id = "demo_mob_1",
            title = "Samsung Galaxy S24 Ultra 5G (Titanium Gray, 256GB)",
            description = "AI Phone with 200MP camera, Snapdragon 8 Gen 3 for Galaxy, S-Pen included, and 5000mAh battery with 45W fast charging.",
            price = 129999.0,
            mrpPrice = 144999.0,
            discountPercentage = 10.0,
            categoryId = "smartphones",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600"
            ),
            stockQuantity = 25,
            inStock = true,
            rating = 4.7,
            reviewsCount = 1240,
            brand = "Samsung",
            tags = listOf("smartphones", "mobiles", "5g", "samsung", "flagship"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_mob_2",
            title = "Apple iPhone 15 (Blue, 128GB)",
            description = "Dynamic Island, 48MP main camera with 2x Telephoto, USB-C, and all-day battery life powered by A16 Bionic chip.",
            price = 69999.0,
            mrpPrice = 79900.0,
            discountPercentage = 12.0,
            categoryId = "smartphones",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600"
            ),
            stockQuantity = 40,
            inStock = true,
            rating = 4.8,
            reviewsCount = 3120,
            brand = "Apple",
            tags = listOf("smartphones", "mobiles", "apple", "iphone", "ios"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_mob_3",
            title = "OnePlus Nord CE4 5G (Dark Chrome, 128GB)",
            description = "Snapdragon 7 Gen 3, 100W SUPERVOOC charging, 5500mAh durable battery, and 50MP Sony LYT-600 with OIS.",
            price = 24999.0,
            mrpPrice = 27999.0,
            discountPercentage = 11.0,
            categoryId = "smartphones",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600"
            ),
            stockQuantity = 35,
            inStock = true,
            rating = 4.4,
            reviewsCount = 890,
            brand = "OnePlus",
            tags = listOf("smartphones", "mobiles", "oneplus", "5g"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- ELECTRONICS ---
        FirestoreProductDto(
            id = "demo_elec_1",
            title = "Sony WH-1000XM5 Wireless ANC Headphones",
            description = "Industry-leading noise canceling with two processors and 8 microphones, Ultra-comfortable lightweight design, up to 30-hour battery life.",
            price = 26990.0,
            mrpPrice = 34990.0,
            discountPercentage = 23.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600"
            ),
            stockQuantity = 20,
            inStock = true,
            rating = 4.6,
            reviewsCount = 760,
            brand = "Sony",
            tags = listOf("electronics", "headphones", "sony", "anc", "audio"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_elec_2",
            title = "Apple MacBook Air M2 (13.6-inch, 8GB RAM, 256GB SSD)",
            description = "Strikingly thin design, 13.6-inch Liquid Retina display, 1080p FaceTime HD camera, MagSafe 3 charging port.",
            price = 84990.0,
            mrpPrice = 99900.0,
            discountPercentage = 15.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"
            ),
            stockQuantity = 15,
            inStock = true,
            rating = 4.8,
            reviewsCount = 940,
            brand = "Apple",
            tags = listOf("electronics", "laptops", "apple", "macbook"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_elec_3",
            title = "boAt Airdopes 141 Bluetooth TWS Earbuds",
            description = "42H playtime, Beast Mode for gaming, ENx Tech, ASAP charge (5 mins = 75 mins), IPX4 water resistance.",
            price = 1299.0,
            mrpPrice = 4490.0,
            discountPercentage = 71.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600"
            ),
            stockQuantity = 60,
            inStock = true,
            rating = 4.1,
            reviewsCount = 15400,
            brand = "boAt",
            tags = listOf("electronics", "earbuds", "boat", "audio"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- FASHION ---
        FirestoreProductDto(
            id = "demo_fash_1",
            title = "Nike Air Max SC Men's Running & Walking Shoes",
            description = "Heritage track look with visible Air cushioning. Genuine leather and airy mesh for all-day comfort and breathability.",
            price = 4495.0,
            mrpPrice = 5995.0,
            discountPercentage = 25.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"
            ),
            stockQuantity = 30,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1820,
            brand = "Nike",
            tags = listOf("fashion", "shoes", "nike", "footwear"),
            deliveryDays = 3,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_fash_2",
            title = "Levi's Men's 511 Slim Fit Stretchable Jeans",
            description = "Classic slim fit cut close through the thigh and leg with room to move. Premium durable denim blend.",
            price = 2199.0,
            mrpPrice = 3599.0,
            discountPercentage = 39.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600"
            ),
            stockQuantity = 45,
            inStock = true,
            rating = 4.3,
            reviewsCount = 670,
            brand = "Levi's",
            tags = listOf("fashion", "jeans", "levis", "clothing"),
            deliveryDays = 3,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_fash_3",
            title = "Puma Classic Solid Cotton Polo T-Shirt",
            description = "Regular fit breathable cotton pique polo with ribbed collar and Puma cat logo embroidery.",
            price = 899.0,
            mrpPrice = 1799.0,
            discountPercentage = 50.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600"
            ),
            stockQuantity = 50,
            inStock = true,
            rating = 4.2,
            reviewsCount = 430,
            brand = "Puma",
            tags = listOf("fashion", "tshirt", "puma", "polo"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- BEAUTY ---
        FirestoreProductDto(
            id = "demo_beau_1",
            title = "Minimalist 10% Niacinamide Face Serum (30ml)",
            description = "Nourishing face serum with Zinc and Aloe Vera to reduce blemishes, control excess oil, and strengthen skin barrier.",
            price = 569.0,
            mrpPrice = 599.0,
            discountPercentage = 5.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600"
            ),
            stockQuantity = 75,
            inStock = true,
            rating = 4.4,
            reviewsCount = 3890,
            brand = "Minimalist",
            tags = listOf("beauty", "skincare", "serum"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_2",
            title = "Philips BHD399 Hair Dryer with ThermoProtect",
            description = "1800W drying power with ionic conditioning for shiny frizz-free hair, 3 heat and speed settings.",
            price = 1499.0,
            mrpPrice = 2295.0,
            discountPercentage = 35.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?w=600"
            ),
            stockQuantity = 22,
            inStock = true,
            rating = 4.3,
            reviewsCount = 510,
            brand = "Philips",
            tags = listOf("beauty", "grooming", "philips"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- HOME ---
        FirestoreProductDto(
            id = "demo_home_1",
            title = "Wakefit Orthopedic Memory Foam Mattress (King Size)",
            description = "High density pressure-relieving memory foam with breathable fabric cover and 10-year manufacturer warranty.",
            price = 13499.0,
            mrpPrice = 18999.0,
            discountPercentage = 29.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600"
            ),
            stockQuantity = 12,
            inStock = true,
            rating = 4.6,
            reviewsCount = 1450,
            brand = "Wakefit",
            tags = listOf("home", "furniture", "mattress"),
            deliveryDays = 4,
            isCodAvailable = false
        ),
        FirestoreProductDto(
            id = "demo_home_2",
            title = "Solimo 100% Cotton 144 TC Double Bedsheet with 2 Pillow Covers",
            description = "Soft combed cotton fabric with vibrant floral prints. Fade resistant and machine washable.",
            price = 799.0,
            mrpPrice = 1499.0,
            discountPercentage = 47.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=600"
            ),
            stockQuantity = 50,
            inStock = true,
            rating = 4.2,
            reviewsCount = 920,
            brand = "Amazon Brand - Solimo",
            tags = listOf("home", "bedsheet", "cotton"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- GROCERIES ---
        FirestoreProductDto(
            id = "demo_groc_1",
            title = "Tata Tea Gold Royal Assam Black Tea (1kg)",
            description = "Exquisite blend of gently rolled aromatic long leaves and fine Assam CTC tea for a rich and aromatic cup.",
            price = 499.0,
            mrpPrice = 640.0,
            discountPercentage = 22.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600"
            ),
            stockQuantity = 80,
            inStock = true,
            rating = 4.5,
            reviewsCount = 4200,
            brand = "Tata Tea",
            tags = listOf("groceries", "tea", "beverage"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_2",
            title = "Fortune Sunlite Refined Sunflower Oil (5 Litre Jar)",
            description = "Enriched with Vitamin A and Vitamin D, light and healthy cooking oil suitable for all Indian delicacies.",
            price = 689.0,
            mrpPrice = 850.0,
            discountPercentage = 19.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600"
            ),
            stockQuantity = 40,
            inStock = true,
            rating = 4.4,
            reviewsCount = 2100,
            brand = "Fortune",
            tags = listOf("groceries", "oil", "cooking"),
            deliveryDays = 1,
            isCodAvailable = true
        ),

        // --- SPORTS ---
        FirestoreProductDto(
            id = "demo_sport_1",
            title = "Yonex Nanoray Light 18i Graphite Badminton Racket",
            description = "Lightweight 77g frame with Isometric head shape, high-modulus graphite shaft and full cover included.",
            price = 1999.0,
            mrpPrice = 3290.0,
            discountPercentage = 39.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600"
            ),
            stockQuantity = 30,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1120,
            brand = "Yonex",
            tags = listOf("sports", "badminton", "yonex"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_sport_2",
            title = "Nivia Storm Football (Size 5)",
            description = "Durable rubberized molded construction with 32 panels, suitable for hard ground and turf matches.",
            price = 449.0,
            mrpPrice = 699.0,
            discountPercentage = 36.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=600"
            ),
            stockQuantity = 35,
            inStock = true,
            rating = 4.2,
            reviewsCount = 740,
            brand = "Nivia",
            tags = listOf("sports", "football", "nivia"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // --- ACCESSORIES ---
        FirestoreProductDto(
            id = "demo_acc_1",
            title = "Titan Neo Splash Analog Watch with Blue Dial",
            description = "Quartz precision movement, genuine leather brown strap, water resistant up to 50 meters with 2-year warranty.",
            price = 2995.0,
            mrpPrice = 3995.0,
            discountPercentage = 25.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600"
            ),
            stockQuantity = 28,
            inStock = true,
            rating = 4.6,
            reviewsCount = 890,
            brand = "Titan",
            tags = listOf("accessories", "watch", "titan"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_acc_2",
            title = "boAt Wave Call Smart Watch with Bluetooth Calling",
            description = "1.69-inch HD display with 550 nits brightness, 150+ watch faces, heart rate & SpO2 tracking, multiple sports modes.",
            price = 1499.0,
            mrpPrice = 7990.0,
            discountPercentage = 81.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600",
            images = listOf(
                "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600"
            ),
            stockQuantity = 45,
            inStock = true,
            rating = 4.2,
            reviewsCount = 6840,
            brand = "boAt",
            tags = listOf("accessories", "smartwatch", "boat"),
            deliveryDays = 2,
            isCodAvailable = true
        )
    )

    fun toProductEntityList(): List<ProductEntity> = demoProducts.map { it.toProductEntity() }

    fun toSearchProductEntityList(): List<SearchProductEntity> = demoProducts.map { it.toSearchProductEntity() }

    fun findProductEntityById(id: String): ProductEntity? =
        demoProducts.find { it.id == id }?.toProductEntity()

    fun findProductDetailsById(id: String): ProductDetailsEntity? =
        demoProducts.find { it.id == id }?.toProductDetailsEntity()

    fun filterByCategory(categoryName: String): List<ProductEntity> {
        val trimmed = categoryName.trim().lowercase()
        return toProductEntityList().filter {
            it.category.lowercase().contains(trimmed) ||
            trimmed.contains(it.category.lowercase())
        }
    }
}

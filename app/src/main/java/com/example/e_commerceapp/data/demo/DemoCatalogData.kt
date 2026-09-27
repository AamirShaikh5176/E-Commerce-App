package com.example.e_commerceapp.data.demo

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

/**
 * Authoritative, high-quality catalog of 48 realistic Indian e-commerce marketplace products & categories.
 * Strict canonical taxonomy: Mobiles, Electronics, Fashion, Beauty, Home, Groceries, Sports, Accessories.
 * Used for idempotent Firestore seeding and offline/fallback demo data.
 */
object DemoCatalogData {

    val demoCategories: List<Category> = listOf(
        Category(name = "Mobiles", slug = "mobiles", url = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=300"),
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
        // ==========================================
        // 1. MOBILES (6 Products)
        // ==========================================
        FirestoreProductDto(
            id = "demo_mob_1",
            title = "Samsung Galaxy S24 Ultra 5G (Titanium Gray, 256GB)",
            description = "AI Phone with 200MP camera, Snapdragon 8 Gen 3 for Galaxy, S-Pen included, and 5000mAh battery with 45W fast charging.",
            price = 129999.0,
            mrpPrice = 144999.0,
            discountPercentage = 10.0,
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600",
            images = listOf("https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=600"),
            stockQuantity = 25,
            inStock = true,
            rating = 4.7,
            reviewsCount = 1240,
            brand = "Samsung",
            tags = listOf("mobiles", "smartphones", "5g", "samsung", "flagship", "galaxy"),
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
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600",
            images = listOf("https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600"),
            stockQuantity = 40,
            inStock = true,
            rating = 4.8,
            reviewsCount = 3120,
            brand = "Apple",
            tags = listOf("mobiles", "smartphones", "apple", "iphone", "ios", "phone"),
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
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600",
            images = listOf("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.4,
            reviewsCount = 890,
            brand = "OnePlus",
            tags = listOf("mobiles", "smartphones", "oneplus", "5g", "phone"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_mob_4",
            title = "Xiaomi Redmi Note 13 Pro+ 5G (Fusion Purple, 256GB)",
            description = "Curved AMOLED 1.5K display, 200MP camera with OIS, 120W HyperCharge, IP68 water & dust resistance.",
            price = 29999.0,
            mrpPrice = 33999.0,
            discountPercentage = 12.0,
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600",
            images = listOf("https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=600"),
            stockQuantity = 30,
            inStock = true,
            rating = 4.3,
            reviewsCount = 670,
            brand = "Xiaomi",
            tags = listOf("mobiles", "smartphones", "xiaomi", "redmi", "5g", "phone"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_mob_5",
            title = "Realme 12 Pro 5G (Submarine Blue, 128GB)",
            description = "Luxury watch design, 32MP telephoto portrait camera, Snapdragon 6 Gen 1, 67W SUPERVOOC charging.",
            price = 23999.0,
            mrpPrice = 28999.0,
            discountPercentage = 17.0,
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600",
            images = listOf("https://images.unsplash.com/photo-1580910051074-3eb694886505?w=600"),
            stockQuantity = 28,
            inStock = true,
            rating = 4.3,
            reviewsCount = 540,
            brand = "Realme",
            tags = listOf("mobiles", "smartphones", "realme", "5g", "phone"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_mob_6",
            title = "Vivo V30 Pro 5G (Andaman Blue, 512GB)",
            description = "Co-engineered with ZEISS optics, 50MP triple main camera system, studio-quality Aura Light portrait, 5000mAh battery.",
            price = 41999.0,
            mrpPrice = 46999.0,
            discountPercentage = 11.0,
            categoryId = "mobiles",
            categoryName = "Mobiles",
            imageUrl = "https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=600",
            images = listOf("https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=600"),
            stockQuantity = 20,
            inStock = true,
            rating = 4.6,
            reviewsCount = 420,
            brand = "Vivo",
            tags = listOf("mobiles", "smartphones", "vivo", "5g", "zeiss", "camera"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 2. ELECTRONICS (6 Products)
        // ==========================================
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
            images = listOf("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600"),
            stockQuantity = 20,
            inStock = true,
            rating = 4.6,
            reviewsCount = 760,
            brand = "Sony",
            tags = listOf("electronics", "headphones", "sony", "anc", "audio", "wireless"),
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
            images = listOf("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600"),
            stockQuantity = 15,
            inStock = true,
            rating = 4.8,
            reviewsCount = 940,
            brand = "Apple",
            tags = listOf("electronics", "laptops", "apple", "macbook", "laptop", "computer"),
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
            images = listOf("https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600"),
            stockQuantity = 60,
            inStock = true,
            rating = 4.1,
            reviewsCount = 15400,
            brand = "boAt",
            tags = listOf("electronics", "earbuds", "boat", "audio", "wireless", "headphones"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_elec_4",
            title = "Dell XPS 15 9530 Core i7 (16GB RAM, 1TB SSD, RTX 4050)",
            description = "OLED 3.5K touch display, Intel Core i7 13th Gen, dedicated NVIDIA GeForce RTX 4050 GPU, machined aluminum chassis.",
            price = 159990.0,
            mrpPrice = 189990.0,
            discountPercentage = 16.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600",
            images = listOf("https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600"),
            stockQuantity = 10,
            inStock = true,
            rating = 4.7,
            reviewsCount = 310,
            brand = "Dell",
            tags = listOf("electronics", "laptops", "dell", "laptop", "gaming", "computer"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_elec_5",
            title = "Apple iPad Air M2 (11-inch, Wi-Fi, 128GB - Space Gray)",
            description = "Apple M2 chip, 11-inch Liquid Retina display with True Tone, landscape 12MP front camera with Center Stage, USB-C.",
            price = 59900.0,
            mrpPrice = 64900.0,
            discountPercentage = 8.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600",
            images = listOf("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600"),
            stockQuantity = 22,
            inStock = true,
            rating = 4.8,
            reviewsCount = 820,
            brand = "Apple",
            tags = listOf("electronics", "tablets", "ipad", "apple", "tablet"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_elec_6",
            title = "Canon EOS R50 Mirrorless Camera with RF-S 18-45mm Lens",
            description = "24.2MP APS-C sensor, 4K 30p uncropped video, Dual Pixel CMOS AF II, compact lightweight creator camera.",
            price = 56990.0,
            mrpPrice = 64995.0,
            discountPercentage = 12.0,
            categoryId = "electronics",
            categoryName = "Electronics",
            imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600",
            images = listOf("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600"),
            stockQuantity = 14,
            inStock = true,
            rating = 4.5,
            reviewsCount = 290,
            brand = "Canon",
            tags = listOf("electronics", "camera", "canon", "mirrorless", "photography"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 3. FASHION (6 Products)
        // ==========================================
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
            images = listOf("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"),
            stockQuantity = 30,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1820,
            brand = "Nike",
            tags = listOf("fashion", "shoes", "nike", "footwear", "sneakers", "running"),
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
            images = listOf("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600"),
            stockQuantity = 45,
            inStock = true,
            rating = 4.3,
            reviewsCount = 670,
            brand = "Levi's",
            tags = listOf("fashion", "jeans", "levis", "clothing", "denim", "pants"),
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
            images = listOf("https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600"),
            stockQuantity = 50,
            inStock = true,
            rating = 4.2,
            reviewsCount = 430,
            brand = "Puma",
            tags = listOf("fashion", "tshirt", "puma", "polo", "clothing", "menswear"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_fash_4",
            title = "Zara Women's Floral Printed Tiered Midi Dress",
            description = "Flowy georgette fabric with subtle floral accents, V-neckline, puff sleeves, and tiered hemline.",
            price = 2990.0,
            mrpPrice = 4590.0,
            discountPercentage = 35.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600",
            images = listOf("https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600"),
            stockQuantity = 24,
            inStock = true,
            rating = 4.6,
            reviewsCount = 380,
            brand = "Zara",
            tags = listOf("fashion", "dress", "zara", "womenswear", "clothing", "floral"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_fash_5",
            title = "Woodland Men's Khaki Nubuck Leather Casual Boots",
            description = "Sturdy all-weather outdoor boots crafted from heavy-duty genuine nubuck leather with grooved rubber traction sole.",
            price = 3795.0,
            mrpPrice = 4995.0,
            discountPercentage = 24.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=600",
            images = listOf("https://images.unsplash.com/photo-1520639888713-7851133b1ed0?w=600"),
            stockQuantity = 28,
            inStock = true,
            rating = 4.4,
            reviewsCount = 740,
            brand = "Woodland",
            tags = listOf("fashion", "boots", "woodland", "shoes", "leather", "footwear"),
            deliveryDays = 3,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_fash_6",
            title = "Adidas Ultraboost Light Running Shoes (Core Black)",
            description = "Lightest Boost cushioning yet, Linear Energy Push system, Continental Natural Rubber outsole for ultimate grip.",
            price = 11999.0,
            mrpPrice = 18999.0,
            discountPercentage = 37.0,
            categoryId = "fashion",
            categoryName = "Fashion",
            imageUrl = "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600",
            images = listOf("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=600"),
            stockQuantity = 18,
            inStock = true,
            rating = 4.7,
            reviewsCount = 910,
            brand = "Adidas",
            tags = listOf("fashion", "shoes", "adidas", "ultraboost", "running", "footwear"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 4. BEAUTY (6 Products)
        // ==========================================
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
            images = listOf("https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600"),
            stockQuantity = 50,
            inStock = true,
            rating = 4.5,
            reviewsCount = 4210,
            brand = "Minimalist",
            tags = listOf("beauty", "skincare", "serum", "minimalist", "niacinamide", "face"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_2",
            title = "Philips Multi Grooming Kit Series 3000 (9-in-1)",
            description = "Self-sharpening skin-friendly blades, 60 min runtime, 7 impact-resistant combs for beard, hair, and body trimming.",
            price = 1795.0,
            mrpPrice = 2295.0,
            discountPercentage = 22.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1621607512214-68297480165e?w=600",
            images = listOf("https://images.unsplash.com/photo-1621607512214-68297480165e?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.4,
            reviewsCount = 890,
            brand = "Philips",
            tags = listOf("beauty", "grooming", "philips", "trimmer", "shaver", "mens-grooming"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_3",
            title = "Maybelline Superstay Matte Ink Liquid Lipstick",
            description = "Up to 16 hours saturated matte color, unique arrow applicator for precise application, transfer-proof formula.",
            price = 525.0,
            mrpPrice = 699.0,
            discountPercentage = 25.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600",
            images = listOf("https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600"),
            stockQuantity = 40,
            inStock = true,
            rating = 4.5,
            reviewsCount = 2890,
            brand = "Maybelline",
            tags = listOf("beauty", "lipstick", "maybelline", "makeup", "cosmetics", "matte"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_4",
            title = "Forest Essentials Soundarya Radiance Cream with 24K Gold",
            description = "Ayurvedic formulation with pure 24 Karat Gold Bhasma and SPF 25 to hydrate, firm, and illuminate complexion.",
            price = 4475.0,
            mrpPrice = 4975.0,
            discountPercentage = 10.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600",
            images = listOf("https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?w=600"),
            stockQuantity = 15,
            inStock = true,
            rating = 4.7,
            reviewsCount = 310,
            brand = "Forest Essentials",
            tags = listOf("beauty", "cream", "forest-essentials", "ayurveda", "luxury", "skincare"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_5",
            title = "L'Oreal Paris Extraordinary Oil Hair Serum (100ml)",
            description = "Blend of 6 rare floral oils for glossy, smooth, frizz-free hair without greasiness. Heat protection up to 230°C.",
            price = 499.0,
            mrpPrice = 649.0,
            discountPercentage = 23.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1608248597359-3a335a0d33e5?w=600",
            images = listOf("https://images.unsplash.com/photo-1608248597359-3a335a0d33e5?w=600"),
            stockQuantity = 45,
            inStock = true,
            rating = 4.4,
            reviewsCount = 1420,
            brand = "L'Oreal",
            tags = listOf("beauty", "haircare", "loreal", "serum", "hair-oil"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_beau_6",
            title = "Kama Ayurveda Pure Rose Water Face Mist (200ml)",
            description = "Steam-distilled from pure Kannauj roses. Hydrates, balances skin pH, tightens pores, and refreshes instantly.",
            price = 1495.0,
            mrpPrice = 1650.0,
            discountPercentage = 9.0,
            categoryId = "beauty",
            categoryName = "Beauty",
            imageUrl = "https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600",
            images = listOf("https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?w=600"),
            stockQuantity = 25,
            inStock = true,
            rating = 4.8,
            reviewsCount = 680,
            brand = "Kama Ayurveda",
            tags = listOf("beauty", "toner", "rosewater", "kama-ayurveda", "skincare", "mist"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 5. HOME (6 Products)
        // ==========================================
        FirestoreProductDto(
            id = "demo_home_1",
            title = "Wakefit Memory Foam Orthopedic Mattress (Queen)",
            description = "High-density pressure-relieving memory foam with breathable fabric cover and 10-year manufacturer warranty.",
            price = 9890.0,
            mrpPrice = 14290.0,
            discountPercentage = 31.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600",
            images = listOf("https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600"),
            stockQuantity = 12,
            inStock = true,
            rating = 4.6,
            reviewsCount = 3890,
            brand = "Wakefit",
            tags = listOf("home", "furniture", "mattress", "wakefit", "bedroom"),
            deliveryDays = 4,
            isCodAvailable = false
        ),
        FirestoreProductDto(
            id = "demo_home_2",
            title = "Solimo 100% Cotton 300 TC Bedsheet (King)",
            description = "Sateen weave soft breathable bedsheet with 2 matching pillow covers, fade resistant and easy machine wash.",
            price = 999.0,
            mrpPrice = 1999.0,
            discountPercentage = 50.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600",
            images = listOf("https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600"),
            stockQuantity = 50,
            inStock = true,
            rating = 4.3,
            reviewsCount = 1200,
            brand = "Solimo",
            tags = listOf("home", "bedsheet", "cotton", "solimo", "linen", "bedroom"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_home_3",
            title = "Philips Smart Air Purifier 1000i Series with HEPA Filter",
            description = "Captures 99.97% of ultrafine particles down to 0.003 microns, real-time air quality display, app-connected control.",
            price = 8999.0,
            mrpPrice = 12995.0,
            discountPercentage = 31.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=600",
            images = listOf("https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=600"),
            stockQuantity = 18,
            inStock = true,
            rating = 4.5,
            reviewsCount = 520,
            brand = "Philips",
            tags = listOf("home", "air-purifier", "philips", "appliance", "clean-air"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_home_4",
            title = "Milton Thermosteel Flip Lid Insulated Flask (1000ml)",
            description = "24-hour hot and cold temperature retention, 100% rust-proof 304 grade stainless steel, leak-proof design.",
            price = 899.0,
            mrpPrice = 1199.0,
            discountPercentage = 25.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600",
            images = listOf("https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600"),
            stockQuantity = 40,
            inStock = true,
            rating = 4.6,
            reviewsCount = 4120,
            brand = "Milton",
            tags = listOf("home", "flask", "bottle", "milton", "kitchen", "steel"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_home_5",
            title = "Prestige PIC 20 1200-Watt Induction Cooktop",
            description = "Indian menu presets, anti-magnetic wall, aerodynamic cooling system, push button controls with pause function.",
            price = 1999.0,
            mrpPrice = 3495.0,
            discountPercentage = 43.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600",
            images = listOf("https://images.unsplash.com/photo-1556911220-e15b29be8c8f?w=600"),
            stockQuantity = 22,
            inStock = true,
            rating = 4.3,
            reviewsCount = 1840,
            brand = "Prestige",
            tags = listOf("home", "cooktop", "induction", "prestige", "kitchen", "appliance"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_home_6",
            title = "Wipro Smart 20W LED Batten with Tunable White & Color",
            description = "Wi-Fi enabled LED batten, 16 million colors + warm to cool white dimming, voice control via Alexa & Google Assistant.",
            price = 699.0,
            mrpPrice = 1299.0,
            discountPercentage = 46.0,
            categoryId = "home",
            categoryName = "Home",
            imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600",
            images = listOf("https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.2,
            reviewsCount = 630,
            brand = "Wipro",
            tags = listOf("home", "lighting", "wipro", "smart-home", "led", "bulb"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 6. GROCERIES (6 Products)
        // ==========================================
        FirestoreProductDto(
            id = "demo_groc_1",
            title = "Tata Tea Gold Premium Black Tea (1kg)",
            description = "Exquisite tea leaves blended with 15% gently rolled aromatic long leaves for superior aroma and taste.",
            price = 485.0,
            mrpPrice = 580.0,
            discountPercentage = 16.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=600",
            images = listOf("https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=600"),
            stockQuantity = 40,
            inStock = true,
            rating = 4.6,
            reviewsCount = 2150,
            brand = "Tata Tea",
            tags = listOf("groceries", "tea", "tata", "beverages", "grocery", "chai"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_2",
            title = "Fortune Sunlite Refined Sunflower Oil (1L Pouch)",
            description = "Enriched with Vitamin A & D, light and easy to digest, perfect for everyday healthy cooking.",
            price = 135.0,
            mrpPrice = 165.0,
            discountPercentage = 18.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600",
            images = listOf("https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600"),
            stockQuantity = 60,
            inStock = true,
            rating = 4.4,
            reviewsCount = 3410,
            brand = "Fortune",
            tags = listOf("groceries", "oil", "fortune", "cooking", "grocery"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_3",
            title = "Aashirvaad Superior MP Shudh Chakki Atta (10kg)",
            description = "100% pure whole wheat grain flour, ground using traditional stone chakki process for softest rotis with natural dietary fibre.",
            price = 425.0,
            mrpPrice = 495.0,
            discountPercentage = 14.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600",
            images = listOf("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600"),
            stockQuantity = 50,
            inStock = true,
            rating = 4.7,
            reviewsCount = 5120,
            brand = "Aashirvaad",
            tags = listOf("groceries", "flour", "atta", "aashirvaad", "grocery", "wheat"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_4",
            title = "Daawat Rozana Gold Basmati Rice (5kg)",
            description = "Finest aged basmati rice grains with distinct rich aroma, non-sticky fluffy texture, ideal for everyday pulao and biryani.",
            price = 399.0,
            mrpPrice = 525.0,
            discountPercentage = 24.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600",
            images = listOf("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600"),
            stockQuantity = 45,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1890,
            brand = "Daawat",
            tags = listOf("groceries", "rice", "basmati", "daawat", "grocery", "grains"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_5",
            title = "Nescafe Classic 100% Pure Instant Coffee Jar (200g)",
            description = "Signature rich aroma and full-bodied classic roasted flavor made from carefully selected Robusta coffee beans.",
            price = 595.0,
            mrpPrice = 690.0,
            discountPercentage = 14.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600",
            images = listOf("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.6,
            reviewsCount = 3890,
            brand = "Nescafe",
            tags = listOf("groceries", "coffee", "nescafe", "beverage", "grocery"),
            deliveryDays = 1,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_groc_6",
            title = "Kellogg's Real Almond & Honey Corn Flakes (1kg)",
            description = "Crispy oven-toasted corn flakes loaded with crunchy almond slices and genuine natural honey for an energetic breakfast.",
            price = 449.0,
            mrpPrice = 550.0,
            discountPercentage = 18.0,
            categoryId = "groceries",
            categoryName = "Groceries",
            imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600",
            images = listOf("https://images.unsplash.com/photo-1525351484163-7529414344d8?w=600"),
            stockQuantity = 30,
            inStock = true,
            rating = 4.3,
            reviewsCount = 980,
            brand = "Kellogg's",
            tags = listOf("groceries", "cereal", "kelloggs", "breakfast", "grocery", "cornflakes"),
            deliveryDays = 1,
            isCodAvailable = true
        ),

        // ==========================================
        // 7. SPORTS (6 Products)
        // ==========================================
        FirestoreProductDto(
            id = "demo_sport_1",
            title = "Yonex Muscle Power 29 Badminton Racket",
            description = "High modulus graphite frame, isometric head shape, built-in T-Joint for extreme durability and pinpoint smash accuracy.",
            price = 2490.0,
            mrpPrice = 3490.0,
            discountPercentage = 29.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600",
            images = listOf("https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600"),
            stockQuantity = 25,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1120,
            brand = "Yonex",
            tags = listOf("sports", "badminton", "yonex", "racket", "fitness"),
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
            images = listOf("https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.2,
            reviewsCount = 740,
            brand = "Nivia",
            tags = listOf("sports", "football", "nivia", "ball", "soccer"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_sport_3",
            title = "Cosco Light Cricket Tennis Ball Pack (6 Balls)",
            description = "High bounce durable felt covered cricket tennis balls, engineered for tournament and box cricket games.",
            price = 420.0,
            mrpPrice = 540.0,
            discountPercentage = 22.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=600",
            images = listOf("https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=600"),
            stockQuantity = 50,
            inStock = true,
            rating = 4.4,
            reviewsCount = 1540,
            brand = "Cosco",
            tags = listOf("sports", "cricket", "cosco", "tennis-ball", "ball"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_sport_4",
            title = "Strauss Anti-Skid 6mm EVA Yoga Mat with Carry Strap",
            description = "Dual-sided textured non-slip surface, lightweight extra thick 6mm cushioning for joint support during yoga and pilates.",
            price = 699.0,
            mrpPrice = 1299.0,
            discountPercentage = 46.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=600",
            images = listOf("https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=600"),
            stockQuantity = 40,
            inStock = true,
            rating = 4.3,
            reviewsCount = 890,
            brand = "Strauss",
            tags = listOf("sports", "yoga", "mat", "strauss", "fitness", "workout"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_sport_5",
            title = "Boldfit Heavy Pull-Up Resistance Bands Set of 3",
            description = "100% natural Malaysian latex bands for assisted pull-ups, strength training, calisthenics, and full body stretching.",
            price = 849.0,
            mrpPrice = 1699.0,
            discountPercentage = 50.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=600",
            images = listOf("https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=600"),
            stockQuantity = 30,
            inStock = true,
            rating = 4.5,
            reviewsCount = 2140,
            brand = "Boldfit",
            tags = listOf("sports", "gym", "resistance-bands", "boldfit", "workout", "fitness"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_sport_6",
            title = "Decathlon Kipsta 20L Water Resistant Sports Duffle Bag",
            description = "Compact foldable gym and team sport bag with separate shoe compartment, adjustable padded shoulder strap.",
            price = 599.0,
            mrpPrice = 899.0,
            discountPercentage = 33.0,
            categoryId = "sports",
            categoryName = "Sports",
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600",
            images = listOf("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600"),
            stockQuantity = 35,
            inStock = true,
            rating = 4.6,
            reviewsCount = 1270,
            brand = "Decathlon",
            tags = listOf("sports", "bag", "duffle", "decathlon", "gym", "kipsta"),
            deliveryDays = 2,
            isCodAvailable = true
        ),

        // ==========================================
        // 8. ACCESSORIES (6 Products)
        // ==========================================
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
            images = listOf("https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=600"),
            stockQuantity = 28,
            inStock = true,
            rating = 4.6,
            reviewsCount = 890,
            brand = "Titan",
            tags = listOf("accessories", "watch", "titan", "analog", "watches"),
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
            images = listOf("https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600"),
            stockQuantity = 45,
            inStock = true,
            rating = 4.2,
            reviewsCount = 6840,
            brand = "boAt",
            tags = listOf("accessories", "smartwatch", "boat", "fitness", "bluetooth"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_acc_3",
            title = "Fastrack Polarized UV Protected Pilot Aviator Sunglasses",
            description = "Classic metal frame with scratch-resistant polarized lenses that eliminate 99% glare and block harmful UVA/UVB rays.",
            price = 1399.0,
            mrpPrice = 2199.0,
            discountPercentage = 36.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600",
            images = listOf("https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600"),
            stockQuantity = 32,
            inStock = true,
            rating = 4.3,
            reviewsCount = 490,
            brand = "Fastrack",
            tags = listOf("accessories", "sunglasses", "fastrack", "eyewear", "aviator"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_acc_4",
            title = "Wildcraft 30L Ergonomic Water Resistant Backpack",
            description = "Three spacious compartments with dedicated 15.6-inch laptop sleeve, padded shoulder straps, and abrasion-resistant base.",
            price = 1699.0,
            mrpPrice = 2799.0,
            discountPercentage = 39.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600",
            images = listOf("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600"),
            stockQuantity = 38,
            inStock = true,
            rating = 4.5,
            reviewsCount = 1120,
            brand = "Wildcraft",
            tags = listOf("accessories", "backpack", "wildcraft", "bag", "laptop-bag"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_acc_5",
            title = "Fossil Gen 6 Touchscreen Smartwatch (Smoke Stainless Steel)",
            description = "Wear OS by Google, Snapdragon 4100+ platform, SpO2 sensor, continuous heart rate monitoring, Google Assistant built-in.",
            price = 14995.0,
            mrpPrice = 24995.0,
            discountPercentage = 40.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600",
            images = listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600"),
            stockQuantity = 16,
            inStock = true,
            rating = 4.4,
            reviewsCount = 340,
            brand = "Fossil",
            tags = listOf("accessories", "smartwatch", "fossil", "wear-os", "watches"),
            deliveryDays = 2,
            isCodAvailable = true
        ),
        FirestoreProductDto(
            id = "demo_acc_6",
            title = "Tommy Hilfiger Genuine Leather Men's Bi-Fold Wallet",
            description = "Pure milled nappa leather, RFID blocking technology, 6 card slots, 2 currency compartments, and embossed brand logo.",
            price = 1799.0,
            mrpPrice = 2999.0,
            discountPercentage = 40.0,
            categoryId = "accessories",
            categoryName = "Accessories",
            imageUrl = "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600",
            images = listOf("https://images.unsplash.com/photo-1627123424574-724758594e93?w=600"),
            stockQuantity = 30,
            inStock = true,
            rating = 4.6,
            reviewsCount = 780,
            brand = "Tommy Hilfiger",
            tags = listOf("accessories", "wallet", "tommy-hilfiger", "leather", "bifold"),
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

    fun isCategoryMatch(productCategory: String, targetCategory: String): Boolean {
        val cat = productCategory.trim().lowercase()
        val target = targetCategory.trim().lowercase()
        if (target == "all" || target.isBlank()) return true
        if (cat == target || cat.contains(target) || target.contains(cat)) return true
        if (target in listOf("mobiles", "smartphones", "phone", "phones") && (cat == "mobiles" || cat == "smartphones")) return true
        if (target in listOf("electronics", "electronic", "laptops", "tablets") && (cat == "electronics" || cat == "laptops" || cat == "tablets" || cat == "mobiles" || cat == "accessories")) return true
        if (target in listOf("fashion", "clothing", "apparel", "wear", "shoes") && (cat == "fashion" || cat.contains("shirt") || cat.contains("shoes") || cat.contains("dress") || cat.contains("jeans"))) return true
        if (target in listOf("beauty", "skincare", "cosmetics", "grooming", "fragrances") && (cat == "beauty" || cat == "skin-care" || cat == "fragrances")) return true
        if (target in listOf("home", "furniture", "kitchen", "appliances", "home-decoration") && (cat == "home" || cat == "furniture" || cat.contains("decor") || cat.contains("kitchen"))) return true
        if (target in listOf("groceries", "grocery", "food") && (cat == "groceries" || cat == "grocery")) return true
        if (target in listOf("sports", "fitness", "sport") && (cat == "sports" || cat.contains("sports"))) return true
        if (target in listOf("accessories", "accessory", "watches") && (cat == "accessories" || cat.contains("watch") || cat.contains("bag"))) return true
        return false
    }

    fun filterByCategory(categoryName: String): List<ProductEntity> {
        return toProductEntityList().filter {
            isCategoryMatch(it.category, categoryName)
        }
    }

    fun search(query: String): List<SearchProductEntity> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        return demoProducts.filter { product ->
            val title = product.title.lowercase()
            val desc = product.description.lowercase()
            val brand = product.brand.lowercase()
            val catName = product.categoryName.lowercase()
            val catId = product.categoryId.lowercase()
            val tags = product.tags.map { it.lowercase() }

            title.contains(q) ||
            desc.contains(q) ||
            brand.contains(q) ||
            catName.contains(q) ||
            catId.contains(q) ||
            tags.any { it.contains(q) } ||
            (q in listOf("mobiles", "smartphones", "phone", "phones") && (catId == "mobiles" || catName.contains("mobile") || tags.contains("mobiles") || tags.contains("smartphones"))) ||
            (q in listOf("electronics", "electronic", "laptops", "tablets") && (catId == "electronics" || catName.contains("electronic") || tags.contains("electronics") || tags.contains("laptops"))) ||
            (q in listOf("fashion", "clothing", "clothes", "shoes", "wear") && (catId == "fashion" || catName.contains("fashion") || tags.contains("fashion") || tags.contains("shoes"))) ||
            (q in listOf("beauty", "skincare", "cosmetics", "grooming") && (catId == "beauty" || catName.contains("beauty") || tags.contains("beauty") || tags.contains("skincare"))) ||
            (q in listOf("home", "furniture", "kitchen", "appliances") && (catId == "home" || catName.contains("home") || tags.contains("home") || tags.contains("furniture"))) ||
            (q in listOf("groceries", "grocery", "food") && (catId == "groceries" || catName.contains("groceries") || tags.contains("groceries") || tags.contains("grocery"))) ||
            (q in listOf("sports", "fitness", "sport") && (catId == "sports" || catName.contains("sports") || tags.contains("sports") || tags.contains("fitness"))) ||
            (q in listOf("accessories", "accessory", "watches", "watch") && (catId == "accessories" || catName.contains("accessories") || tags.contains("accessories") || tags.contains("watch")))
        }.map { it.toSearchProductEntity() }
    }
}

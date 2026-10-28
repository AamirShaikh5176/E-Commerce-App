## 📱 App Screenshots

<table>
  <tr>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/01_Splash_Screen.jpg" width="100%" alt="Splash Screen"><br><sub>Splash Screen</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/02_Home_Screen.jpg" width="100%" alt="Home Screen"><br><sub>Home Screen</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/03_Home_Trending_and_Mega_Savings.jpg" width="100%" alt="Home Trending and Mega Savings"><br><sub>Home Trending and Mega Savings</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/04_Search_Screen.jpg" width="100%" alt="Search Screen"><br><sub>Search Screen</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/05_Product_Details_Top.jpg" width="100%" alt="Product Details Top"><br><sub>Product Details Top</sub></td>
  </tr>
  <tr>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/06_Product_Details_Offers_and_Specs.jpg" width="100%" alt="Product Details Offers and Specs"><br><sub>Product Details Offers and Specs</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/07_Favorites.jpg" width="100%" alt="Favorites"><br><sub>Favorites</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/08_Shopping_Cart.jpg" width="100%" alt="Shopping Cart"><br><sub>Shopping Cart</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/09_Checkout_Select_Delivery_Address.jpg" width="100%" alt="Checkout Select Delivery Address"><br><sub>Checkout Select Delivery Address</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/10_Checkout_Payment_Method.jpg" width="100%" alt="Checkout Payment Method"><br><sub>Checkout Payment Method</sub></td>
  </tr>
  <tr>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/11_Checkout_UPI_QR_Payment.jpg" width="100%" alt="Checkout UPI QR Payment"><br><sub>Checkout UPI QR Payment</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/12_User_Profile.jpg" width="100%" alt="User Profile"><br><sub>User Profile</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/13_Edit_Profile.jpg" width="100%" alt="Edit Profile"><br><sub>Edit Profile</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/14_Help_and_Support.jpg" width="100%" alt="Help and Support"><br><sub>Help and Support</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/15_Settings_Appearance_and_Notifications.jpg" width="100%" alt="Settings Appearance and Notifications"><br><sub>Settings Appearance and Notifications</sub></td>
  </tr>
  <tr>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/16_Settings_Account_and_Privacy.jpg" width="100%" alt="Settings Account and Privacy"><br><sub>Settings Account and Privacy</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/17_Admin_Sign_In.jpg" width="100%" alt="Admin Sign In"><br><sub>Admin Sign In</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/18_Admin_Console_Dashboard.jpg" width="100%" alt="Admin Console Dashboard"><br><sub>Admin Console Dashboard</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/19_Admin_Management_Portals_and_Recent_Orders.jpg" width="100%" alt="Admin Management Portals and Recent Orders"><br><sub>Admin Management Portals and Recent Orders</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/20_Admin_Product_Inventory.jpg" width="100%" alt="Admin Product Inventory"><br><sub>Admin Product Inventory</sub></td>
  </tr>
  <tr>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/21_Admin_Add_New_Product.jpg" width="100%" alt="Admin Add New Product"><br><sub>Admin Add New Product</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/22_Admin_Categories.jpg" width="100%" alt="Admin Categories"><br><sub>Admin Categories</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/23_Admin_Orders_and_Fulfillment.jpg" width="100%" alt="Admin Orders and Fulfillment"><br><sub>Admin Orders and Fulfillment</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/24_Admin_Cancelled_Orders_and_Returns.jpg" width="100%" alt="Admin Cancelled Orders and Returns"><br><sub>Admin Cancelled Orders and Returns</sub></td>
    <td align="center" width="20%"><img src="BazaarGo_Screenshots/25_Admin_Registered_Customers.jpg" width="100%" alt="Admin Registered Customers"><br><sub>Admin Registered Customers</sub></td>
  </tr>
</table>

# 🛒 BazaarGo — Modern E-Commerce Android App

---

## 📖 Overview

**BazaarGo** is a modern Android e-commerce application designed to provide a complete online shopping experience with customer authentication, product discovery, cart and wishlist management, multiple payment methods, order tracking, wallet functionality, SuperCoins rewards, cancellations, returns, refunds, and a dedicated administrator console.

The application is developed natively using **Kotlin and Jetpack Compose** and follows **Clean Architecture + MVVM** principles.

Firebase provides the core cloud infrastructure for authentication and database operations, while secure server-side functionality handles payment-related operations.

---

# ✨ Features

## 👤 Customer Experience

### Authentication

- Email and password registration
- User login
- Password reset
- Secure logout
- Guest browsing
- Customer profile
- Account management
- Delivery address management
- Secure account deletion

### 🛍️ Product Catalog

- Product browsing
- Category-based filtering
- Product search
- Product details
- Product images
- Product pricing
- Product stock information
- Brand information
- Product descriptions
- Product variants where supported
- Real-time Firestore catalog synchronization

### 🛒 Shopping Cart

- Add products to cart
- Remove products
- Increase/decrease quantity
- Automatic price calculation
- Stock-aware quantity handling
- Persistent cart
- Local Room database caching

### ❤️ Wishlist

- Add/remove products from wishlist
- Persistent wishlist
- Product navigation from wishlist
- Local persistence

---

# 💳 Payment System

BazaarGo supports multiple payment methods.

## 1. Razorpay Online Payment

Razorpay Standard Checkout supports online payments such as:

- UPI
- Credit Cards
- Debit Cards
- Net Banking
- Other supported Razorpay payment methods

Payment architecture:

```text
Android Application
        ↓
Secure Backend
        ↓
Razorpay Order Creation
        ↓
Razorpay Checkout
        ↓
Payment
        ↓
Backend Signature Verification
        ↓
Firestore Order Update
````

The Razorpay secret key is never exposed to the Android application.

---

## 2. Manual UPI QR Payment

Customers can also pay using the application's bundled UPI QR code.

### Customer Flow

```text
Select UPI QR Payment
        ↓
Display QR Code
        ↓
Customer scans using UPI app
        ↓
Customer pays exact order amount
        ↓
"I've Completed Payment"
        ↓
Payment Verification Pending
        ↓
Admin Reviews Payment
```

The customer cannot directly mark the order as paid.

### Admin Flow

```text
Verification Pending
        ↓
Admin Reviews Payment
        ↓
 ┌───────────────┐
 ↓               ↓
Confirm         Reject
 ↓               ↓
PAID        PAYMENT_REJECTED
```

Administrators can:

* View customer details
* View order amount
* View ordered products
* View product categories
* Confirm payment
* Reject payment
* Add rejection reason
* Review verification history

Every administrative action is recorded in the audit trail.

---

## 3. BazaarGo Wallet

The application provides an in-app INR wallet.

Features include:

* Wallet balance
* Add Money
* Razorpay-powered wallet top-up
* Wallet transaction history
* Wallet credits
* Wallet debits
* Wallet refunds
* Wallet payments during checkout
* Split payment where supported
* Server-controlled wallet balance

Wallet architecture:

```text
Customer
   ↓
Add Money
   ↓
Razorpay
   ↓
Payment Verification
   ↓
Server-side Wallet Credit
   ↓
Firestore Wallet Ledger
```

The Android client cannot directly modify the wallet balance.

---

## 4. SuperCoins

BazaarGo includes a loyalty/rewards system called **SuperCoins**.

Features:

* SuperCoins balance
* Earn coins from eligible orders
* Redeem coins at checkout
* Transaction history
* Lifetime earned coins
* Lifetime redeemed coins

SuperCoins are separate from the INR wallet.

---

## 5. Cash on Delivery

Customers can choose:

**Cash on Delivery (COD)**

where available.

The payment state and order fulfillment state are maintained separately.

---

# 📦 Order Management

Every order contains a complete snapshot of the products purchased.

For each ordered product, the application stores:

* Product ID
* Product name
* Category ID
* Category name
* Quantity
* Unit price
* Total price
* Product image where available

This prevents future product catalog changes from incorrectly changing historical order information.

Example:

```text
Order
 └── Items
      ├── Product ID
      ├── Product Name
      ├── Category
      ├── Quantity
      ├── Unit Price
      ├── Total Price
      └── Product Image
```

---

# 🚚 Order Fulfillment

Orders follow a controlled fulfillment lifecycle.

```text
PLACED
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
SHIPPED
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED
```

The Admin Console controls fulfillment progression.

Customers can view the current status through an order tracking timeline.

---

# ❌ Cancellation Management

Customers can request cancellation for eligible orders.

Typical flow:

```text
Customer
    ↓
Cancellation Requested
    ↓
Admin Review
    ↓
 ┌───────────────┐
 ↓               ↓
Approve         Reject
 ↓               ↓
Refund Flow    Rejected
```

Cancellation requests are controlled by the administrator.

Customers cannot directly approve their own cancellation or refund.

---

# 🔄 Returns

For delivered products, customers can request a return.

Return lifecycle:

```text
RETURN_REQUESTED
        ↓
RETURN_APPROVED
        ↓
PICKUP_SCHEDULED
        ↓
PICKED_UP
        ↓
PRODUCT_RECEIVED
        ↓
REFUND_INITIATED
        ↓
REFUND_PROCESSING
        ↓
REFUND_COMPLETED
        ↓
RETURNED
```

The administrator controls the return process.

---

# 💰 Refund Management

Refunds can be processed according to the original payment method and existing business rules.

Supported refund scenarios include:

* Razorpay refund
* Wallet refund
* Applicable payment reversal
* Return-based refund
* Cancellation-based refund

Refunds are tracked through explicit states.

```text
REFUND_INITIATED
        ↓
REFUND_PROCESSING
        ↓
REFUND_COMPLETED
```

Revenue is adjusted only when the refund is actually completed.

A rejected cancellation or pending refund does not incorrectly reduce revenue.

---

# 🧑‍💼 Admin Console

BazaarGo includes a dedicated administrator console.

Admin access is protected using Firebase Authentication and administrator custom claims.

Administrative access requires:

```text
request.auth.token.admin == true
```

---

## 📊 Admin Dashboard

The Admin Dashboard provides an overview of store operations.

It can include:

* Total revenue
* Total orders
* Pending orders
* Delivered orders
* Cancelled orders
* Returned orders
* Refunded amount
* Registered customers
* Low-stock products
* Out-of-stock products
* Average order value
* Sales analytics
* Recent orders
* Payment statistics
* Inventory alerts
* Return/refund statistics

All business metrics are based on application data rather than hardcoded demo values.

---

# 🏪 Admin Management Portals

The Admin Console provides dedicated management areas.

### Product Inventory & Stock

Administrators can:

* Add products
* Edit products
* Update prices
* Update stock
* Manage product information
* Manage product images
* Monitor low-stock products
* Monitor out-of-stock products

### Categories

Administrators can manage product categories and catalog organization.

### Order Fulfillment & Tracking

Administrators can:

* View orders
* Search orders
* Filter orders
* View customer details
* View ordered products
* View product categories
* Update fulfillment status
* View payment status
* View order audit trail

### Cancelled Orders & Returns

Dedicated management for:

* Cancellation requests
* Cancellation approval
* Cancellation rejection
* Return requests
* Return approval
* Return rejection
* Pickup scheduling
* Product receipt
* Refund processing
* Refund completion

### UPI Payment Verification

Administrators can review:

* Pending UPI payment claims
* Customer information
* Order information
* Ordered products
* Payment amount
* Submission timestamp

Actions:

```text
Confirm Payment
Reject Payment
```

### Registered Customers

Administrators can view registered customer information and relevant account/order information.

---

# 🧾 Audit Trail

Administrative actions are recorded for traceability.

An audit record can contain:

```text
action
adminUid
timestamp
previousStatus
newStatus
notes
```

Examples:

```text
ORDER_CONFIRMED
ORDER_SHIPPED
ORDER_DELIVERED
CANCELLATION_APPROVED
CANCELLATION_REJECTED
RETURN_APPROVED
RETURN_REJECTED
REFUND_INITIATED
REFUND_COMPLETED
UPI_PAYMENT_CONFIRMED
UPI_PAYMENT_REJECTED
```

This provides a history of important administrative changes.

---

# 🏗️ Architecture

BazaarGo follows **Clean Architecture + MVVM** with unidirectional state flow.

```text
┌───────────────────────────────────────────┐
│              Presentation Layer           │
│                                           │
│ Jetpack Compose │ Screens │ ViewModels    │
│ UI State        │ Events  │ Navigation   │
└──────────────────────┬────────────────────┘
                       │
                       ▼
┌───────────────────────────────────────────┐
│                Domain Layer               │
│                                           │
│ Entities │ Use Cases │ Business Rules     │
│ Repository Interfaces                     │
└──────────────────────┬────────────────────┘
                       │
                       ▼
┌───────────────────────────────────────────┐
│                 Data Layer                │
│                                           │
│ Repositories │ DTOs │ Mappers             │
│ Firestore    │ Room │ Retrofit            │
└───────────────────────────────────────────┘
```

---

# 🔄 Data Flow

A typical product purchase follows:

```text
Product Catalog
      ↓
Product Details
      ↓
Cart
      ↓
Checkout
      ↓
Payment Method
      ↓
Order Creation
      ↓
Firestore
      ↓
Admin Console
      ↓
Order Fulfillment
      ↓
Delivery
```

---

# 🛡️ Security Architecture

Security is an important part of the application.

## Firebase Authentication

Firebase Authentication is used for customer and administrator authentication.

## Admin Authorization

Administrator access is protected by Firebase custom claims.

```text
Firebase Authentication
        ↓
ID Token
        ↓
Custom Claims
        ↓
admin == true
        ↓
Admin Console
```

Customers cannot gain administrative access simply by modifying client-side state.

---

## Firestore Security

Firestore rules enforce separation between customer and administrator operations.

Customers should not be able to directly modify sensitive financial fields such as:

* Confirmed payment status
* Admin verification information
* Refund state
* Administrative audit records
* Admin-controlled fulfillment states

Payment verification and other privileged operations are controlled by the appropriate backend/admin flow.

---

# 🗄️ Firestore Database

The primary backend database is **Cloud Firestore**.

## Main Collections

```text
users
products
categories
orders
wallets
supercoins_wallets
```

---

## Order Structure

A simplified order structure:

```text
orders/{orderId}

orderId
userId
customerName
customerPhone
customerEmail
shippingAddress

items:
  - productId
  - productName
  - categoryId
  - categoryName
  - quantity
  - unitPrice
  - totalPrice
  - productImage

itemsTotal
discountTotal
deliveryFee
finalTotal
currency

paymentMethod
paymentStatus
paymentVerificationRequired
paymentVerificationStatus

orderStatus

createdAt
updatedAt

adminAuditTrail
```

---

## Wallet Structure

```text
wallets/{userId}

balance
lifetimeEarned
lifetimeRedeemed

transactions/{transactionId}
```

Wallet transactions can represent:

```text
CREDIT
DEBIT
REFUND
```

---

## SuperCoins Structure

```text
supercoins_wallets/{userId}

balance
lifetimeEarned
lifetimeRedeemed
```

---

# 🛠️ Technology Stack

## Android

| Component            | Technology                 |
| -------------------- | -------------------------- |
| Language             | Kotlin                     |
| UI                   | Jetpack Compose            |
| Design               | Material 3                 |
| Architecture         | Clean Architecture + MVVM  |
| Dependency Injection | Dagger Hilt                |
| Local Database       | Room                       |
| Async Programming    | Kotlin Coroutines          |
| State Management     | StateFlow                  |
| Navigation           | Navigation Compose         |
| Networking           | Retrofit + OkHttp          |
| JSON                 | Gson                       |
| Image Loading        | Coil Compose               |
| Animation            | Lottie Compose             |
| Payment SDK          | Razorpay Standard Checkout |

---

## Backend & Cloud

| Component           | Technology               |
| ------------------- | ------------------------ |
| Authentication      | Firebase Authentication  |
| Database            | Cloud Firestore          |
| Serverless Backend  | Firebase Cloud Functions |
| Backend Runtime     | Node.js                  |
| Payment Gateway     | Razorpay                 |
| Security            | Firebase Security Rules  |
| Admin Authorization | Firebase Custom Claims   |

---

# 📁 Project Structure

```text
E-Commerce-App-Jetpack-Compose-master/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/e_commerceapp/
│           │       │
│           │       ├── data/
│           │       │   ├── data_source/
│           │       │   ├── dto/
│           │       │   ├── mapper/
│           │       │   └── repository/
│           │       │
│           │       ├── di/
│           │       │
│           │       ├── domain/
│           │       │
│           │       ├── navigation/
│           │       │
│           │       ├── ui/
│           │       │   ├── screens/
│           │       │   │   ├── admin/
│           │       │   │   ├── auth/
│           │       │   │   ├── cart/
│           │       │   │   ├── orders/
│           │       │   │   ├── payment/
│           │       │   │   ├── profile/
│           │       │   │   ├── supercoins/
│           │       │   │   ├── wallet/
│           │       │   │   └── support/
│           │       │   │
│           │       │   └── theme/
│           │       │
│           │       └── utils/
│           │
│           └── res/
│               ├── drawable/
│               └── values/
│
├── functions/
│   └── index.js
│
├── backend/
│
├── firestore.rules
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

# ⚙️ Requirements

Before building the application, install:

* Android Studio
* JDK 21
* Android SDK
* Android SDK Build Tools
* Git
* Node.js for backend/Cloud Functions development
* Firebase CLI for Firebase deployment

The project uses:

```text
Minimum SDK: API 24
Target SDK: API 36
Compile SDK: API 36
```

---

# 🚀 Installation

## 1. Clone the Repository

```bash
git clone https://github.com/AamirShaikh5176/E-Commerce-App.git
cd E-Commerce-App
```

---

## 2. Open in Android Studio

Open the project root:

```text
E-Commerce-App-Jetpack-Compose-master
```

Allow Android Studio to synchronize Gradle dependencies.

---

# 🔥 Firebase Configuration

Create/configure a Firebase project and register the Android application with:

```text
Package Name:
com.example.e_commerceapp
```

Enable:

* Firebase Authentication
* Email/Password Authentication
* Cloud Firestore
* Required Firebase services used by the application

Place the Firebase configuration file at:

```text
app/google-services.json
```

### Important

Do not commit sensitive Firebase configuration or other secrets when the project's security policy requires them to remain private.

Deploy Firestore rules using:

```bash
firebase deploy --only firestore:rules
```

---

# 💳 Razorpay Configuration

For development, use Razorpay Test Mode.

The Android application should contain only the public Razorpay Key ID required by the checkout SDK.

The Razorpay secret key must remain on the secure backend.

Never place the secret in:

```text
Kotlin source code
BuildConfig
Android resources
APK
Git repository
Firestore
Logcat
```

For production, replace the test configuration with the appropriate production credentials through the secure backend configuration.

---

# ☁️ Firebase Cloud Functions

The preferred production architecture is:

```text
Android App
     ↓
Firebase HTTPS Cloud Function
     ↓
Razorpay
     ↓
Firestore
```

Cloud Functions are used for operations that require trusted server-side execution, such as:

* Razorpay order creation
* Payment signature verification
* Wallet crediting
* Secure financial operations
* Refund-related operations
* Payment webhooks where configured

Deploy functions using:

```bash
firebase deploy --only functions
```

---

# 🔐 Environment Variables

Backend secrets must be configured securely.

Example:

```env
RAZORPAY_KEY_ID=rzp_test_YourKeyHere
RAZORPAY_KEY_SECRET=YourRazorpaySecretHere
```

Do not commit `.env` files containing real credentials.

Use secure deployment configuration/secrets for production.

---

# 🏗️ Build

To build the debug APK:

```powershell
.\gradlew.bat assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 📱 Install on Android Device

Connect an Android device with USB debugging enabled.

Check the device:

```powershell
adb devices
```

Install the APK:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

# 📦 Release Build

For Google Play distribution, create a properly signed release build/App Bundle.

Recommended deployment flow:

```text
Development
     ↓
Debug Build
     ↓
Internal Testing
     ↓
Closed Testing
     ↓
Production Review
     ↓
Google Play Store
```

Never distribute a production release containing:

* Test credentials
* Development backend URLs
* Localhost URLs
* Temporary tunnels
* Debug-only configuration
* Server secrets

---

# 🌐 Production Architecture

The intended production architecture is:

```text
                  ┌─────────────────────┐
                  │   Android Customer  │
                  │        App          │
                  └──────────┬──────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
        Firebase Auth    Firestore       Storage
              │              │
              └──────┬───────┘
                     │
                     ▼
            Firebase Cloud Functions
                     │
                     ▼
                  Razorpay
```

Administrative operations use the same Firebase infrastructure with administrator authorization.

---

# 👨‍💻 Admin Access

The Admin Console is not intended to be accessible to normal customers.

Administrative access requires a Firebase Authentication account with the appropriate administrator custom claim.

Example:

```text
admin: true
```

The application validates the administrator token before granting access.

---

# 🧪 Development Notes

During development:

* Razorpay should remain in Test Mode.
* Do not use production payment credentials.
* Do not commit secrets.
* Do not rely on localhost for production payment flows.
* Do not use temporary tunnels as permanent payment infrastructure.
* Test Firestore security rules before production deployment.
* Test payment state transitions independently from order fulfillment.

---

# 🔄 Payment State vs Order State

BazaarGo intentionally keeps payment and fulfillment states separate.

### Payment State

```text
PENDING
PAID
COMPLETED
VERIFICATION_PENDING
PAYMENT_REJECTED
REFUNDED
```

### Order State

```text
PLACED
CONFIRMED
PROCESSING
SHIPPED
OUT_FOR_DELIVERY
DELIVERED
CANCELLED
RETURNED
```

This prevents payment confirmation from being incorrectly treated as delivery completion.

---

# 📊 Revenue Logic

Revenue should represent actual completed financial outcomes.

Important rules:

* A pending payment is not revenue.
* A rejected payment is not revenue.
* A cancellation request does not immediately reduce revenue.
* A completed refund reduces revenue.
* A rejected cancellation does not reduce revenue.
* Duplicate refund events must not cause duplicate revenue deductions.
* Wallet refunds must be reflected according to the application's financial rules.
* COD orders must not create fake online-payment transactions.

---

# 🔒 Security Principles

BazaarGo follows these security principles:

1. Never trust client-side payment confirmation.
2. Never expose payment secrets in the Android application.
3. Keep financial operations server-controlled.
4. Use Firebase Authentication for identity.
5. Use Firebase custom claims for administrator authorization.
6. Protect Firestore with security rules.
7. Maintain audit trails for sensitive administrative operations.
8. Prevent customers from changing administrative/payment verification fields.
9. Prevent duplicate wallet credits.
10. Prevent duplicate refund processing.

---

# 🧭 Main Application Modules

```text
Customer
├── Authentication
├── Home
├── Categories
├── Product Details
├── Search
├── Cart
├── Wishlist
├── Checkout
├── Payment
├── Orders
├── Order Details
├── Wallet
├── SuperCoins
├── Profile
├── Addresses
└── Support

Admin
├── Admin Login
├── Admin Dashboard
├── Product Inventory & Stock
├── Categories
├── Order Fulfillment & Tracking
├── UPI Payment Verification
├── Cancelled Orders & Returns
├── Registered Customers
├── Refund Management
└── Audit Trail
```

---

# 🎯 Project Goals

BazaarGo aims to provide:

* A modern Android shopping experience
* Secure authentication
* Real-time product and order synchronization
* Multiple payment methods
* Digital wallet functionality
* Loyalty rewards
* Manual UPI payment verification
* Controlled order fulfillment
* Cancellation and return management
* Refund management
* Professional administration tools
* Secure server-side financial operations
* Scalable Firebase-based cloud infrastructure

---

# 👨‍💻 Development

This project is built as an Android e-commerce application using modern Android development practices.

Core principles include:

* Clean Architecture
* MVVM
* Unidirectional data flow
* Dependency Injection
* Repository pattern
* Immutable UI state
* Separation of payment and fulfillment states
* Server-side financial verification
* Firebase-backed real-time data

---

# 📄 License

This project is distributed under the MIT License.

See the `LICENSE` file for more information.

---

# ⭐ Acknowledgements

This project uses and integrates technologies including:

* Kotlin
* Jetpack Compose
* Android Jetpack
* Material 3
* Dagger Hilt
* Room
* Retrofit
* Firebase
* Cloud Firestore
* Firebase Authentication
* Firebase Cloud Functions
* Razorpay
* Coil
* Lottie

---

## 👨‍💻 Developer

Aamir Shaikh

B.Tech Information Technology

Built as an Android e-commerce project using modern Android development technologies.

```
```

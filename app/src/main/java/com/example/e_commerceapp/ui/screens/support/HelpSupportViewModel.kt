package com.example.e_commerceapp.ui.screens.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String,
)

@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _ordersState = MutableStateFlow(ScreenState<List<FirestoreOrderDto>>())
    val ordersState: StateFlow<ScreenState<List<FirestoreOrderDto>>> = _ordersState.asStateFlow()

    init {
        loadUserOrders()
    }

    fun loadUserOrders() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            _ordersState.update { it.copy(isLoading = false, data = emptyList()) }
            return
        }

        viewModelScope.launch {
            _ordersState.update { it.copy(isLoading = true, errorMessage = null) }
            firestoreCatalogDataSource.getUserOrdersFlow(currentUserId)
                .catch { err ->
                    _ordersState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load orders")
                    }
                }
                .collect { list ->
                    _ordersState.update {
                        it.copy(isLoading = false, errorMessage = null, data = list)
                    }
                }
        }
    }

    val faqCategories: List<String> = listOf(
        "All",
        "Orders",
        "Payments",
        "Delivery",
        "Returns & Refunds",
        "Account",
        "Cart & Wishlist",
        "Coupons"
    )

    val faqList: List<FaqItem> = listOf(
        // Orders
        FaqItem(
            id = "ord_1",
            category = "Orders",
            question = "How do I track my order status?",
            answer = "Go to Profile > My Orders, or the Returns & Refunds tab here. Tapping any order opens live tracking with current status (Placed, Confirmed, Shipped, Out for Delivery, or Delivered) and courier tracking details."
        ),
        FaqItem(
            id = "ord_2",
            category = "Orders",
            question = "Can I cancel my order after placing it?",
            answer = "Orders can be cancelled directly from the Order Details screen while they are in 'PLACED' or 'CONFIRMED' status. Once dispatched or 'SHIPPED', cancellation is disabled; you can request a return upon delivery."
        ),
        FaqItem(
            id = "ord_3",
            category = "Orders",
            question = "Where can I view my order invoice and breakdown?",
            answer = "Open any order from My Orders or the Return & Refund Center. You will find an itemized bill showing product prices, applied coupons, delivery fees, and final paid totals."
        ),

        // Payments
        FaqItem(
            id = "pay_1",
            category = "Payments",
            question = "Which payment methods are accepted?",
            answer = "We support Cash on Delivery (COD), UPI (Google Pay, PhonePe, Paytm, BHIM), Credit/Debit Cards (Visa, Mastercard, RuPay), and Net Banking."
        ),
        FaqItem(
            id = "pay_2",
            category = "Payments",
            question = "Is my online payment safe and encrypted?",
            answer = "Yes, all transactions are secured with 256-bit SSL encryption and processed through RBI-authorized payment gateways. We never store your full card details or CVV."
        ),
        FaqItem(
            id = "pay_3",
            category = "Payments",
            question = "Money was debited, but the order failed. What should I do?",
            answer = "If money was debited for an unconfirmed order, the payment gateway automatically reconciles and refunds the amount back to your original bank account within 3 to 5 business days."
        ),

        // Delivery
        FaqItem(
            id = "del_1",
            category = "Delivery",
            question = "What is the standard delivery timeline?",
            answer = "Most orders are delivered within 2 to 4 business days. Estimated delivery dates for your pin code are shown on product pages and at checkout."
        ),
        FaqItem(
            id = "del_2",
            category = "Delivery",
            question = "Can I change my shipping address after placing an order?",
            answer = "Address changes are possible before the item is dispatched. Please contact our support team immediately with your Order ID via WhatsApp or Phone to update your delivery address."
        ),
        FaqItem(
            id = "del_3",
            category = "Delivery",
            question = "What if I am unavailable at the time of delivery?",
            answer = "Our delivery partner will attempt delivery up to 3 times and will contact you via phone. You can also reschedule delivery via the courier partner's tracking link."
        ),

        // Returns & Refunds
        FaqItem(
            id = "ret_1",
            category = "Returns & Refunds",
            question = "What is the return and replacement window?",
            answer = "We offer a 7-day return and replacement policy from the date of delivery for eligible items in unused condition with all tags and original packaging intact."
        ),
        FaqItem(
            id = "ret_2",
            category = "Returns & Refunds",
            question = "How long does it take to receive a refund?",
            answer = "Once the returned item is picked up and verified, refunds are initiated within 24 to 48 hours. Online payments reflect in 3-5 business days; COD refunds are transferred to your bank account or UPI ID."
        ),
        FaqItem(
            id = "ret_3",
            category = "Returns & Refunds",
            question = "Which items are not eligible for return?",
            answer = "Personal care, hygiene items, fragrances, and innerwear are non-returnable once unsealed. However, if received damaged, defective, or incorrect, they are 100% eligible for replacement."
        ),

        // Account
        FaqItem(
            id = "acc_1",
            category = "Account",
            question = "How do I update my account information?",
            answer = "Navigate to the Profile tab. Tap 'Edit Profile' to modify your display name, contact phone number, or manage your saved delivery addresses."
        ),
        FaqItem(
            id = "acc_2",
            category = "Account",
            question = "How do I reset or change my password?",
            answer = "Go to Profile > Change Password to update your password while logged in. If forgotten, use the 'Forgot Password' link on the Sign In screen to receive a password reset email."
        ),

        // Cart & Wishlist
        FaqItem(
            id = "cw_1",
            category = "Cart & Wishlist",
            question = "Are products saved in my cart reserved?",
            answer = "Products added to your cart are not reserved until checkout is completed. We suggest completing your purchase promptly if item stocks are low."
        ),
        FaqItem(
            id = "cw_2",
            category = "Cart & Wishlist",
            question = "Is my wishlist private and saved to my account?",
            answer = "Yes! Items saved to your wishlist are privately linked to your authenticated account and remain accessible whenever you log in on any device."
        ),

        // Coupons
        FaqItem(
            id = "coup_1",
            category = "Coupons",
            question = "How do I use a coupon code?",
            answer = "Add products to your cart and navigate to the Cart screen. Enter a valid promotional code (such as FESTIVE100 or WELCOME50) in the promo code field and tap Apply to receive an instant discount."
        ),
        FaqItem(
            id = "coup_2",
            category = "Coupons",
            question = "Why is my coupon code not applying?",
            answer = "Check the minimum order value (e.g., ₹999 for FESTIVE100 or ₹499 for WELCOME50) and ensure the coupon code is spelled correctly without extra spaces."
        )
    )
}

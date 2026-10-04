package com.example.e_commerceapp.utils

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity

/**
 * Thread-safe holder for checkout session items.
 * Distinguishes between direct "Buy Now" purchases of a single product and
 * cart-based checkouts containing multiple user cart items.
 */
object CheckoutSessionHolder {
    @Volatile
    private var buyNowItem: UserCartEntity? = null

    fun setBuyNowItem(item: UserCartEntity) {
        buyNowItem = item
    }

    fun getBuyNowItem(): UserCartEntity? = buyNowItem

    fun isBuyNow(): Boolean = buyNowItem != null

    fun clear() {
        buyNowItem = null
    }
}

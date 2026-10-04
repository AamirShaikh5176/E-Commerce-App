package com.example.e_commerceapp.utils

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity

/**
 * Thread-safe holder for checkout session items.
 * Distinguishes between direct "Buy Now" purchases of a single product and
 * cart-based checkouts containing multiple user cart items.
 * Ensures the exact item snapshot is preserved throughout checkout to order placement.
 */
object CheckoutSessionHolder {
    @Volatile
    private var buyNowItem: UserCartEntity? = null

    @Volatile
    private var cartItems: List<UserCartEntity> = emptyList()

    fun setBuyNowItem(item: UserCartEntity) {
        buyNowItem = item
        cartItems = emptyList()
    }

    fun setCartItems(items: List<UserCartEntity>) {
        cartItems = items
        buyNowItem = null
    }

    fun getBuyNowItem(): UserCartEntity? = buyNowItem

    fun getCartItems(): List<UserCartEntity> = cartItems

    fun getEffectiveCheckoutItems(): List<UserCartEntity> {
        val single = buyNowItem
        if (single != null) return listOf(single)
        return cartItems
    }

    fun isBuyNow(): Boolean = buyNowItem != null

    fun clear() {
        buyNowItem = null
        cartItems = emptyList()
    }
}

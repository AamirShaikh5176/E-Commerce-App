package com.example.e_commerceapp.data.data_source.remote.api

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.data.dto.ProductDto
import com.example.e_commerceapp.data.dto.ProductsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("products")
    suspend fun getProductsListFromApi(): ProductsResponse

    @GET("products/search")
    suspend fun getProductsListBySearchFromApi(@Query("q") query: String): ProductsResponse

    @GET("products/{id}")
    suspend fun getSingleProductByIdFromApi(@Path("id") productId: Int): ProductDto

    @GET("products/categories")
    suspend fun getAllCategoriesFromApi(): List<Category>

    @GET("products/category/{categoryName}")
    suspend fun getProductsListByCategoryNameFromApi(@Path("categoryName") categoryName: String): ProductsResponse
}

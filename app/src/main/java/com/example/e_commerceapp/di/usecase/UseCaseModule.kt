package com.example.e_commerceapp.di.usecase

import com.example.e_commerceapp.data.data_source.local.AppDao
import com.example.e_commerceapp.domain.repository.ApiRepository
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.domain.usecase.cart.DeleteCart
import com.example.e_commerceapp.domain.usecase.cart.GetAllCart
import com.example.e_commerceapp.domain.usecase.cart.GetOneCart
import com.example.e_commerceapp.domain.usecase.cart.GetTotalPrice
import com.example.e_commerceapp.domain.usecase.cart.GetUserCartBadge
import com.example.e_commerceapp.domain.usecase.cart.InsertUserCart
import com.example.e_commerceapp.domain.usecase.cart.UpdateCart
import com.example.e_commerceapp.domain.usecase.favorite.AddFavoriteProduct
import com.example.e_commerceapp.domain.usecase.favorite.DeleteFavoriteProduct
import com.example.e_commerceapp.domain.usecase.favorite.FavoriteProductsUseCase
import com.example.e_commerceapp.domain.usecase.favorite.GetAllFavoriteProducts
import com.example.e_commerceapp.domain.usecase.favorite.GetOneFavoriteProduct
import com.example.e_commerceapp.domain.usecase.product.GetAllCategory
import com.example.e_commerceapp.domain.usecase.product.GetAllProducts
import com.example.e_commerceapp.domain.usecase.product.GetOneProduct
import com.example.e_commerceapp.domain.usecase.product.GetProductsByCategory
import com.example.e_commerceapp.domain.usecase.product.ProductsUseCase
import com.example.e_commerceapp.domain.usecase.product.SearchProduct
import com.example.e_commerceapp.domain.usecase.user.FirebaseUserUseCase
import com.example.e_commerceapp.domain.usecase.user.ForgetPassword
import com.example.e_commerceapp.domain.usecase.user.LogoutUser
import com.example.e_commerceapp.domain.usecase.user.ReadUser
import com.example.e_commerceapp.domain.usecase.user.SignInUser
import com.example.e_commerceapp.domain.usecase.user.SignUpUser
import com.example.e_commerceapp.domain.usecase.user.WriteUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideFirebaseUserUseCase(
        firebaseRepository: FirebaseRepository,
    ): FirebaseUserUseCase {
        return FirebaseUserUseCase(
            signInUser = SignInUser(firebaseRepository),
            signUpUser = SignUpUser(firebaseRepository),
            readUser = ReadUser(firebaseRepository),
            writeUser = WriteUser(firebaseRepository),
            forgetPassword = ForgetPassword(firebaseRepository),
            logoutUser = LogoutUser(firebaseRepository)
        )
    }


    @Provides
    @ViewModelScoped
    fun provideProductsUseCase(
        apiRepository: ApiRepository,
    ): ProductsUseCase {
        return ProductsUseCase(
            getAllProducts = GetAllProducts(apiRepository),
            getProductsByCategory = GetProductsByCategory(apiRepository),
            getAllCategory = GetAllCategory(apiRepository),
            getOneProduct = GetOneProduct(apiRepository),
            searchProduct = SearchProduct(apiRepository)
        )
    }


    @Provides
    @ViewModelScoped
    fun provideCartsUseCase(
        localRepository: LocalRepository,
    ): CartUseCase {
        return CartUseCase(
            getAllCart = GetAllCart(localRepository),
            insertUserCart = InsertUserCart(localRepository),
            getTotalPrice = GetTotalPrice(localRepository),
            getOneCart = GetOneCart(localRepository),
            updateCart = UpdateCart(localRepository),
            deleteCart = DeleteCart(localRepository),
            getUserCartBadge = GetUserCartBadge(localRepository)
        )
    }


    @Provides
    @ViewModelScoped
    fun provideFavoriteProductsUseCase(
        localRepository: LocalRepository,
    ): FavoriteProductsUseCase {
        return FavoriteProductsUseCase(
            getAllFavoriteProducts = GetAllFavoriteProducts(localRepository),
            addFavoriteProduct = AddFavoriteProduct(localRepository),
            deleteFavoriteProduct = DeleteFavoriteProduct(localRepository),
            getOneFavoriteProduct = GetOneFavoriteProduct(localRepository)
        )
    }

}
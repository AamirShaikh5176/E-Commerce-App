package com.example.e_commerceapp.ui.screens.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.ui.screens.common.QuantityButton
import java.util.Locale

@Composable
fun CartItem(
    userCartEntity: UserCartEntity,
    onCartItemClicked: (UserCartEntity) -> Unit = {},
    onDeleteCartClicked: (UserCartEntity) -> Unit = {},
    onIncrementClicked: () -> Unit = {},
    onDecrementClicked: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable { onCartItemClicked(userCartEntity) },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min)
        ) {

            // Image
            AsyncImage(
                model = userCartEntity.image,
                contentDescription = stringResource(id = R.string.product_image_content),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product Info (Title + Desc + Price)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = userCartEntity.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = userCartEntity.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = String.format(
                        Locale.ENGLISH,
                        "%.2f EGP",
                        userCartEntity.price * userCartEntity.quantity
                    ),
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions (RIGHT EDGE)
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Image(
                    painter = painterResource(id = R.drawable.trash),
                    contentDescription = "delete cart",
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onDeleteCartClicked(userCartEntity) },
                )


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuantityButton(
                        icon = Icons.Default.Remove,
                        sizeBox = 22,
                        sizeIcon = 14,
                        onClick = onDecrementClicked
                    )

                    Text(
                        text = userCartEntity.quantity.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontWeight = FontWeight.Bold
                    )

                    QuantityButton(
                        icon = Icons.Default.Add,
                        sizeBox = 22,
                        sizeIcon = 14,
                        onClick = onIncrementClicked
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CartItemPreview() {
    CartItem(
        userCartEntity = UserCartEntity(
            productId = 1,
            title = "Essence Mascara Lash Princess",
            description = "The Essence Mascara Lash Princess is a popular mascara known for its volumizing and lengthening effects. Achieve dramatic lashes with this long-lasting and cruelty-free formula.",
            price = 9.99,
            rating = "5",
            image = "",
            quantity = 1,
            availableQuantity = 1
        )
    )

}
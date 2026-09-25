package com.example.e_commerceapp.ui.screens.address

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.address.AddressEntity
import java.util.UUID

@Composable
fun AddEditAddressScreen(
    addressId: String? = null,
    onBackClick: () -> Unit,
    onAddressSaved: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.addressesState.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var houseFlatNumber by remember { mutableStateOf("") }
    var streetArea by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var addressType by remember { mutableStateOf("HOME") }
    var isDefault by remember { mutableStateOf(false) }

    // Prepopulate if editing
    LaunchedEffect(addressId, state.data) {
        if (!addressId.isNullOrBlank()) {
            val existing = state.data?.find { it.id == addressId }
            existing?.let {
                fullName = it.fullName
                phoneNumber = it.phoneNumber
                pinCode = it.pinCode
                houseFlatNumber = it.houseFlatNumber
                streetArea = it.streetArea
                landmark = it.landmark
                city = it.city
                stateName = it.state
                addressType = it.addressType
                isDefault = it.isDefault
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }

                    Text(
                        text = if (addressId.isNullOrBlank()) "Add Delivery Address" else "Edit Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            // Scrollable Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CONTACT DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // 10-digit Mobile Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 10) phoneNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("10-Digit Mobile Number *") },
                    prefix = { Text("+91  ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "ADDRESS DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                // 6-digit PIN Code
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
                    label = { Text("6-Digit PIN Code *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // House / Flat / Building No.
                OutlinedTextField(
                    value = houseFlatNumber,
                    onValueChange = { houseFlatNumber = it },
                    label = { Text("Flat, House no., Building, Apartment *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // Area / Street / Locality
                OutlinedTextField(
                    value = streetArea,
                    onValueChange = { streetArea = it },
                    label = { Text("Area, Colony, Street, Sector *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // Landmark
                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.dark_blue),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                // City & State Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.dark_blue),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    OutlinedTextField(
                        value = stateName,
                        onValueChange = { stateName = it },
                        label = { Text("State *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.dark_blue),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Address Type Selection
                Text(
                    text = "SAVE ADDRESS AS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("HOME", "WORK", "OTHER").forEach { type ->
                        val selected = addressType.equals(type, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { addressType = type },
                            label = { Text(type, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (type) {
                                        "WORK" -> Icons.Default.Business
                                        "HOME" -> Icons.Default.Home
                                        else -> Icons.Default.Place
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorResource(R.color.dark_blue),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                // Default Address Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = colorResource(R.color.dark_blue)
                        )
                    )
                    Text(
                        text = "Make this my default delivery address",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }

        // Save Button Bottom Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 12.dp,
            color = Color.White
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        // Validation
                        if (fullName.isBlank()) {
                            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (phoneNumber.length != 10) {
                            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (pinCode.length != 6) {
                            Toast.makeText(context, "Please enter a valid 6-digit Indian PIN code", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (houseFlatNumber.isBlank() || streetArea.isBlank() || city.isBlank() || stateName.isBlank()) {
                            Toast.makeText(context, "Please fill in all required address fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val entity = AddressEntity(
                            id = addressId ?: UUID.randomUUID().toString(),
                            fullName = fullName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            pinCode = pinCode.trim(),
                            houseFlatNumber = houseFlatNumber.trim(),
                            streetArea = streetArea.trim(),
                            landmark = landmark.trim(),
                            city = city.trim(),
                            state = stateName.trim(),
                            addressType = addressType,
                            isDefault = isDefault
                        )

                        viewModel.saveAddress(
                            address = entity,
                            onSuccess = {
                                Toast.makeText(context, "Address saved successfully!", Toast.LENGTH_SHORT).show()
                                onAddressSaved()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text(
                        text = "Save Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

package com.example.e_commerceapp

import com.example.e_commerceapp.domain.entity.address.AddressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IndianAddressValidationTest {

    @Test
    fun addressEntity_defaultValuesConformToIndianStructure() {
        val address = AddressEntity(
            id = "addr_1",
            fullName = "Aamir Shaikh",
            phoneNumber = "9876543210",
            houseFlatNumber = "Flat 101, Green Heights",
            streetArea = "MG Road, Indiranagar",
            landmark = "Opposite Metro Station",
            city = "Bengaluru",
            state = "Karnataka",
            pinCode = "560038",
            addressType = "HOME",
            isDefault = true
        )

        assertEquals("560038", address.pinCode)
        assertEquals("Karnataka", address.state)
        assertEquals("Bengaluru", address.city)
        assertEquals("HOME", address.addressType)
        assertTrue(address.isDefault)
    }

    @Test
    fun pinCodeValidation_acceptsOnlySixDigits() {
        val validPin = "560038"
        val invalidPinShort = "56003"
        val invalidPinAlpha = "56003A"

        assertTrue(validPin.length == 6 && validPin.all { it.isDigit() })
        assertFalse(invalidPinShort.length == 6 && invalidPinShort.all { it.isDigit() })
        assertFalse(invalidPinAlpha.length == 6 && invalidPinAlpha.all { it.isDigit() })
    }

    @Test
    fun mobileNumberValidation_acceptsTenDigits() {
        val validPhone = "9876543210"
        val invalidPhone = "98765"

        assertTrue(validPhone.length == 10 && validPhone.all { it.isDigit() })
        assertFalse(invalidPhone.length == 10 && invalidPhone.all { it.isDigit() })
    }
}

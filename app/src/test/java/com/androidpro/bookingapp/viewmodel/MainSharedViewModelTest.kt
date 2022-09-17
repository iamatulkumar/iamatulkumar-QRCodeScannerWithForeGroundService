package com.androidpro.bookingapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.androidpro.bookingapp.repository.BookingApiRepository
import com.androidpro.bookingapp.repository.BookingModel
import com.androidpro.bookingapp.repository.BookingRepository
import com.androidpro.bookingapp.repository.BookingStatus
import com.androidpro.bookingapp.util.TimerUtil
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONTokener
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class MainSharedViewModelTest{

    @MockK
    private lateinit var bookingRepository: BookingRepository

    @MockK
    private lateinit var bookingApiRepository: BookingApiRepository

    private lateinit var viewModel: MainSharedViewmodel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this);
        viewModel = MainSharedViewmodel(bookingRepository, bookingApiRepository, testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `should set view model state ScanQrCodeSuccess, when successfully parse response`() {
        val response = "{\"location_id\":\"ButterKnifeLib-1234\",\"location_details\":\"ButterKnife Lib, 80 Feet Rd, Koramangala 1A Block, Bangalore\",\"price_per_min\":5.50}"
        val currentTimeInMillis = 1663400642201L
        val bookingModel = BookingModel(
            locationId = "ButterKnifeLib-1234",
            locationDetails = "ButterKnife Lib, 80 Feet Rd, Koramangala 1A Block, Bangalore",
            pricePerMin = "5.50",
            startTime = currentTimeInMillis,
            status = BookingStatus.ACTIVE
        )

        mockkConstructor(JSONTokener::class)
        mockkConstructor(TimerUtil::class)

        every { TimerUtil.getCurrentTimeInMillis() } returns currentTimeInMillis
        every { anyConstructed<JSONTokener>().nextValue().toString() } returns response

        viewModel.scanQRCodeResponse(response)

        val actual = viewModel.bookingDetail.value
        val expected = ScanQrCodeSuccess(bookingModel)

        assertEquals(actual, expected)
    }

    @Test
    fun `should set view model state ScanQrCodeSuccess with status inactive, when successfully parse response`() {
        val response = "{\"location_id\":\"ButterKnifeLib-1234\",\"location_details\":\"ButterKnife Lib, 80 Feet Rd, Koramangala 1A Block, Bangalore\",\"price_per_min\":5.50}"
        val currentTimeInMillis = 1663400642201L
        val bookingModel = BookingModel(
            locationId = "ButterKnifeLib-1234",
            locationDetails = "ButterKnife Lib, 80 Feet Rd, Koramangala 1A Block, Bangalore",
            pricePerMin = "5.50",
            startTime = currentTimeInMillis,
            status = BookingStatus.INACTIVE
        )

        mockkConstructor(JSONTokener::class)
        mockkConstructor(TimerUtil::class)

        every { TimerUtil.getCurrentTimeInMillis() } returns currentTimeInMillis
        every { anyConstructed<JSONTokener>().nextValue().toString() } returns response

        viewModel.isBookingExist = true
        viewModel.scanQRCodeResponse(response)

        val actual = viewModel.bookingDetail.value
        val expected = ScanQrCodeSuccess(bookingModel)

        assertEquals(actual, expected)
    }
}
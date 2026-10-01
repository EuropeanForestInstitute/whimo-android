/*
 * Copyright (c) 2025 EFI (https://efi.int/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whimo.data.transactions.model.mappers

import com.whimo.data.commodity.model.response.Commodity
import com.whimo.data.harvestseasons.model.response.HarvestSeasonData
import com.whimo.data.transactions.model.response.TransactionData
import com.whimo.extensions.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class TransactionsMapperTest {

    @Test
    fun `toDomain maps updated_at to updatedDate`() {
        val updatedAt = "2025-02-03T12:30:45Z"

        val result = transactionData(updatedAt = updatedAt).toDomain()

        assertEquals(updatedAt.toLocalDateTime(), result.updatedDate)
    }

    @Test
    fun `toDomain falls back to createdDate when updated_at is missing`() {
        val createdAt = "2025-02-03T10:00:00Z"

        val result = transactionData(createdAt = createdAt, updatedAt = null).toDomain()

        assertEquals(result.createdDate, result.updatedDate)
    }

    @Test
    fun `toDomain maps harvest season when response contains season object`() {
        val result = transactionData(
            harvestSeason = HarvestSeasonData(
                id = "season-id",
                name = "Cocoa 2025/26",
                start_date = "2025-09-01",
                end_date = "2026-09-01",
                status = "active",
            )
        ).toDomain()

        assertNotNull(result.harvestSeason)
        assertEquals("season-id", result.harvestSeason?.id)
        assertEquals("Cocoa 2025/26", result.harvestSeason?.name)
        assertEquals(LocalDate.of(2025, 9, 1), result.harvestSeason?.startDate)
        assertEquals(LocalDate.of(2026, 9, 1), result.harvestSeason?.endDate)
    }

    @Test
    fun `toDomain prefers transaction coordinates over legacy and farm coordinates`() {
        val result = transactionData(
            latitude = 1.0,
            longitude = 2.0,
            transactionLatitude = 3.0,
            transactionLongitude = 4.0,
            farmLatitude = 5.0,
            farmLongitude = 6.0,
        ).toDomain()

        assertEquals(3.0, result.location?.latitude)
        assertEquals(4.0, result.location?.longitude)
    }

    private fun transactionData(
        createdAt: String = "2025-02-03T10:00:00Z",
        updatedAt: String? = "2025-02-03T12:30:45Z",
        latitude: Double? = null,
        longitude: Double? = null,
        transactionLatitude: Double? = null,
        transactionLongitude: Double? = null,
        farmLatitude: Double? = null,
        farmLongitude: Double? = null,
        harvestSeason: HarvestSeasonData? = null,
    ) = TransactionData(
        id = "transaction-id",
        created_at = createdAt,
        updated_at = updatedAt,
        expires_at = null,
        type = "downstream",
        status = "accepted",
        action = "buying",
        location = "gps",
        latitude = latitude,
        longitude = longitude,
        transaction_latitude = transactionLatitude,
        transaction_longitude = transactionLongitude,
        farm_latitude = farmLatitude,
        farm_longitude = farmLongitude,
        commodity = Commodity(
            id = "commodity-id",
            code = "COCOA",
            name = "Cocoa",
            unit = "kg",
            has_recipe = false,
            group = null,
            balance = null,
        ),
        volume = 10f,
        traceability = "full",
        seller = null,
        buyer = null,
        is_buying_from_farmer = false,
        is_automatic = false,
        harvest_season = harvestSeason,
        created_by_id = "creator-id",
    )
}

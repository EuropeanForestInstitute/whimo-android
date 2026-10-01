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
package com.whimo.di

import com.whimo.data.harvestseasons.repository.HarvestSeasonsRepository
import com.whimo.data.harvestseasons.repository.HarvestSeasonsRepositoryImpl
import com.whimo.data.harvestseasons.service.HarvestSeasonsService
import com.whimo.data.AppDatabase
import com.whimo.data.harvestseasons.repository.HarvestSeasonCommodityLinker
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractor
import com.whimo.domain.harvestseasons.HarvestSeasonsInteractorImpl
import org.koin.dsl.module
import retrofit2.Retrofit

val harvestSeasonsModule = module {
    // Services
    single { get<Retrofit>(AUTHORISED).create(HarvestSeasonsService::class.java) }

    // DB
    single { get<AppDatabase>().harvestSeasonsDao() }

    // Repositories
    single {
        HarvestSeasonCommodityLinker(
            harvestSeasonsDao = get(),
            balancesDao = get(),
        )
    }
    factory<HarvestSeasonsRepository> {
        HarvestSeasonsRepositoryImpl(
            service = get(),
            dao = get(),
            harvestSeasonCommodityLinker = get(),
        )
    }

    // Interactors
    factory<HarvestSeasonsInteractor> { HarvestSeasonsInteractorImpl(repository = get()) }
}

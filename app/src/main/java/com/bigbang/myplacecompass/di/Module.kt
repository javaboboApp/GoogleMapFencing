package com.bridge.androidtechnicaltest.di


import com.bigbang.myplacecompass.model.IPlacesRepository
import com.bigbang.myplacecompass.model.PlacesRepository
import com.bigbang.myplacecompass.network.IPlacesService
import com.bigbang.myplacecompass.network.PlacesRetrofitInstance
import com.bigbang.myplacecompass.viewmodel.CompassViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val networkModule = module {
    factory { PlacesRetrofitInstance.createPlacesService() }
}

val repositoriesModule = module {
       single<IPlacesRepository>{ PlacesRepository(get()) }
}

val viewModelModule = module {
    viewModel { CompassViewModel(get()) }
}

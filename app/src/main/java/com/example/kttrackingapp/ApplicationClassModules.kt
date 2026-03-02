package com.example.kttrackingapp

import android.content.Context
import androidx.room.Room
import androidx.work.WorkerParameters
import com.example.kttrackingapp.roomDB.AppDatabase
import com.example.kttrackingapp.roomDB.roomRepo.AllUserRepo
import com.example.kttrackingapp.roomDB.roomRepo.UserDetailRepo
import com.example.kttrackingapp.roomDB.roomVM.AllUserVM
import com.example.kttrackingapp.roomDB.roomVM.UserDetailVM
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val databasemodule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            AppDatabase::class.java,
            "kt_tracking_app"
        ).fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().allUserDao() }
    single { get<AppDatabase>().userDetailDao() }
}

val dbRepository = module {
    single { AllUserRepo(get()) }
    single { UserDetailRepo(get()) }
}

val dbViewModel = module {
    single { AllUserVM(get()) }
    viewModel { UserDetailVM(get(), androidApplication()) } //  androidApplication()
}

val workerModule = module {
    worker { LocationWorker(get(), get(), get()) } //  context, params, dao
}


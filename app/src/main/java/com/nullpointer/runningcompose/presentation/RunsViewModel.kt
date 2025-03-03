package com.nullpointer.runningcompose.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.nullpointer.runningcompose.R
import com.nullpointer.runningcompose.core.states.Resource
import com.nullpointer.runningcompose.core.utils.Utility
import com.nullpointer.runningcompose.core.utils.launchSafeIO
import com.nullpointer.runningcompose.domain.config.ConfigRepository
import com.nullpointer.runningcompose.domain.location.TrackingRepository
import com.nullpointer.runningcompose.domain.runs.RunRepository
import com.nullpointer.runningcompose.models.Run
import com.nullpointer.runningcompose.models.types.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class RunsViewModel @Inject constructor(
    private val runsRepository: RunRepository,
    private val configRepository: ConfigRepository,
    locationRepository: TrackingRepository,
) : ViewModel() {

    init {
        Timber.e("Init time running")
    }

    private val _messageRuns = Channel<Int>()
    val messageRuns = _messageRuns.receiveAsFlow()

    val stateTracking = locationRepository
        .stateTracking
        .flowOn(Dispatchers.IO).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TrackingState.WAITING
        )

    val listRunsOrdered = flow<Resource<List<Run>>> {
        runsRepository.listRunsOrdered.collect {
            emit(Resource.Success(it))

        }
    }.catch {
        Timber.e("Error when load run $it")
        _messageRuns.trySend(R.string.error_load_runs)
        emit(Resource.Failure)
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Resource.Loading
    )


    fun insertNewRun(
        timeRun: Long,
        listPoints: List<List<LatLng>>,
        bitmap: Bitmap?
    ) = launchSafeIO {
        // * need weight user for calculate calories burned

        // ? Se estima que el costo energético de cada kilómetro que corres,
        // ? es de 1 kcal (1000 calorías) por cada kilogramo de peso corporal del corredo

        val weightUser = configRepository.userConfig.first()!!.weight
        val mapConfig = configRepository.mapConfig.first()
        val currentTime = System.currentTimeMillis()

        var distanceInMeters = 0f
        val listPolylineEncode = listPoints.asSequence().filter {
            it.size >= 2
        }.onEach {
            distanceInMeters += Utility.calculatePolylineLength(it)
        }.map {
            PolyUtil.encode(it)
        }.toList()
        val avgSpeedInMS = distanceInMeters / (timeRun / 1000f)
        val caloriesBurned = distanceInMeters * (weightUser / 1000f)

        val run = Run(
            avgSpeedInMeters = avgSpeedInMS,
            distanceInMeters = distanceInMeters,
            timeRunInMillis = timeRun,
            caloriesBurned = caloriesBurned,
            listPolyLineEncode = listPolylineEncode,
            mapConfig = mapConfig,
            timestamp = currentTime,
        )
        runsRepository.insertNewRun(run,bitmap)
    }

    fun deleterRun(run: Run) = launchSafeIO {
        runsRepository.deleterRun(run)
    }

    fun deleterListRun(listIds: List<Long>) = launchSafeIO {
        runsRepository.deleterListRuns(listIds)
    }
}

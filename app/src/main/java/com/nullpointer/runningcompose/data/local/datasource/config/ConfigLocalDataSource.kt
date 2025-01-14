package com.nullpointer.runningcompose.data.local.datasource.config

import androidx.compose.ui.graphics.Color
import com.nullpointer.runningcompose.models.config.MapConfig
import com.nullpointer.runningcompose.models.config.SortConfig
import com.nullpointer.runningcompose.models.config.UserConfig
import com.nullpointer.runningcompose.models.types.MapStyle
import com.nullpointer.runningcompose.models.types.MetricType
import com.nullpointer.runningcompose.models.types.SortType
import kotlinx.coroutines.flow.Flow

interface ConfigLocalDataSource {
    val userConfig: Flow<UserConfig?>
    val mapConfig: Flow<MapConfig>
    val sortConfig: Flow<SortConfig>
    val metricsConfig:Flow<MetricType>
    val isFirstPermissionLocation: Flow<Boolean>

    suspend fun changeUserConfig(userConfig: UserConfig)
    suspend fun changeMapConfig(style: MapStyle?, weight: Int?, color: Color?)
    suspend fun changeSortConfig(sortType: SortType?, isReverse: Boolean?)
    suspend fun changeMetricsConfig(metricType: MetricType)
    suspend fun changeIsFirstPermissionLocation()
}
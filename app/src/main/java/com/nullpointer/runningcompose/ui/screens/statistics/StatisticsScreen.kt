package com.nullpointer.runningcompose.ui.screens.statistics

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nullpointer.runningcompose.R
import com.nullpointer.runningcompose.core.states.Resource
import com.nullpointer.runningcompose.models.Run
import com.nullpointer.runningcompose.models.StatisticsRun
import com.nullpointer.runningcompose.models.types.MetricType
import com.nullpointer.runningcompose.presentation.ConfigViewModel
import com.nullpointer.runningcompose.presentation.StatisticsViewModel
import com.nullpointer.runningcompose.ui.navigation.HomeNavGraph
import com.nullpointer.runningcompose.ui.screens.empty.EmptyScreen
import com.nullpointer.runningcompose.ui.screens.statistics.componets.GraphRuns
import com.nullpointer.runningcompose.ui.screens.statistics.componets.LoadingStatistics
import com.nullpointer.runningcompose.ui.screens.statistics.componets.StatisticsRuns
import com.nullpointer.runningcompose.ui.states.OrientationScreenState
import com.nullpointer.runningcompose.ui.states.rememberOrientationScreenState
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@HomeNavGraph
@Destination
fun StatisticsScreen(
    configViewModel: ConfigViewModel,
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
    statisticsState: OrientationScreenState = rememberOrientationScreenState()
) {
    val fullStatistics by statisticsViewModel.fullStatistics.collectAsState()
    val metricType by configViewModel.metrics.collectAsState()

    LaunchedEffect(key1 = Unit) {
        statisticsViewModel.messageStatistics.collect(statisticsState::showSnackMessage)
    }

    Scaffold(
        scaffoldState = statisticsState.scaffoldState
    ) {
        when (val fullStatistics = fullStatistics) {
            Resource.Failure -> {
                EmptyScreen(
                    animation = R.raw.empty2,
                    textEmpty = stringResource(R.string.message_empty_statisctis),
                    modifier = Modifier.padding(it)
                )
            }
            Resource.Loading -> {
                LoadingStatistics(modifier = Modifier.padding(it))
            }
            is Resource.Success -> {
                val (listRuns, statistics) = fullStatistics.data
                if (listRuns.isEmpty()) {
                    EmptyScreen(
                        animation = R.raw.empty2,
                        textEmpty = stringResource(R.string.message_empty_statisctis),
                        modifier = Modifier.padding(it)
                    )
                } else {
                    StatisticsAndGraph(
                        orientation = statisticsState.orientation,
                        listRuns = listRuns.reversed(),
                        statistics = statistics,
                        metricType = metricType,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticsAndGraph(
    orientation: Int,
    listRuns: List<Run>,
    statistics: StatisticsRun,
    metricType: MetricType,
    modifier: Modifier = Modifier
) {

    when (orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            Column(modifier = modifier) {
                StatisticsRuns(
                    statisticsRun = statistics,
                    metricType = metricType,
                    modifier = Modifier
                        .weight(2F)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                GraphRuns(
                    list = listRuns,
                    metricType = metricType,
                    modifier = Modifier
                        .weight(6F)
                        .fillMaxWidth()
                )
            }
        }
        else -> {
            Row(modifier = modifier) {
                StatisticsRuns(
                    statisticsRun = statistics,
                    metricType = metricType,
                    modifier = Modifier
                        .weight(.5F)
                        .fillMaxHeight()
                )
                GraphRuns(
                    list = listRuns,
                    metricType = metricType,
                    modifier = Modifier
                        .weight(.5f)
                        .fillMaxHeight()
                    )
                }
            }
        }
}





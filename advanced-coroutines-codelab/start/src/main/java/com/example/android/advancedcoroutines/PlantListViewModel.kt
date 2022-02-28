/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.advancedcoroutines

import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlantListViewModel internal constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _snackbar = MutableLiveData<String?>()

    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)

    val spinner: LiveData<Boolean>
        get() = _spinner

    private val growZone = MutableLiveData<GrowZone>(NoGrowZone)

    private val growZoneFlow = MutableStateFlow<GrowZone>(NoGrowZone)

    val plants: LiveData<List<Plant>> = growZone.switchMap { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plants
        } else {
            plantRepository.getPlantsWithGrowZone(growZone)
        }
    }

    val plantsUsingFlow: LiveData<List<Plant>> = growZoneFlow.flatMapLatest { growZone ->
        if (growZone == NoGrowZone) {
            plantRepository.plantsFlow
        } else {
            plantRepository.getPlantsWithGrowZoneFlow(growZone)
        }
    }.asLiveData()

    init {
        clearGrowZoneNumber()

        growZoneFlow.mapLatest { growZone ->
            _spinner.value = true
            if (growZone == NoGrowZone) {
                plantRepository.tryUpdateRecentPlantsCache()
            } else {
                plantRepository.tryUpdateRecentPlantsForGrowZoneCache(growZone)
            }
        }
            .onEach { _spinner.value = false }
            .catch { throwable -> _snackbar.value = throwable.message }
            .launchIn(viewModelScope)
    }

    fun setGrowZoneNumber(num: Int) {
        growZone.value = GrowZone(num)
        growZoneFlow.value = GrowZone(num)
    }

    fun clearGrowZoneNumber() {
        growZone.value = NoGrowZone
        growZoneFlow.value = NoGrowZone
    }


    fun isFiltered() = growZone.value != NoGrowZone

    fun onSnackbarShown() {
        _snackbar.value = null
    }


    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }
}

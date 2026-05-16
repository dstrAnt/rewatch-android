package it.polimi.rewatch.utility

import it.polimi.rewatch.common.PersistRepository
import it.polimi.rewatch.components.BottomSheetViewModel
import it.polimi.rewatch.model.MediaId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.StandardTestDispatcher

class FakeBottomSheetViewModel (
    private val id: MediaId,
    val persistRepo: PersistRepository,
    val viewModelScope: CoroutineScope =
        CoroutineScope(StandardTestDispatcher()),
){
    val watchedItem = persistRepo.watchedFlow
        .map { result -> result.getOrNull()?.find { it.id == id } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Keeps it alive briefly during config changes
            initialValue = null, // The starting value while it searches
        )
    val watchlistItem = persistRepo.watchlistFlow
        .map { result ->
            val foundItem = result.getOrNull()?.find { it.id == id }
            // 2. Check if our equality logic is working
            println("DEBUG_FLOWSHEET: Searching for $id. Found: ${foundItem?.id}")
            foundItem
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null, // The starting value while it searches
        )
}
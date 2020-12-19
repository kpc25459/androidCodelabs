package net.dev.roomwordsample

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.dev.roomwordsample.data.Word
import net.dev.roomwordsample.data.WordRepository

class WordViewModel(private val repository: WordRepository) : ViewModel() {
    val allWords: LiveData<List<Word>> = repository.allWords.asLiveData()

    fun insert(word: Word) = viewModelScope.launch {
        repository.insert(word)
    }
}

class WordViewModelFactory(private val repository: WordRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(WordViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return WordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
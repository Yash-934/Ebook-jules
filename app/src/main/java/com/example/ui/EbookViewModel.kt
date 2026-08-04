package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Book
import com.example.data.BookRepository
import com.example.ui.screens.DrawnLine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class EbookViewModel(private val repository: BookRepository) : ViewModel() {
    val allBooks: StateFlow<List<Book>> = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addBook(book: Book) {
        viewModelScope.launch {
            repository.insert(book)
        }
    }

    fun deleteBook(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun updateBookCover(bookId: Int, coverUri: String) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                repository.update(book.copy(coverUri = coverUri))
            }
        }
    }

    fun updateProgress(bookId: Int, progress: Float) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                repository.update(book.copy(progress = progress))
            }
        }
    }

    fun toggleFavorite(bookId: Int) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                repository.update(book.copy(isFavorite = !book.isFavorite))
            }
        }
    }

    fun addBookmark(bookId: Int, position: Int, name: String = "Bookmark") {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                val array = try { JSONArray(book.bookmarks) } catch(e: Exception) { JSONArray() }
                val obj = JSONObject()
                obj.put("position", position)
                obj.put("name", name)
                array.put(obj)
                repository.update(book.copy(bookmarks = array.toString()))
            }
        }
    }

    fun removeBookmark(bookId: Int, index: Int) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                val array = try { JSONArray(book.bookmarks) } catch(e: Exception) { JSONArray() }
                if (index >= 0 && index < array.length()) {
                    array.remove(index)
                    repository.update(book.copy(bookmarks = array.toString()))
                }
            }
        }
    }

    fun updateAnnotations(bookId: Int, lines: List<DrawnLine>) {
        viewModelScope.launch {
            val book = repository.getBookById(bookId)
            if (book != null) {
                val jsonArray = JSONArray()
                for (line in lines) {
                    val jsonObj = JSONObject()
                    jsonObj.put("color", line.color.value.toLong())
                    jsonObj.put("strokeWidth", line.strokeWidth.toDouble())
                    jsonObj.put("alpha", line.alpha.toDouble())
                    jsonObj.put("tool", line.tool)

                    val pointsArray = JSONArray()
                    for (point in line.points) {
                        val pointObj = JSONObject()
                        pointObj.put("x", point.x.toDouble())
                        pointObj.put("y", point.y.toDouble())
                        pointsArray.put(pointObj)
                    }
                    jsonObj.put("points", pointsArray)
                    jsonArray.put(jsonObj)
                }
                repository.update(book.copy(annotations = jsonArray.toString()))
            }
        }
    }
}

class EbookViewModelFactory(private val repository: BookRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EbookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EbookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

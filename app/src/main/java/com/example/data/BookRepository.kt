package com.example.data

import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    suspend fun getBookById(id: Int): Book? = bookDao.getBookById(id)

    suspend fun insert(book: Book) = bookDao.insertBook(book)

    suspend fun update(book: Book) = bookDao.updateBook(book)

    suspend fun deleteById(id: Int) = bookDao.deleteBookById(id)
}

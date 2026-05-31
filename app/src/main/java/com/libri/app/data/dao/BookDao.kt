package com.libri.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.libri.app.data.entity.AuthorEntity
import com.libri.app.data.entity.BookAuthorCrossRef
import com.libri.app.data.entity.BookEntity
import com.libri.app.data.entity.BookWithAuthors
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Transaction
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooksWithAuthors(): Flow<List<BookWithAuthors>>

    @Transaction
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchBooks(query: String): Flow<List<BookWithAuthors>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookWithAuthors(id: Long): BookWithAuthors?

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBook(id: Long): BookEntity?

    @Insert
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Insert
    suspend fun insertAuthor(author: AuthorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAuthor(author: AuthorEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookAuthorCrossRef(crossRef: BookAuthorCrossRef)

    @Query("SELECT * FROM authors WHERE firstName = :firstName AND lastName = :lastName LIMIT 1")
    suspend fun findAuthorByName(firstName: String, lastName: String): AuthorEntity?

    @Query("DELETE FROM book_author_cross_ref WHERE bookId = :bookId")
    suspend fun deleteBookAuthorCrossRefs(bookId: Long)
}

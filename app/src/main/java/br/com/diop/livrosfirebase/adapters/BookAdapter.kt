package br.com.diop.livrosfirebase.adapters

import android.view.View
import android.view.ViewGroup

import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query

import br.com.diop.livrosfirebase.domain.Book
import br.com.diop.livrosfirebase.R


class BookAdapter(ref: Query, private val mBookClickListener: BookClickListener?) : FirebaseRecyclerAdapter<Book, BookHolder>(Book::class.java, R.layout.item_book, BookHolder::class.java, ref) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        val bookHolder = super.onCreateViewHolder(parent, viewType)

        bookHolder.mBinding.root.setOnClickListener {
            if (mBookClickListener != null) {
                val position = bookHolder.adapterPosition
                val book = getItem(position)
                book.id = getRef(position).key
                mBookClickListener.onBookClicked(book)
            }
        }

        return bookHolder
    }

    override fun populateViewHolder(bookHolder: BookHolder, book: Book, position: Int) {
        bookHolder.setBook(book)
    }

    interface BookClickListener {
        fun onBookClicked(book: Book)
    }
}

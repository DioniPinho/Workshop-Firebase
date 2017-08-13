package br.com.diop.livrosfirebase.adapters

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.View

import br.com.diop.livrosfirebase.domain.Book
import br.com.diop.livrosfirebase.databinding.ItemBookBinding

class BookHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal var mBinding: ItemBookBinding

    init {
        mBinding = DataBindingUtil.bind<ItemBookBinding>(view)
    }

    fun setBook(book: Book) {
        mBinding.book = book
    }

}
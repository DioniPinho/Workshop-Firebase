package br.com.diop.livrosfirebase.utils

import android.databinding.BindingAdapter
import android.widget.ImageView

import com.bumptech.glide.Glide

object ImageBindind {

    @BindingAdapter("android:src")
    fun loadImage(imageView: ImageView, url: String) {
        Glide.with(imageView.context).load(url).into(imageView)
    }
}

package com.example.masterdetailapirest
import java.util.ArrayList

object Posts {
    val lista: MutableList<Post> = ArrayList()

    init {

    }


    data class Post(val titulo: String, val descripcion: String) {
        override fun toString(): String {
            return "$titulo"
        }
    }
}
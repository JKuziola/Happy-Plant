package com.example.happyplant.model

data class Task(
    val title: String,
    var hour: String,
    var date: String,
    val id: Int = 0,
    val cycle: String = "7", // In days.

    ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id != other.id) return false

        return true

    }

    override fun hashCode(): Int {
        return id
    }
}

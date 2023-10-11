package com.example.kingoftokyo.Entity

sealed class UIEvent {
    data class ShowDialog(val title: String, val message: String, val onDismiss: (() -> Unit)? = null) : UIEvent()
}

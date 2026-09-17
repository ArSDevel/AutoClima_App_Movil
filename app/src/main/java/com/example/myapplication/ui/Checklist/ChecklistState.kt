package com.example.myapplication.ui.checklist

sealed class ChecklistState {
    object Idle : ChecklistState()
    object Loading : ChecklistState()
    data class Success(val checklistId: Long) : ChecklistState()
    data class Error(val mensaje: String) : ChecklistState()
}
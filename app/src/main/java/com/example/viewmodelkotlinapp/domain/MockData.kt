package com.example.viewmodelkotlinapp.domain

// Mock data source 
// In a real app, this would be a Repository that fetches from a database or network
object TaskDataSource {

    fun getInitialTasks(): List<Task> = listOf(
        Task(1, "ostoslista", "lista", 1, "12-12-2026", true),
        Task(2, "Työlista", "lista", 2, "30-12-2026", false),
        Task(3, "kotitehtävät", "tehtävä", 3, "14-12-2026", false),
        Task(4, "tiskaus", "tehtävä", 4, "12-12-2026", false),
        Task(5, "öljynvaihto", "huolto", 5, "12-12-2027", false),
        Task(6, "keittiön uusinta", "remontti", 6, "12-12-2028", false),
    )
}
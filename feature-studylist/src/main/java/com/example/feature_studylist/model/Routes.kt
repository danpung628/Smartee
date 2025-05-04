package com.example.feature_studylist.model

sealed class Routes(val route:String) {
    object List:Routes(route = "List")
    object Search:Routes(route = "Search")
    object Detail:Routes(route = "Detail")
}
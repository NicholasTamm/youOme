package com.example.youome.home

sealed class HomeRecyclerViewItems {
    data class GroupItem(val group: com.example.youome.data.model.GroupUiModel) : HomeRecyclerViewItems()
    object CreateGroupItem : HomeRecyclerViewItems()
}

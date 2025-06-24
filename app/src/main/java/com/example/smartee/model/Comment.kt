package com.example.smartee.model

data class Comment(
    val commentId: String = "",
    val studyId: String = "",
    val userId: String = "",
    val userNickname: String = "",
    val userProfileUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "방금 전"
            diff < 3600000 -> "${diff / 60000}분 전"
            diff < 86400000 -> "${diff / 3600000}시간 전"
            else -> "${diff / 86400000}일 전"
        }
    }
}

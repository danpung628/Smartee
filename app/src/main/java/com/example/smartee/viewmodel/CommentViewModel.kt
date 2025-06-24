package com.example.smartee.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartee.model.Comment
import com.example.smartee.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentViewModel : ViewModel() {
    private val commentRepository = CommentRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadComments(studyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val commentList = commentRepository.getComments(studyId)
                _comments.value = commentList
            } catch (e: Exception) {
                _error.value = "댓글을 불러오는데 실패했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(studyId: String, content: String, userNickname: String, userId: String) {
        if (content.trim().isEmpty()) return

        viewModelScope.launch {
            val comment = Comment(
                studyId = studyId,
                userId = userId,
                userNickname = userNickname,
                content = content.trim()
            )

            val success = commentRepository.addComment(comment)
            if (success) {
                // 댓글 목록 새로고침
                loadComments(studyId)
            } else {
                _error.value = "댓글 작성에 실패했습니다"
            }
        }
    }

    fun deleteComment(commentId: String, studyId: String) {
        viewModelScope.launch {
            val success = commentRepository.deleteComment(commentId, studyId)
            if (success) {
                // 댓글 목록 새로고침
                loadComments(studyId)
            } else {
                _error.value = "댓글 삭제에 실패했습니다"
            }
        }
    }
}
package com.example.smartee.repository

import android.util.Log
import com.example.smartee.model.Comment
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class CommentRepository {
    private val firestore = Firebase.firestore

    suspend fun getComments(studyId: String): List<Comment> {
        return try {
            firestore.collection("comments")
                .whereEqualTo("studyId", studyId)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Comment::class.java)
        } catch (e: Exception) {
            Log.e("CommentRepository", "댓글 조회 실패", e)
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            val commentId = firestore.collection("comments").document().id
            val commentWithId = comment.copy(commentId = commentId)

            firestore.collection("comments")
                .document(commentId)
                .set(commentWithId)
                .await()

            // 스터디의 댓글 수 증가
            updateCommentCount(comment.studyId, 1)
            true
        } catch (e: Exception) {
            Log.e("CommentRepository", "댓글 추가 실패", e)
            false
        }
    }

    suspend fun deleteComment(commentId: String, studyId: String): Boolean {
        return try {
            firestore.collection("comments")
                .document(commentId)
                .update("isDeleted", true)
                .await()

            // 스터디의 댓글 수 감소
            updateCommentCount(studyId, -1)
            true
        } catch (e: Exception) {
            Log.e("CommentRepository", "댓글 삭제 실패", e)
            false
        }
    }

    private suspend fun updateCommentCount(studyId: String, increment: Int) {
        try {
            firestore.collection("studies")
                .document(studyId)
                .update("commentCount", FieldValue.increment(increment.toLong()))
                .await()
        } catch (e: Exception) {
            Log.e("CommentRepository", "댓글 수 업데이트 실패", e)
        }
    }
}
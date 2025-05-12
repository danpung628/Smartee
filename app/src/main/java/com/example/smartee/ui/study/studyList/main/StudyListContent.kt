package com.example.smartee.ui.study.studyList.main

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartee.model.StudyData

@Composable
fun StudyListContent(
    modifier: Modifier = Modifier,
    filteredStudyList: MutableList<StudyData>,
    onStudyDetailNavigate: (String) -> Unit
) {
    LazyColumn {
        items(filteredStudyList) {
            Log.d("StudyID", "studyId = ${it.studyId}")
            StudyListItem(
                item = it,
                onClick = onStudyDetailNavigate
            )
        }
    }
}

@Preview
@Composable
private fun StudyListPreview() {

}
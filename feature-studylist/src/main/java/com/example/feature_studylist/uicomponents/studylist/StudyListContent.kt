package com.example.feature_studylist.uicomponents.studylist

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.feature_studylist.model.StudyData

@Composable
fun StudyListContent(
    modifier: Modifier = Modifier,
    filteredStudyList: MutableList<StudyData>,
    onStudyDetailNavigate: (String) -> Unit
) {
    Column {
        filteredStudyList.forEach {//스터디 아이템
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
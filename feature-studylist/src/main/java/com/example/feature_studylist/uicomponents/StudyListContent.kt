package com.example.feature_studylist.uicomponents

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.feature_studylist.model.StudyData

@Composable
fun StudyList(
    modifier: Modifier = Modifier,
    studyList: MutableList<StudyData>
) {
    Column {
        studyList.forEach {//스터디 아이템
            Log.d("StudyID", "studyId = ${it.studyId}")
            StudyListItem(item = it)
        }
    }
}

@Preview
@Composable
private fun StudyListPreview() {

}
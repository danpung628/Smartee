package com.example.smartee.ui.study.studyList.main

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.smartee.model.StudyData
import com.example.smartee.viewmodel.StudyViewModel

@Composable
fun StudyListContent(
    modifier: Modifier = Modifier,
    studyViewModel: StudyViewModel,
    onStudyDetailNavigate: (String) -> Unit
) {
    val filteredStudyList = studyViewModel.filteredStudyList.observeAsState(initial = emptyList<StudyData>()).value
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
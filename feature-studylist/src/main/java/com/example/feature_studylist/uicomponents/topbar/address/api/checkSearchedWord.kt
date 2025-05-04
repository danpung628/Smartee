package com.example.feature_studylist.uicomponents.topbar.address.api

// 검색어 필터링 함수
fun checkSearchedWord(keyword: String): String {
    var filteredKeyword = keyword

    // 특수문자 제거
    val expText = Regex("[%=><]")
    filteredKeyword = filteredKeyword.replace(expText, "")

    // SQL 예약어 제거
    val sqlArray = arrayOf(
        "OR", "SELECT", "INSERT", "DELETE", "UPDATE", "CREATE", "DROP", "EXEC",
        "UNION", "FETCH", "DECLARE", "TRUNCATE"
    )

    for (sql in sqlArray) {
        val regex = Regex(sql, RegexOption.IGNORE_CASE)
        if (regex.containsMatchIn(filteredKeyword)) {
            filteredKeyword = filteredKeyword.replace(regex, "")
        }
    }

    return filteredKeyword
}
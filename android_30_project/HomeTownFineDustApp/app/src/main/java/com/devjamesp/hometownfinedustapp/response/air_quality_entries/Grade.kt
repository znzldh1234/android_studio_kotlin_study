package com.devjamesp.hometownfinedustapp.response.air_quality_entries

import androidx.annotation.ColorRes
import com.devjamesp.hometownfinedustapp.R
import com.google.gson.annotations.SerializedName

enum class Grade(
    val label: String,
    val emoji: String,
    @ColorRes val colorResId: Int,
) {
    @SerializedName("1")
    GOOD("좋음", "\uD83D\uDE04", R.color.blue),
    @SerializedName("2")
    NORMAL("보통", "\uD83D\uDE42", R.color.green),
    @SerializedName("3")
    BAD("나쁨", "\uD83D\uDE10", R.color.yellow),
    @SerializedName("4")
    AWFUL("매우 나쁨", "\uD83D\uDE2D", R.color.red),
    UNKNOWN("미측정", "\uD83D\uDD75", R.color.gray);

    override fun toString(): String {
        return "$label $emoji"
    }


}
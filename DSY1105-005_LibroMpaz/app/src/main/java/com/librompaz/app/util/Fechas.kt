package com.librompaz.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun hoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


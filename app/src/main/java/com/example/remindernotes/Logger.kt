package com.example.remindernotes

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Logger {

    private const val TAG = "NotesApp"
    private const val FILE_NAME = "notes_app.log"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private lateinit var logFile: File

    fun init(context: Context) {
        logFile = File(context.filesDir, FILE_NAME)
        i("Logger", "Logger initialized. Log file: ${logFile.absolutePath}")
        i("Logger", "App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        i("Logger", "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        i("Logger", "Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        i("Logger", "=".repeat(50))
    }

    fun v(tag: String, message: String) = write("VERBOSE", tag, message)
    fun d(tag: String, message: String) = write("DEBUG", tag, message)
    fun i(tag: String, message: String) = write("INFO", tag, message)
    fun w(tag: String, message: String) = write("WARN", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        write("ERROR", tag, message)
        throwable?.let { write("ERROR", tag, it.stackTraceToString()) }
    }

    private fun write(level: String, tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val line = "[$timestamp] [$level] [$tag] $message"

        // Print to Logcat as usual
        when (level) {
            "VERBOSE" -> Log.v(tag, message)
            "DEBUG"   -> Log.d(tag, message)
            "INFO"    -> Log.i(tag, message)
            "WARN"    -> Log.w(tag, message)
            "ERROR"   -> Log.e(tag, message)
        }

        try {
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(line)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log to file: ${e.message}")
        }
    }

    fun readLogs(): String {
        return try {
            if (logFile.exists()) logFile.readText() else "Log file is empty."
        } catch (e: Exception) {
            "Failed to read log file: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            if (logFile.exists()) logFile.delete()
            i("Logger", "Logs cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear logs: ${e.message}")
        }
    }

    fun getLogFile(): File = logFile
}
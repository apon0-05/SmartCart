package com.example.smartcard

import android.util.Log

private const val QR_FLOW_PHONE_TAG = "QR_FLOW_PHONE"

object QrFlowPhoneLog {
    fun d(event: String, vararg fields: Pair<String, Any?>) {
        Log.d(QR_FLOW_PHONE_TAG, format(event, fields))
    }

    fun e(event: String, throwable: Throwable? = null, vararg fields: Pair<String, Any?>) {
        if (throwable != null) {
            Log.e(QR_FLOW_PHONE_TAG, format(event, fields), throwable)
        } else {
            Log.e(QR_FLOW_PHONE_TAG, format(event, fields))
        }
    }

    private fun format(event: String, fields: Array<out Pair<String, Any?>>): String {
        val sb = StringBuilder()
        sb.append("event=").append(event)
        for ((k, v) in fields) {
            sb.append(' ')
            sb.append(k)
            sb.append('=')
            sb.append(v?.toString() ?: "null")
        }
        return sb.toString()
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nominees")
data class NomineeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // নাম, যেমন: "আব্দুর রহমান (বড় ভাই)"
    val relation: String, // সম্পর্ক, যেমন: "ভাই", "স্ত্রী", "সন্তান", "পিতা", "বিশ্বস্ত বন্ধু"
    val phone: String = "",
    val email: String = "",
    val accessScope: String = "ALL", // "ALL" (সম্পূর্ণ হিসাব), "PAYABLE_ONLY" (শুধু দেনা/ঋণ পরিশোধ), "RECEIVABLE_ONLY" (শুধু পাওনা আদায়), "DEBTS_AND_RECEIVABLES" (দেনা ও পাওনা)
    val canViewCash: Boolean = true, // ক্যাশ ও ব্যাংক ব্যালেন্স দেখতে পারবে কিনা
    val accessKey: String = "", // ৮ সংখ্যার সিকিউর এক্সেস কোড / ভেরিফিকেশন কি
    val note: String = "", // জরুরি বার্তা বা ওসিয়ত
    val createdAt: Long = System.currentTimeMillis()
)

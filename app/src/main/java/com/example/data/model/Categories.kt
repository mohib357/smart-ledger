package com.example.data.model

object Categories {
    val incomeCategories = listOf(
        "ব্যবসা আয়", "বেতন", "লেনদেন", "বকেয়া আদায়",
        "কমিশন", "টাকা ফেরত", "অন্যান্য"
    )

    val expenseCategories = listOf(
        "সাপ্লায়ার", "খাবার", "ভাড়া", "স্বাস্থ্য", "শিক্ষা",
        "বিনোদন", "যাতায়াত", "ঘাটতি", "দোকান খরচ",
        "দৈনন্দিন বাজার", "বকেয়া পরিশোধ", "অন্যান্য"
    )

    val unified = (incomeCategories + expenseCategories).distinct()
}

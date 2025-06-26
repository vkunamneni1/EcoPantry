---
title: "EcoPantry"
author: "veds"
description: "grocery expiration tracking, recipe suggestions, and a Raspberry Pi sensor suite for real-time fridge monitoring"
created_at: "2025-06-26"
---

# June 26th - Adding a Raspberry Pi to EcoPantry

[So I made EcoPantry in conjunction with my high school's EECS class, while learning how to use JavaFX, SQLite, and other APIs. During the ~1-2 months of development, the app is able to read receipts (w/ Tesseract OCR), suggest recipes (w/ TheMealDB API), store user data (SQLite), and display statistics on food waste/savings. However, when I found out about Highway, I wanted to add a more physical component to EcoPantry. While it did have stats that showed how much food a user wasted/saved, actual data from inside a fridge would be really helpful. It could even allow the expiry date estimation to be even more accurate. So, I decided to start working on developing a sensor suite w/ Raspberry Pi.]

**Total time spent: 5h**

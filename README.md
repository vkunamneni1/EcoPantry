---
title: "EcoPantry"
author: "veds"
description: "grocery expiration tracking, recipe suggestions, and a Raspberry Pi sensor suite for real-time fridge monitoring"
created_at: "2025-06-26"
---

# June 26th - Adding a Raspberry Pi to EcoPantry

So I made EcoPantry in conjunction with my high school's EECS class, while learning how to use JavaFX, SQLite, and other APIs. During the ~1-2 months of development, the app is able to read receipts (w/ Tesseract OCR), suggest recipes (w/ TheMealDB API), store user data (SQLite), and display statistics on food waste/savings. It is important to note, however, that it is pretty much unsustainable and very rudimentary. When I found out about Highway, I wanted to add a more physical component to EcoPantry. While it did have stats that showed how much food a user wasted/saved, actual data from inside a fridge would be really helpful. It could even allow the expiry date estimation to be even more accurate. So, I decided to start working on developing a sensor suite w/ Raspberry Pi.

Starting off, I made a very simple mockup of what I envision the entire system to be in the end (you can see that below). I researched around a little bit to see what parts I would need, but didn't come to a conclusive list yet.

After doing that, I went back to EcoPantry and started to clean it up. This included adding API integrations, fixing bugs, and just making UI look better. **I do not plan on using this version of EcoPantry since it's somewhat vibe coded and not good. I will make a new version with many more parts and features myself based on the current version that fits the needs of this new project.**

!(https://i.ibb.co/HT0syRNV/Eco-Pantry-System-Mockup.jpg)

**Total time spent: 5h**

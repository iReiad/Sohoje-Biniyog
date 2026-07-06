package com.example.data

import kotlin.random.Random

data class LiveStock(
    val symbol: String,
    val companyName: String,
    val companyNameBangla: String,
    val exchange: String, // "DSE" or "CSE"
    var currentPrice: Double,
    val yesterdayClose: Double,
    var high: Double,
    var low: Double,
    var volume: Long,
    val sector: String,
    val sectorBangla: String,
    val description: String,
    val descriptionBangla: String,
    val regularFees: String, // Brokerage etc.
    val regularFeesBangla: String,
    val hiddenFees: String,  // CDBL, Tax, etc.
    val hiddenFeesBangla: String,
    val dividendYield: Double, // %
    val peRatio: Double,
    val pbRatio: Double,
    val isMutualFund: Boolean = false,
    val fundManager: String = "",
    val fundManagerBangla: String = "",
    // Pre-calculated indicators for technical analysis
    var rsi: Double = 52.4,
    var sma50: Double = yesterdayClose * 0.98,
    var macd: Double = 0.45,
    var signal: String = "HOLD", // BUY, SELL, HOLD
    var signalBangla: String = "ধরে রাখুন" // ক্রয়, বিক্রয়, ধরে রাখুন
) {
    val change: Double get() = currentPrice - yesterdayClose
    val changePercent: Double get() = (change / yesterdayClose) * 100.0
}

object StockService {
    val initialStocks = listOf(
        LiveStock(
            symbol = "GP",
            companyName = "Grameenphone Ltd.",
            companyNameBangla = "গ্রামীণফোন লিমিটেড",
            exchange = "DSE",
            currentPrice = 286.4,
            yesterdayClose = 284.0,
            high = 289.0,
            low = 283.0,
            volume = 452000,
            sector = "Telecommunication",
            sectorBangla = "টেলিযোগাযোগ",
            description = "The leading telecommunications service provider in Bangladesh with millions of subscribers.",
            descriptionBangla = "বাংলাদেশের বৃহত্তম টেলিযোগাযোগ কোম্পানি। এটি একটি উচ্চ ডিভিডেন্ড প্রদানকারী নির্ভরযোগ্য শেয়ার (ব্লু চিপ ক্যাটাগরি)। দীর্ঘমেয়াদী বিনিয়োগের জন্য আদর্শ।",
            regularFees = "Brokerage Fee: 0.35% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৩৫%",
            hiddenFees = "Laga Charge: 0.02%, BO Account: 450 BDT/yr, 10% tax on Dividend (TIN holders).",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, বিও ফি: ৪৫০ টাকা/বছর, ডিভিডেন্ড কর: ১০% (টিন থাকলে), ১৫% (টিন না থাকলে)।",
            dividendYield = 8.5,
            peRatio = 12.4,
            pbRatio = 3.8
        ),
        LiveStock(
            symbol = "BATBC",
            companyName = "British American Tobacco Bangladesh",
            companyNameBangla = "ব্রিটিশ আমেরিকান টোব্যাকো বাংলাদেশ",
            exchange = "DSE",
            currentPrice = 398.2,
            yesterdayClose = 402.5,
            high = 405.0,
            low = 396.0,
            volume = 158000,
            sector = "Food & Allied",
            sectorBangla = "খাদ্য ও আনুষঙ্গিক",
            description = "One of the oldest and largest multinational companies operating in Bangladesh, known for premium dividends.",
            descriptionBangla = "বহুজাতিক সিগারেট উৎপাদনকারী প্রতিষ্ঠান। শক্তিশালী মুনাফা ও আকর্ষণীয় বোনাস ডিভিডেন্ডের জন্য পরিচিত। বাজারের অন্যতম নির্ভরযোগ্য ক্যাশ-ফ্লো শেয়ার।",
            regularFees = "Brokerage Fee: 0.35% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৩৫%",
            hiddenFees = "Laga Charge: 0.02%, BO Account Maintenance: 450 BDT, Source tax on Dividend: 10%.",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, বিও ফি: ৪৫০ টাকা/বছর, ডিভিডেন্ড কর: ১০%।",
            dividendYield = 7.8,
            peRatio = 14.2,
            pbRatio = 4.2
        ),
        LiveStock(
            symbol = "SQURPHARMA",
            companyName = "Square Pharmaceuticals PLC",
            companyNameBangla = "স্কয়ার ফার্মাসিউটিক্যালস পিএলসি",
            exchange = "DSE",
            currentPrice = 216.5,
            yesterdayClose = 215.1,
            high = 218.0,
            low = 214.5,
            volume = 680000,
            sector = "Pharmaceuticals",
            sectorBangla = "ওষুধ ও রসায়ন",
            description = "The pioneer of the pharmaceutical industry in Bangladesh with strong export markets.",
            descriptionBangla = "বাংলাদেশের ওষুধ শিল্পের শীর্ষস্থানীয় কোম্পানি। চমৎকার করপোরেট সুশাসন ও ধারাবাহিক প্রবৃদ্ধির অনন্য উদাহরণ। নতুনদের পোর্টফোলিওতে রাখার জন্য অত্যন্ত উপযুক্ত।",
            regularFees = "Brokerage Fee: 0.30% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৩০%",
            hiddenFees = "Laga Charge: 0.02%, CDBL Transaction Fee: 0.015%.",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, সিডিবিএল চার্জ: ০.০১%। লভ্যাংশের উপর ১০% কর কেটে রাখা হয়।",
            dividendYield = 5.2,
            peRatio = 9.8,
            pbRatio = 1.9
        ),
        LiveStock(
            symbol = "LHBL",
            companyName = "LafargeHolcim Bangladesh Ltd.",
            companyNameBangla = "লাফার্জহোলসিম বাংলাদেশ লিমিটেড",
            exchange = "CSE",
            currentPrice = 64.2,
            yesterdayClose = 62.8,
            high = 65.5,
            low = 62.5,
            volume = 1204000,
            sector = "Cement",
            sectorBangla = "সিমেন্ট শিল্প",
            description = "A global leader in building materials with advanced clinker production capabilities in Bangladesh.",
            descriptionBangla = "নির্মাণ খাতের বৃহত্তম বহুজাতিক সিমেন্ট কোম্পানি। আবাসন খাতের প্রবৃদ্ধির সাথে এর মূল্য সরাসরি সম্পর্কিত। কিছুটা মাঝারি ঝুঁকিপূর্ণ তবে ভালো সম্ভাবনাময় শেয়ার।",
            regularFees = "Brokerage Fee: 0.40% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৪০%",
            hiddenFees = "Laga Charge: 0.02%, Hawla Charge: 2 BDT per transaction.",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, হাওলা ফি: ২ টাকা। ডিভিডেন্ড কর: ১০%-১৫%।",
            dividendYield = 4.5,
            peRatio = 11.5,
            pbRatio = 2.1
        ),
        LiveStock(
            symbol = "ROBI",
            companyName = "Robi Axiata Limited",
            companyNameBangla = "রবি আজিয়াটা লিমিটেড",
            exchange = "DSE",
            currentPrice = 26.8,
            yesterdayClose = 27.2,
            high = 27.5,
            low = 26.4,
            volume = 2150000,
            sector = "Telecommunication",
            sectorBangla = "টেলিযোগাযোগ",
            description = "The second largest mobile network operator in Bangladesh with innovative digital services.",
            descriptionBangla = "বাংলাদেশের দ্বিতীয় বৃহত্তম মোবাইল অপারেটর। প্রবৃদ্ধির সম্ভাবনা প্রচুর তবে বর্তমানে পিই রেশিও বেশ উঁচুতে। নতুন বিনিয়োগকারীদের জন্য মাঝারি দীর্ঘমেয়াদে ভাবা যেতে পারে।",
            regularFees = "Brokerage Fee: 0.35% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৩৫%",
            hiddenFees = "Laga Charge: 0.02%, BO Fee: 450 BDT.",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, বিও রক্ষণাবেক্ষণ ফি: ৪৫০ টাকা।",
            dividendYield = 2.8,
            peRatio = 32.1,
            pbRatio = 2.4
        ),
        LiveStock(
            symbol = "BRACBANK",
            companyName = "BRAC Bank PLC",
            companyNameBangla = "ব্র্যাক ব্যাংক পিএলসি",
            exchange = "DSE",
            currentPrice = 38.4,
            yesterdayClose = 38.0,
            high = 39.2,
            low = 37.8,
            volume = 985000,
            sector = "Banking",
            sectorBangla = "ব্যাংক",
            description = "A leading commercial bank focusing on SME lending, famous for outstanding corporate ethics.",
            descriptionBangla = "ক্ষুদ্র ও মাঝারি শিল্প (SME) অর্থায়নে দেশের সেরা ব্যাংক। স্বচ্ছ হিসাব ও নির্ভরযোগ্য করপোরেট ট্র্যাক রেকর্ডের জন্য ব্যাংকিং খাতের সবচেয়ে আকর্ষণীয় শেয়ারগুলোর একটি।",
            regularFees = "Brokerage Fee: 0.30% per trade.",
            regularFeesBangla = "ব্রোকারেজ ফি: প্রতি লেনদেনে ০.৩০%",
            hiddenFees = "Laga Charge: 0.02%, CDBL Fee.",
            hiddenFeesBangla = "লাগা চার্জ: ০.০২%, বার্ষিক ব্যাংক ও বিও চার্জ প্রযোজ্য। ডিভিডেন্ড কর: ১০%।",
            dividendYield = 5.0,
            peRatio = 8.2,
            pbRatio = 1.1
        ),
        // Mutual Funds
        LiveStock(
            symbol = "1STPRIMFMF",
            companyName = "Prime Finance First Mutual Fund",
            companyNameBangla = "প্রাইম ফাইন্যান্স ফার্স্ট মিউচুয়াল ফান্ড",
            exchange = "DSE",
            currentPrice = 14.8,
            yesterdayClose = 14.5,
            high = 15.2,
            low = 14.4,
            volume = 520000,
            sector = "Mutual Funds",
            sectorBangla = "মিউচুয়াল ফান্ড",
            description = "A closed-end mutual fund aiming to provide stable income by investing in diversified equity portfolios.",
            descriptionBangla = "একটি মেয়াদী মিউচুয়াল ফান্ড। এটি সাধারণ বিনিয়োগকারীদের টাকা পেশাদার ফান্ড ম্যানেজার দ্বারা বৈচিত্র্যময় শেয়ার বাজারে খাটায়। নতুনদের জন্য ঝুঁকি কমানোর দারুণ মাধ্যম।",
            regularFees = "Fund Management Fee: 1.25% annually (inclusive inside Nav). Brokerage: 0.35%.",
            regularFeesBangla = "ফান্ড ব্যবস্থাপনা ফি: বছরে ১.২৫% (ন্যাভ-এর সাথে সমন্বিত)। ব্রোকারেজ: ০.৩৫%",
            hiddenFees = "Trustee Fee: 0.10%, Custodian Fee: 0.08%, Tax on Dividend.",
            hiddenFeesBangla = "ট্রাস্টি ফি: ০.১০%, কাস্টোডিয়ান ফি: ০.০৮%। ২৫,০০০ টাকা পর্যন্ত মিউচুয়াল ফান্ড লভ্যাংশ সম্পূর্ণ করমুক্ত!",
            dividendYield = 9.1,
            peRatio = 6.4,
            pbRatio = 0.85,
            isMutualFund = true,
            fundManager = "ICB Asset Management Company Ltd.",
            fundManagerBangla = "আইসিবি অ্যাসেট ম্যানেজমেন্ট কোম্পানি লিমিটেড"
        ),
        LiveStock(
            symbol = "AIBL1STIMF",
            companyName = "AIBL 1st Islamic Mutual Fund",
            companyNameBangla = "এআইবিএল ১ম ইসলামিক মিউচুয়াল ফান্ড",
            exchange = "CSE",
            currentPrice = 8.5,
            yesterdayClose = 8.6,
            high = 8.8,
            low = 8.4,
            volume = 320000,
            sector = "Mutual Funds",
            sectorBangla = "মিউচুয়াল ফান্ড (শরীয়াহ)",
            description = "A Shariah-compliant mutual fund investing in Islamic-approved stocks and instruments.",
            descriptionBangla = "সম্পূর্ণ শরীয়াহ ভিত্তিক ইসলামিক মিউচুয়াল ফান্ড। সুদমুক্ত ও হালাল উপায়ে শেয়ার বাজারে বিনিয়োগের জন্য সেরা অপশন। আন্ডারভ্যালুড অবস্থায় আছে (পিবি রেশিও ১-এর নিচে)।",
            regularFees = "Management Fee: 1.5% annually. Brokerage: 0.30%.",
            regularFeesBangla = "ব্যবস্থাপনা ফি: বছরে ১.৫০%। ব্রোকারেজ: ০.৩০%",
            hiddenFees = "CDBL Charge: 0.02%, No Tax on Dividend up to 25,000 BDT.",
            hiddenFeesBangla = "সিডিবিএল চার্জ: ০.০২%। মিউচুয়াল ফান্ড লভ্যাংশ ২৫,০০০ টাকা পর্যন্ত সম্পূর্ণ করমুক্ত।",
            dividendYield = 10.2,
            peRatio = 5.2,
            pbRatio = 0.65,
            isMutualFund = true,
            fundManager = "LankaBangla Asset Management Ltd.",
            fundManagerBangla = "লংকাবাংলা অ্যাসেট ম্যানেজমেন্ট লিমিটেড"
        )
    )

    // Simulates the price fluctuations of the stock market
    fun fluctuatePrices(stocks: List<LiveStock>): List<LiveStock> {
        return stocks.map { stock ->
            val changePercent = Random.nextDouble(-1.5, 1.6) // max 1.5% change
            val oldPrice = stock.currentPrice
            val newPrice = Math.round((oldPrice * (1.0 + changePercent / 100.0)) * 10.0) / 10.0
            
            // Limit bounds to avoid crazy numbers
            val boundedPrice = if (newPrice < stock.yesterdayClose * 0.9) {
                stock.yesterdayClose * 0.9 // circuit breaker lower limit 10%
            } else if (newPrice > stock.yesterdayClose * 1.1) {
                stock.yesterdayClose * 1.1 // circuit breaker upper limit 10%
            } else {
                newPrice
            }

            // Update high / low
            val updatedHigh = if (boundedPrice > stock.high) boundedPrice else stock.high
            val updatedLow = if (boundedPrice < stock.low) boundedPrice else stock.low
            val additionalVolume = Random.nextLong(500, 8000)

            // Recalculate Mock Technical Indicators dynamically!
            val updatedRsi = (stock.rsi + Random.nextDouble(-2.0, 2.1)).coerceIn(10.0, 90.0)
            val updatedSma = stock.yesterdayClose * (0.95 + (updatedRsi / 150.0))
            val updatedMacd = stock.macd + Random.nextDouble(-0.02, 0.021)

            // Generate actionable signals for beginners!
            val (sig, sigB) = when {
                updatedRsi < 35.0 -> Pair("BUY", "ক্রয় করুন (কম দাম)")
                updatedRsi > 70.0 -> Pair("SELL", "বিক্রয় করুন (অতিরিক্ত দাম)")
                else -> Pair("HOLD", "ধরে রাখুন (স্থিতিশীল)")
            }

            stock.copy(
                currentPrice = boundedPrice,
                high = updatedHigh,
                low = updatedLow,
                volume = stock.volume + additionalVolume,
                rsi = updatedRsi,
                sma50 = updatedSma,
                macd = updatedMacd,
                signal = sig,
                signalBangla = sigB
            )
        }
    }
}

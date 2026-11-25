package com.example.data.local

import com.example.domain.model.BotTemplate

object BuiltInTemplates {

    val testBotPythonCode = """
import telebot
from telebot import types
import datetime

# BOT BUILDERS uz - Test Bot (Namuna)
# Ushbu bot.py faylini o'rganib, o'zingiz ham yangi botlar yuklashingiz mumkin!

BOT_NAME = "Test Echo & Salom Bot"
VERSION = "1.0"

@bot.message_handler(commands=['start'])
def handle_start(message):
    user_name = message.from_user.first_name
    markup = types.InlineKeyboardMarkup()
    markup.add(
        types.InlineKeyboardButton(text="ℹ️ Bot haqida", callback_data="about_bot"),
        types.InlineKeyboardButton(text="🕒 Joriy vaqt", callback_data="current_time")
    )
    markup.add(
        types.InlineKeyboardButton(text="📋 Buyruqlar ro'yxati", callback_data="help_cmd")
    )
    text = f"Assalomu alaykum, {user_name}!\n\n🤖 Men {BOT_NAME} man.\n📱 Telefoningizda 24/7 fon rejimida ishlayapman!\n\nMenga istalgan xabar yozing yoki quyidagi tugmalardan foydalaning:"
    bot.send_message(message.chat.id, text, reply_markup=markup)

@bot.message_handler(commands=['help'])
def handle_help(message):
    help_text = "📌 Mavjud buyruqlar:\n/start - Botni ishga tushirish\n/help - Yordam menyusi\n/info - Bot tizim ma'lumoti\n/vaqt - Server vaqtini ko'rish\n\nShuningdek, istalgan matn yuborsangiz uni qaytarib beraman!"
    bot.reply_to(message, help_text)

@bot.message_handler(commands=['info'])
def handle_info(message):
    info_text = f"⚙️ Tizim: BOT BUILDERS uz Python Engine\n🤖 Nomi: {BOT_NAME}\n🔢 Versiya: {VERSION}\n⚡ Holat: 24/7 Faol"
    bot.reply_to(message, info_text)

@bot.message_handler(commands=['vaqt'])
def handle_time(message):
    now_str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    bot.reply_to(message, f"🕒 Hozirgi vaqt: {now_str}")

@bot.callback_query_handler(func=lambda call: True)
def handle_callbacks(call):
    if call.data == "about_bot":
        bot.answer_callback_query(call.id, text="BOT BUILDERS uz namunaviy bot!")
        bot.send_message(call.message.chat.id, "🚀 Bu bot Python (bot.py) + malumot.json + icon.png asosida telefonda to'g'ridan-to'g'ri ishlamoqda.")
    elif call.data == "current_time":
        now_str = datetime.datetime.now().strftime("%H:%M:%S")
        bot.answer_callback_query(call.id, text=f"Vaqt: {now_str}")
        bot.send_message(call.message.chat.id, f"🕒 Hozirgi vaqt: {now_str}")
    elif call.data == "help_cmd":
        bot.answer_callback_query(call.id, text="Buyruqlar yuborildi")
        bot.send_message(call.message.chat.id, "Buyruqlar: /start, /help, /info, /vaqt")

@bot.message_handler(func=lambda m: True)
def echo_all(message):
    user_name = message.from_user.first_name
    text = message.text
    if "salom" in text.lower():
        bot.reply_to(message, f"Vaalaykum assalom, {user_name}! 👋 Qanday yordam bera olaman?")
    else:
        bot.reply_to(message, f"💬 {user_name}, siz yozdingiz:\n\"{text}\"")

bot.polling(none_stop=True)
""".trimIndent()

    val testBotInfoJson = """
{
  "name": "Test Bot (Namuna)",
  "tagline": "O'rganish va sinash uchun bepul Python bot",
  "description": "Bu rasmiy namuna boti bo'lib, bot.py, malumot.json va icon.png fayllari qanday tuzilishini ko'rsatadi. /start, /help, /info, /vaqt buyruqlari hamda inline tugmalar bilan ishlaydi.",
  "price": 0,
  "monthly_fee": 0,
  "category": "Namuna / Test",
  "version": "1.0",
  "author": "BOT BUILDERS uz",
  "commands": [
    "/start - Asosiy menyu va tugmalar",
    "/help - Barcha buyruqlar",
    "/info - Bot haqida tizim ma'lumoti",
    "/vaqt - Aniq vaqtni ko'rish"
  ],
  "features": [
    "24/7 fon rejimida avtomatik ishlash",
    "Inline tugmalar (InlineKeyboardMarkup)",
    "Aqlli Salom va Echo javoblari",
    "To'liq ochiq bot.py kodi"
  ]
}
""".trimIndent()

    private val shopBotPythonCode = """
import telebot
from telebot import types

STORE_NAME = "TechMarket UZ"

@bot.message_handler(commands=['start'])
def start_shop(message):
    user_name = message.from_user.first_name
    markup = types.InlineKeyboardMarkup()
    markup.add(
        types.InlineKeyboardButton(text="📱 Smartfonlar", callback_data="cat_phone"),
        types.InlineKeyboardButton(text="💻 Noutbuklar", callback_data="cat_laptop")
    )
    markup.add(
        types.InlineKeyboardButton(text="🎧 Aksessuarlar", callback_data="cat_acc"),
        types.InlineKeyboardButton(text="📞 Operator", callback_data="contact_op")
    )
    bot.send_message(message.chat.id, f"🛍 {STORE_NAME} do'koniga xush kelibsiz, {user_name}!\n\nBo'limni tanlang:", reply_markup=markup)

@bot.message_handler(commands=['katalog'])
def show_catalog(message):
    bot.reply_to(message, "📦 Asosiy mahsulotlar:\n1. Smartfon Pro — 4 500 000 so'm\n2. Ultrabook Air — 8 200 000 so'm\n3. Sim-siz quloqchin — 350 000 so'm\n\nBuyurtma uchun /start bosing.")

@bot.callback_query_handler(func=lambda call: True)
def shop_callbacks(call):
    if call.data == "cat_phone":
        bot.answer_callback_query(call.id, text="Smartfonlar ochildi")
        markup = types.InlineKeyboardMarkup()
        markup.add(types.InlineKeyboardButton(text="✅ Buyurtma berish", callback_data="order_phone"))
        bot.send_message(call.message.chat.id, "📱 Smartfon Pro 256GB\n💰 Narxi: 4 500 000 so'm\n🚚 Yetkazib berish: Bepul", reply_markup=markup)
    elif call.data == "cat_laptop":
        bot.answer_callback_query(call.id, text="Noutbuklar ochildi")
        markup = types.InlineKeyboardMarkup()
        markup.add(types.InlineKeyboardButton(text="✅ Buyurtma berish", callback_data="order_laptop"))
        bot.send_message(call.message.chat.id, "💻 Ultrabook Air M-Series\n💰 Narxi: 8 200 000 so'm\n🛡 Kafolat: 1 yil", reply_markup=markup)
    elif call.data == "cat_acc":
        bot.answer_callback_query(call.id, text="Aksessuarlar")
        bot.send_message(call.message.chat.id, "🎧 AirSound Pro — 350 000 so'm\n🔋 PowerBank 20000mAh — 280 000 so'm")
    elif call.data == "contact_op":
        bot.answer_callback_query(call.id, text="Operator aloqasi")
        bot.send_message(call.message.chat.id, "📞 Murojaat uchun telefon raqamingizni xabar sifatida yozib qoldiring.")
    else:
        bot.answer_callback_query(call.id, text="Buyurtma qabul qilindi!")
        bot.send_message(call.message.chat.id, "🎉 Buyurtmangiz ro'yxatga olindi! Tez orada menejer bog'lanadi.")

@bot.message_handler(func=lambda m: True)
def handle_order_text(message):
    bot.reply_to(message, f"✅ Xabaringiz qabul qilindi, {message.from_user.first_name}! Operatorlarimiz tez orada javob berishadi.")
""".trimIndent()

    private val shopBotInfoJson = """
{
  "name": "Do'kon & Savdo Bot",
  "tagline": "Onlayn savdo va buyurtma qabul qiluvchi bot",
  "description": "Telegram orqali mahsulot katalogini ko'rsatish, narxlarni chiqarish va mijozlardan buyurtma qabul qilish uchun tayyor professional savdo boti.",
  "price": 25000,
  "monthly_fee": 0,
  "category": "Biznes / Savdo",
  "version": "2.1",
  "author": "BOT BUILDERS uz",
  "commands": [
    "/start - Do'kon bosh menyusi",
    "/katalog - Mahsulotlar narxlari"
  ],
  "features": [
    "Kategoriyalar bo'yicha inline menyu",
    "1 bosishda buyurtma qabul qilish",
    "Mijoz xabarlarini avtomatik tasdiqlash"
  ]
}
""".trimIndent()

    private val adminBotPythonCode = """
import telebot

BAD_WORDS = ["http://", "https://", "t.me/", "reklama", "casino"]

@bot.message_handler(commands=['start', 'rules'])
def send_rules(message):
    rules = "🛡 Guruh qoidalari:\n1. Reklama va havola (link) tashlamang\n2. Hurmatni saqlang\n3. Spam yubormang\n\n⚠️ Qoidabuzarlik xabari avtomatik o'chiriladi!"
    bot.reply_to(message, rules)

@bot.message_handler(commands=['ping'])
def ping(message):
    bot.reply_to(message, "🟢 Nazoratchi tizim faol! Guruh 24/7 himoya ostida.")

@bot.message_handler(func=lambda m: True)
def moderate_messages(message):
    lower_text = message.text.lower()
    for word in BAD_WORDS:
        if word in lower_text:
            bot.delete_message(message.chat.id, message.message_id)
            bot.send_message(message.chat.id, f"⚠️ {message.from_user.first_name}, guruhda reklama va havola tarqatish taqiqlangan!")
            return
    if "admin" in lower_text:
        bot.reply_to(message, "👮‍♂️ Adminlar xabardor qilindi. Qoidabuzarlik bo'lsa /rules ni ko'ring.")
""".trimIndent()

    private val adminBotInfoJson = """
{
  "name": "Guruh Nazoratchi Bot",
  "tagline": "Reklama va linklarni o'chiruvchi himoya boti",
  "description": "Telegram guruhlarni 24/7 nazorat qiladi: ruxsatsiz havolalar, reklama va spam xabarlarni avtomatik o'chirib, ogohlantirish yuboradi.",
  "price": 18000,
  "monthly_fee": 0,
  "category": "Xavfsizlik / Admin",
  "version": "1.8",
  "author": "BOT BUILDERS uz",
  "commands": [
    "/start - Botni faollashtirish",
    "/rules - Guruh qoidalari",
    "/ping - Himoya holatini tekshirish"
  ],
  "features": [
    "Havola (http/t.me) larni avtomatik o'chirish",
    "Tezkor ogohlantirish tizimi",
    "Guruhda 24/7 uzluksiz nazorat"
  ]
}
""".trimIndent()

    private val channelBotPythonCode = """
import telebot
from telebot import types

@bot.message_handler(commands=['start'])
def start_support(message):
    markup = types.InlineKeyboardMarkup()
    markup.add(
        types.InlineKeyboardButton(text="❓ Ko'p so'raladigan savollar", callback_data="faq"),
        types.InlineKeyboardButton(text="📢 Reklama narxlari", callback_data="ads")
    )
    markup.add(
        types.InlineKeyboardButton(text="✍️ Murojaat yuborish", callback_data="feedback")
    )
    bot.send_message(message.chat.id, f"Assalomu alaykum, {message.from_user.first_name}!\n\n📡 Kanal va murojaatlar bo'limiga xush kelibsiz:", reply_markup=markup)

@bot.message_handler(commands=['about'])
def about_channel(message):
    bot.reply_to(message, "📡 Bizning rasmiy aloqa botimiz. Taklif va murojaatlaringizni yozib qoldiring!")

@bot.callback_query_handler(func=lambda call: True)
def handle_support_cb(call):
    if call.data == "faq":
        bot.answer_callback_query(call.id, text="FAQ")
        bot.send_message(call.message.chat.id, "❓ Tez-tez so'raladigan savollar:\n• Ish vaqti: 24/7\n• Javob berish muddati: 5 daqiqa ichida")
    elif call.data == "ads":
        bot.answer_callback_query(call.id, text="Reklama")
        bot.send_message(call.message.chat.id, "📢 Reklama tariflari:\n• Oddiy post: 50 000 so'm\n• Top 24 soat: 120 000 so'm")
    elif call.data == "feedback":
        bot.answer_callback_query(call.id, text="Yozishingiz mumkin")
        bot.send_message(call.message.chat.id, "✍️ Murojaat matnini shu yerga yozib yuboring:")

@bot.message_handler(func=lambda m: True)
def receive_feedback(message):
    bot.reply_to(message, f"📩 Rahmat, {message.from_user.first_name}! Murojaatingiz adminga yetkazildi.")
""".trimIndent()

    private val channelBotInfoJson = """
{
  "name": "Kanal & Aloqa Bot",
  "tagline": "Murojaatlar, FAQ va reklama qabul boti",
  "description": "Telegram kanallar va bloglar uchun qulay aloqa boti. Obunachilardan taklif, murojaat va reklama buyurtmalarini tartibli qabul qiladi.",
  "price": 15000,
  "monthly_fee": 0,
  "category": "Kanal / Media",
  "version": "1.5",
  "author": "BOT BUILDERS uz",
  "commands": [
    "/start - Aloqa menyusi",
    "/about - Kanal haqida"
  ],
  "features": [
    "FAQ va reklama tariflari menyusi",
    "Avtomatik murojaat qabul qilish",
    "Tezkor inline tugmalar"
  ]
}
""".trimIndent()

    val templates: List<BotTemplate> = listOf(
        BotTemplate(
            id = "builtin_test_bot",
            name = "Test Bot (Namuna)",
            shortTagline = "Bepul o'rganish va sinash boti",
            description = "Bu rasmiy namuna boti bo'lib, bot.py, malumot.json va icon.png fayllari qanday tuzilishini ko'rsatadi. /start, /help, /info, /vaqt buyruqlari hamda inline tugmalar bilan real ishlaydi.",
            priceSom = 0L,
            monthlyServerFeeSom = 0L,
            category = "Namuna / Test",
            version = "1.0",
            author = "BOT BUILDERS uz",
            commands = listOf(
                "/start - Asosiy menyu va tugmalar",
                "/help - Barcha buyruqlar",
                "/info - Bot haqida tizim ma'lumoti",
                "/vaqt - Aniq vaqtni ko'rish"
            ),
            features = listOf(
                "24/7 fon rejimida avtomatik ishlash",
                "Inline tugmalar (InlineKeyboardMarkup)",
                "Aqlli Salom va Echo javoblari",
                "To'liq ochiq bot.py va malumot.json"
            ),
            pythonCode = testBotPythonCode,
            infoJson = testBotInfoJson,
            drawableResName = "img_bot_test",
            uptimeSla = "99.9%",
            speedScore = 99,
            reliabilityScore = 98,
            popularityScore = 100
        ),
        BotTemplate(
            id = "builtin_shop_bot",
            name = "Do'kon & Savdo Bot",
            shortTagline = "Onlayn katalog va buyurtma boti",
            description = "Telegram orqali mahsulot katalogini ko'rsatish, narxlarni chiqarish va mijozlardan buyurtma qabul qilish uchun tayyor professional savdo boti.",
            priceSom = 25000L,
            monthlyServerFeeSom = 0L,
            category = "Biznes / Savdo",
            version = "2.1",
            author = "BOT BUILDERS uz",
            commands = listOf(
                "/start - Do'kon bosh menyusi",
                "/katalog - Mahsulotlar narxlari"
            ),
            features = listOf(
                "Kategoriyalar bo'yicha inline menyu",
                "1 bosishda buyurtma qabul qilish",
                "Mijoz xabarlarini avtomatik tasdiqlash"
            ),
            pythonCode = shopBotPythonCode,
            infoJson = shopBotInfoJson,
            drawableResName = "img_bot_shop",
            uptimeSla = "99.9%",
            speedScore = 97,
            reliabilityScore = 99,
            popularityScore = 95
        ),
        BotTemplate(
            id = "builtin_admin_bot",
            name = "Guruh Nazoratchi",
            shortTagline = "Anti-link va reklama himoyachisi",
            description = "Telegram guruhlarni 24/7 nazorat qiladi: ruxsatsiz havolalar, reklama va spam xabarlarni avtomatik o'chirib, ogohlantirish yuboradi.",
            priceSom = 18000L,
            monthlyServerFeeSom = 0L,
            category = "Xavfsizlik / Admin",
            version = "1.8",
            author = "BOT BUILDERS uz",
            commands = listOf(
                "/start - Botni faollashtirish",
                "/rules - Guruh qoidalari",
                "/ping - Himoya holatini tekshirish"
            ),
            features = listOf(
                "Havola (http/t.me) larni avtomatik o'chirish",
                "Tezkor ogohlantirish tizimi",
                "Guruhda 24/7 uzluksiz nazorat"
            ),
            pythonCode = adminBotPythonCode,
            infoJson = adminBotInfoJson,
            drawableResName = "img_bot_admin",
            uptimeSla = "99.9%",
            speedScore = 98,
            reliabilityScore = 97,
            popularityScore = 91
        ),
        BotTemplate(
            id = "builtin_channel_bot",
            name = "Kanal & Aloqa Bot",
            shortTagline = "Murojaatlar va reklama qabul boti",
            description = "Telegram kanallar va bloglar uchun qulay aloqa boti. Obunachilardan taklif, murojaat va reklama buyurtmalarini tartibli qabul qiladi.",
            priceSom = 15000L,
            monthlyServerFeeSom = 0L,
            category = "Kanal / Media",
            version = "1.5",
            author = "BOT BUILDERS uz",
            commands = listOf(
                "/start - Aloqa menyusi",
                "/about - Kanal haqida"
            ),
            features = listOf(
                "FAQ va reklama tariflari menyusi",
                "Avtomatik murojaat qabul qilish",
                "Tezkor inline tugmalar"
            ),
            pythonCode = channelBotPythonCode,
            infoJson = channelBotInfoJson,
            drawableResName = "img_bot_channel",
            uptimeSla = "99.8%",
            speedScore = 96,
            reliabilityScore = 96,
            popularityScore = 89
        )
    )
}

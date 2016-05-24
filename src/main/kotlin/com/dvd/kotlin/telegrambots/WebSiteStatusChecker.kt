package com.dvd.kotlin.telegrambots

import org.telegram.telegrambots.TelegramApiException
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.regex.Pattern

/**
 * Created by dvdandroid on 24/05/2016.
 */
class WebSiteStatusChecker : TelegramLongPollingBot() {
    var timeout = 5000

    var verbose = 1
    var isTimeout = false
    var isVerbose = false
    val messages = arrayOf(
            arrayOf("Try again", "Malformed URL. Please provide a right link"),
            arrayOf("UP", "%s is UP."),
            arrayOf("DOWN", "%s is DOWN."))
    val status = arrayOf("DISABLED", "ENABLED")

    fun pingHost(host: String): String {
        val web = Pattern.compile("^(http(s?)://)?[a-zA-Z0-9\\.\\-_]+(\\.[a-zA-Z]{2,3})+(/[a-zA-Z0-9_\\-\\s\\./\\?%#&=]*)?$")
        if (!web.matcher(host).matches()) {
            return messages[0][verbose]
        }

        try {
            Socket().use({ socket ->
                socket.connect(InetSocketAddress(host, 80), timeout)
                return String.format(messages[1][verbose], host)
            })
        } catch (e: IOException) {
            return String.format(messages[2][verbose], host)
        }
    }

    override fun onUpdateReceived(update: Update?) {
        val chat_id = update!!.message.chatId.toString()
        val text = update.message.text

        try {
            if (isTimeout) {
                val oldTimeout = timeout;
                timeout = try {
                    Integer.parseInt(text)
                } catch(e: NumberFormatException) {
                    oldTimeout
                }

                val verboseSet = SendMessage()
                verboseSet.text = "Timeout is $timeout"
                verboseSet.chatId = chat_id
                sendMessage(verboseSet)
                isTimeout = false
                return
            }

            if (isVerbose) {
                verbose = if (text.equals("ENABLED")) 1; else 0;

                val verboseSet = SendMessage()
                verboseSet.text = "Verbose mode is " + status[verbose]
                verboseSet.chatId = chat_id
                sendMessage(verboseSet)

                isVerbose = false
                return
            }

            when (text) {
                "/start" -> {
                    val message = SendMessage();
                    message.text = "Bot initialized"
                    message.chatId = chat_id;
                    sendMessage(message)
                }
                "/verbose" -> {
                    val row = KeyboardRow()
                    row.add(KeyboardButton("ENABLED"))
                    row.add(KeyboardButton("DISABLED"))

                    val list = ArrayList<KeyboardRow>()
                    list.add(row)

                    val keyboardMarkup = ReplyKeyboardMarkup()
                    keyboardMarkup.keyboard = list
                    keyboardMarkup.oneTimeKeyboad = true
                    keyboardMarkup.resizeKeyboard = true

                    val verboseMessage = SendMessage()
                    verboseMessage.text = "Set verbose mode."
                    verboseMessage.chatId = chat_id
                    verboseMessage.replayMarkup = keyboardMarkup
                    sendMessage(verboseMessage)

                    isVerbose = true
                }
                "/timeout" -> {
                    isTimeout = true

                    val enterTimeout = SendMessage()
                    enterTimeout.text = "Enter a timeout value in milliseconds (default 5000):"
                    enterTimeout.chatId = chat_id
                    sendMessage(enterTimeout)
                }
                else -> {
                    val result = SendMessage()
                    result.text = pingHost(text)
                    result.chatId = chat_id
                    result.disableWebPagePreview()
                    sendMessage(result)
                }
            }

        } catch(e: TelegramApiException) {
            e.printStackTrace()
        }

    }

    override fun getBotToken(): String? {
        return BotConfig().TOKEN_WEBSITE_STATUS_CHECKER;
    }

    override fun getBotUsername(): String? {
        return "websitestatuschecker_bot"
    }

}
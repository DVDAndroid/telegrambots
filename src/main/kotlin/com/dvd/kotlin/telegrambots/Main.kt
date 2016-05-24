package com.dvd.kotlin.telegrambots

import org.telegram.telegrambots.TelegramBotsApi

/**
 * Created by dvdandroid on 24/05/2016.
 */
class Main {
    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val bots = TelegramBotsApi()
            bots.registerBot(WebSiteStatusChecker())
        }
    }
}
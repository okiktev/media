package com.delfin.media

import org.slf4j.LoggerFactory

class Utils {

	companion object {

		private val log = LoggerFactory.getLogger(Utils::class.java)

		fun timer(msg: String, consumer: () -> Unit) {
			val start = System.currentTimeMillis()
			consumer()
			log.info(msg + " for ${System.currentTimeMillis() - start}ms")
		}

		fun timerGet(msg: String, function: () -> Any): Any {
			val start = System.currentTimeMillis()
			val res = function()
			log.info(msg + " for ${System.currentTimeMillis() - start}ms")
			return res
		}

	}

}
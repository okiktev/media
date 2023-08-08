package com.delfin.media.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.json.JSONException
import java.io.IOException
import org.json.JSONObject
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.WebSocketMessage

import com.delfin.media.Film
import com.delfin.media.Store
import com.delfin.media.Scanner

import org.json.JSONArray
import java.io.File


@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
		println("$$ registerWebSocketHandlers")
        registry.addHandler(TextHandler(), "/websocket").setAllowedOrigins("*")
    }

}

@Component
class TextHandler : TextWebSocketHandler() {

    @Throws(InterruptedException::class, IOException::class, JSONException::class)
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
		val jsonPayload = JSONObject(message.payload)
		val cfg = jsonPayload.get("cfg") as JSONObject
		var fimlsPerRow: Int = 4
		val names = JSONObject.getNames(cfg)
		if (names != null && names.contains("fimlsPerRow")) {			
			fimlsPerRow = cfg.get("fimlsPerRow") as Int
		}
		if (names != null && names.contains("imgTimestamp")) {			
			val imgTimestamp = cfg.get("imgTimestamp") as Int
			val hash = ((jsonPayload.get("films") as JSONArray).get(0) as JSONObject).get("hash") as String
			Scanner.updateImage(hash, imgTimestamp)
			val msg: Message = Message(listOfNotNull(Store.find(hash)), Cfg(null), Cmd(true))
			session.sendMessage(TextMessage(JSONObject(msg).toString()))
			return
		}
		val cmd = jsonPayload.get("cmd") as JSONObject
		val commands = JSONObject.getNames(cmd)
		if (commands != null && commands.contains("play")) {
			val location = ((jsonPayload.get("films") as JSONArray).get(0) as JSONObject).get("location") as String
			File("scripts", "to_play.txt").writeText(location)
			return
		}

		val msg: Message = Message(Store.getFilms(), Cfg(fimlsPerRow), Cmd(null))
		session.sendMessage(TextMessage(JSONObject(msg).toString()))
    }

}

class Message(val films: List<Film>, val cfg: Cfg, val cmd: Cmd) {
	
}

class Cfg(val fimlsPerRow: Int?) {
	
}

class Cmd(val updateFilm: Boolean?) {
	
}


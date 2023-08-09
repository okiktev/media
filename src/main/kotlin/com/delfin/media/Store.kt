package com.delfin.media

import java.util.concurrent.ConcurrentHashMap
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.Date
import java.util.HashMap
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.stream.Stream
import com.delfin.media.websocket.Cfg


class Store {

	private val log = LoggerFactory.getLogger(javaClass)

	private val data: ConcurrentHashMap<String, Film> = ConcurrentHashMap()
	private val toAdd: ConcurrentHashMap<String, Film> = ConcurrentHashMap()
	private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
	private val dumpPeriod = AppProps.get("store.dump.period.ms", "120000").toLong()
	private val storeFile = File(AppProps.get("store.file.path", "data/films.json"))

	private constructor() {
		executor.schedule(Callable {
			while (true) {
				try {
					dump()
					Thread.sleep(dumpPeriod)
				} catch (t: Throwable) {
					log.error("An error occurred while dumping films.", t)
				}
			}
		}, 10, TimeUnit.MINUTES)
	}

	companion object {
		private val store: Store = Store()
		private val cfg: Cfg = Cfg(4)
		fun setFilmsPerRow(filmsPerRow: Int) {
			cfg.fimlsPerRow = filmsPerRow
		}
		fun getCfg(): Cfg {
			return cfg
		}
		fun add(film : Film) {
			println("adding film $film")
			store.toAdd.put(film.hash, film)
		}
		fun delete(f: Film) {
			store.data.remove(f.hash)
			store.toAdd.remove(f.hash)
			store.log.info("Film '${f.name}' has been removed.")
		}
		fun load() {
			store.load()
		}
		fun isNotStored(hash: String): Boolean {
			return !store.toAdd.containsKey(hash) && !store.data.containsKey(hash)
		}
		fun isNotStored(file: File): Boolean {
			return !Stream.concat(store.toAdd.values.stream(), store.data.values.stream())
				.filter{film -> film.location.equals(file)}
				.findFirst()
				.isPresent()
		}
		fun getFilms(): List<Film> {
			val films = ArrayList<Film>()
			films.addAll(store.data.values)
			films.addAll(store.toAdd.values)
			return films
		}
		fun find(hash: String): Film? {
			var film: Film? = store.data.get(hash)
			if (film == null) {
				film = store.toAdd.get(hash)
			}
			return film
		}
    }

	private fun load() {
		val content = storeFile.readText()
		if (content.isEmpty()) {
			return
		}
		val toStore: HashMap<String, Film> = HashMap()
		JSONObject(content).toMap().forEach { (key, value) ->
		    toStore.put(key, Film.create(value as HashMap<String, Any?>))
		}
		data.putAll(toStore)
		log.info("Loaded ${data.size} films.")
	}

	private fun dump() {
		if (toAdd.isEmpty()) {
			return
		}
		data.putAll(toAdd)
		toAdd.clear()
		storeFile.writeText(JSONObject(data).toString())
		log.info("Dumped ${data.size} films.")
	}

}
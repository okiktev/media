package com.delfin.media

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.Callable
import java.util.Date
import java.io.File
import java.lang.Runnable
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.ArrayList


class Scanner () {

	private val filmsCreator: ExecutorService = Executors.newFixedThreadPool(10)
	private val videoDir = File(AppProps.get("store.video.dir.path"))
	private val locked: ConcurrentHashMap<String, File> = ConcurrentHashMap()
	private val regexAudio = """(\s.)*(Stream\s#0):(.*):\sAudio:(.*)""".toRegex()

	companion object {
		private val log = LoggerFactory.getLogger(Scanner::class.java)
		private val scanPeriod = AppProps.get("store.scan.period.ms", "1800000").toLong()
		private val ffToolDir = AppProps.get("ffmpeg.dir.path")
		val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
		val scanner: Scanner = Scanner()
		fun start() {
			executor.schedule(Callable {
				while (true) {
					try {
						scanner.scan()
						Thread.sleep(scanPeriod)
					} catch (t: Throwable) {
						t.printStackTrace()
					}
				}
			}, 10, TimeUnit.SECONDS)
		}
		fun updateImage(hash: String, timestamp: Int) {
			val f: Film? = Store.find(hash)
			if (f == null) {
				return
			}
			try {
				val imgFile = File("app/src/assets/img/previews/${f.hash}_${timestamp}.png")
				CmdRun.run(null, "\"${ffToolDir}/ffmpeg\""
					, "-loglevel", "quiet"
					, "-i", "\"${f.location.getAbsolutePath()}\""
					,  "-ss", "" + timestamp
					,  "-f",  "image2", "-r", "25", "-y", "\"${imgFile.getAbsolutePath()}\"")
				//TODO: remove old img preview
				val img = ImgData(imgFile.getParentFile().getAbsolutePath().replace(File("app/src").getAbsolutePath() + "\\", "").replace("\\", "/"), timestamp)
				f.imgPath = img.imgPath
				f.imgTimestamp = img.imgTimestamp
			} catch (e: Exception) {
				log.error("Unable to extract image from video ${f.location}", e)
			}
		}
    }

	fun scan() = Utils.timer("Scanned source $videoDir", {
		val discovered = ArrayList<File>()
		bypass(videoDir, discovered)
		Store.getFilms().stream()
			.filter { f -> !discovered.contains(f.location) }
			.forEach { Store.delete(it as Film) }
		
		println("$$$ discovered films " + discovered)
		
		discovered.forEach {
			filmsCreator.submit(Runnable() {
				if (locked.contains(it.getAbsolutePath())) {
					return@Runnable
				} else {
					locked.put(it.getAbsolutePath(), it)
				}
				try {
					createFilm(it)
				} finally {
					locked.remove(it.getAbsolutePath())
				}
			})
		}
	})

	private fun createFilm(file: File) {
		val start = System.currentTimeMillis()
		// TODO it takes hash of the video file only.
		// It's better to add filepath hash as well.

		if (Store.isNotStored(file)) {
			val hash = Film.hash(file)
			try {
				val resolution = extractFilmResolution(file)
				val imgData = extractImagePath(file, hash)
				val audioTracks = extractAudioTracks(file)
				Store.add(Film.create(file, hash,  resolution[0].toInt(), resolution[1].toInt(), audioTracks, imgData.imgPath, imgData.imgTimestamp))
				log.info("Film '${file.getName()}' has been added for " + (System.currentTimeMillis() - start))
			} catch (e: Exception) {
				log.error("Couldn't create a file ${file}", e)
			}
		}
	}

	private fun extractAudioTracks(file: File): List<String> {
		try {
			val res = CmdRun.run(null, "\"${ffToolDir}/ffmpeg\"", "-i", "\"${file.getAbsolutePath()}\"")
			val result = ArrayList<String>()
			(res.out + '\n' + res.err).lines().forEach {
				val l = regexAudio.findAll(it).map {
					it.groupValues[3] + ", " + it.groupValues[4]
				}.toList()
				result.addAll(l)
			}
			return result
		} catch (e: Exception) {
			log.error("Unable to extract audio tracks from video $file", e)
			return emptyList()
		}
	}

	private fun extractImagePath(file: File, hash: String): ImgData {
		try {
			val imgFile = File("app/src/assets/img/previews/${hash}_30.png")
			CmdRun.run(null, "\"${ffToolDir}/ffmpeg\""
				, "-loglevel", "quiet"
				, "-i", "\"${file.getAbsolutePath()}\""
				,  "-ss", "30"
				,  "-f",  "image2", "-r", "25", "-y", "\"${imgFile.getAbsolutePath()}\"")
			return ImgData(imgFile.getParentFile().getAbsolutePath().replace(File("app/src").getAbsolutePath() + "\\", "").replace("\\", "/"), 30)
		} catch (e: Exception) {
			log.error("Unable to extract image from video $file", e)
			return ImgData("", 30)
		}
	}

	private fun extractFilmResolution(file: File): List<String> {
		try {
			val res = CmdRun.run(null, "\"${ffToolDir}/ffprobe\"", "-v", "error"
				, "-select_streams", "v:0"
				, "-show_entries", "stream=width,height"
				, "-of", "csv=p=0"
				, "\"${file.getAbsolutePath()}\"")
			if (res.code != 0) {
				log.error("Unable to extract video resolution from $file. Cause: [${res.err}]")
				return listOf("0", "0")
			}
			return res.out.trim().split(",")
		} catch (e: Exception) {
			log.error("Unable to extract resolution from video $file", e)
			return listOf("0", "0")
		}
	}

	private fun bypass(dir: File, discovered: ArrayList<File>) {
		if (dir.isFile()) {
			discovered.add(dir)
		} else if (dir.isDirectory()) {
			val files = dir.listFiles()
			if (files != null) {				
				for (f in files) {					
					bypass(f, discovered)
				}
			}
		} else {
			log.error("Unknown file type of: $dir")
		}
	}

	private class ImgData(val imgPath: String, val imgTimestamp: Int) {
		
	}

}
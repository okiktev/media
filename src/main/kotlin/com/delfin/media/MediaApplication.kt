package com.delfin.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired


@SpringBootApplication
class MediaApplication

fun main(args: Array<String>) {
	Store.load()
	Scanner.start()
	println("$$$ running application")
	runApplication<MediaApplication>(*args)
	println("$$$ running application done")
}

@RestController
class MessageController {
	
	@Autowired
    lateinit var appProperties: AppProperties
	
    @GetMapping("/")
    fun index(@RequestParam("name") name: String) = "Hello, $name! ${appProperties.storeFilePath}"
}

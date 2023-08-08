package com.delfin.media

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties () {
	@Value("\${store.file.path}") lateinit var storeFilePath: String
	@Value("\${store.dump.period.ms}") lateinit var dumpPeriod: String
}
package com.delfin.media

import java.io.InputStream
import java.util.Properties

class AppProps {

	private val properties: Properties = Properties()

	private constructor() {
		val stream: InputStream = javaClass.getClassLoader().getResourceAsStream("application.properties")
		properties.load(stream);
	}

	companion object {

		private val appProps: AppProps = AppProps()

		fun get(property: String, default: String): String = (appProps.properties.get(property)?: default) as String

		fun get(property: String): String? = appProps.properties.get(property) as String

    }

}
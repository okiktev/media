package com.delfin.media

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipException
import java.util.zip.ZipFile

class Md5Hash {

	companion object {
		fun get(file: File): String {
			return Md5Hash().calculate(file)
		}
	}

	private fun calculate(file: File): String {
		var fis: InputStream? = null
		try {
			fis = FileInputStream(file)
			return getBytes(fis).toHex()
		} catch (e: Exception) {
			throw RuntimeException("Couldn't calculate MD5 hashsum for $file.", e)
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (e: IOException) {
					e.printStackTrace()
				}
			}
		}
	}

	private fun getBytes(stream: InputStream): ByteArray {
		val buffer: ByteArray = ByteArray(15000)
		val complete: MessageDigest = MessageDigest.getInstance("MD5")
		var numRead: Int
		do {
			numRead = stream.read(buffer)
			if (numRead > 0) {
				complete.update(buffer, 0, numRead)
			}
		} while (numRead != -1)
		return complete.digest()
	}

}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }



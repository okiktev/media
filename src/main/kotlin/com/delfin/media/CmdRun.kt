package com.delfin.media

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


class CmdRun {

	private val log = LoggerFactory.getLogger(javaClass)

	companion object {
		private val runner = CmdRun()

		fun run(workingDir: File?, vararg command: String): Result = runner.runCmd(workingDir, *command)

	}

	private fun runCmd(workingDir: File?, vararg command: String): Result {
		try {
			val proc = Runtime.getRuntime().exec(command, null, workingDir)
			return handle(proc, OutReader(), ErrReader())
	    } catch(e: IOException) {
	        throw RuntimeException("Unable to run console command ${command.toString()}", e)
	    }
	}

	@Throws(InterruptedException::class)
	private fun handle(process: Process, stdout: ConsoleReader, stderr: ConsoleReader) : Result {
		var out: InputStream? = null
		var err: InputStream? = null
		try {
			out = process.getInputStream()
			err = process.getErrorStream()
			stdout.start(out)
			stderr.start(err)
			return Result(process.waitFor(), stdout.toString(), stderr.toString())
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (e: IOException) {
					log.warn("Unable to close output stream", e)
				}
			}
			if (err != null) {
				try {
					err.close();
				} catch (e: IOException) {
					log.warn("Unable to close error stream", e)
				}
			}
		}
	}

}

class Result(val code: Int, val out: String, val err: String) {

	override fun toString(): String {
		val res = StringBuilder()
		res.append("code:[" + code).append("]\n")
		res.append("stdout:[" + out).append("]\n")
		res.append("stderr:[" + err).append("]\n")
		return res.toString()
	}

}

abstract class ConsoleReader {

	private var reader: BufferedReader? = null
	private val out: StringBuilder = StringBuilder()

	fun start(stream: InputStream) {
		reader = BufferedReader(InputStreamReader(stream))
		val thread: Thread  = Thread(Runnable() {
			try {
				var line = reader?.readLine()
				while (line != null) {
					processLine(line)
					line = reader?.readLine()
				}
			} catch (e: IOException) {
				out.append("\nAn Exception occurred: ").append(e);
			}
		});
		thread.setDaemon(true);
		thread.setName(getThreadName());
		thread.start();
	}

	override fun toString(): String = out.toString()

	protected fun processLine(line: String) {
		out.append(line).append('\n')
	}

	protected abstract fun getThreadName(): String

}

class OutReader: ConsoleReader() {

	protected override fun getThreadName(): String = "Output console reader"

}

class ErrReader: ConsoleReader() {

	protected override fun getThreadName(): String = "Error console reader"

}


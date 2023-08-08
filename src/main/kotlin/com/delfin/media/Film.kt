package com.delfin.media

import java.io.File


public class Film (val name: String
				   , val location: File
				   , val hash: String
				   , val width: Int
				   , val height: Int
				   , val audioTracks: List<String>
				   , var imgPath: String
				   , var imgTimestamp: Int) {

	companion object {
		// TODO can be cached in the map
		fun hash(file: File): String = Md5Hash.get(file)

		fun create(file: File, hash: String, width: Int, height: Int, audioTracks: List<String>, imgPath: String, imgTimestamp: Int): Film {
			return Film(file.getName(), file, hash, width, height, audioTracks, imgPath, imgTimestamp)
		}

		fun create(map: HashMap<String, Any?>): Film {
			return Film((map.get("name")?:"") as String
				, File((map.get("location")?:"") as String)
				, (map.get("hash")?:"") as String
				, (map.get("width")?:0) as Int
				, (map.get("height")?:0) as Int
				, (map.get("audioTracks")?:emptyList<String>()) as List<String>
			    , (map.get("imgPath")?:"") as String
			    , (map.get("imgTimestamp")?:0) as Int
			)
		}
    }

	override fun toString(): String = "Film[name=$name;hash=$hash]"

}
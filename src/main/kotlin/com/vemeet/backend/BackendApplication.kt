package com.vemeet.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendApplication
//TODO handle error for field missing in body
fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}

package com.ollama.springai.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.ollama.springai.services.OllamaService;

@RestController
public class AnswerAnyThingStreamingController {

	@Autowired
	OllamaService service;

}
package com.ollama.springai.text;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ollama.springai.services.OllamaService;


@Controller
public class AnswerAnyThingController {

	@Autowired
    private OllamaService service;

    @GetMapping("/showAskAnything")
    public String showAskAnything() {
         return "askAnything";
    }
    @PostMapping("/askAnything")
    public String askAnything(@RequestParam String question, Model model) {
    	ChatResponse response = service.generateAnswer(question);
    	System.out.println(response);
    	model.addAttribute("question",question);
    	model.addAttribute("answer",response.getResult().getOutput().getText());
        return "askAnything";
    }
}
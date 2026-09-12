package com.ollama.springai.text.prompttemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ollama.springai.services.OllamaService;

@Controller
public class TravelGuideController {
	@Autowired
	private OllamaService service;

	@GetMapping("/showTravelGuide")
	public String showChatPage() {
		return "travelGuide";
	}

	@PostMapping("/travelGuide")
	public String getChatResponse(@RequestParam String city, @RequestParam String month,
			@RequestParam String language, @RequestParam String budget, Model model) {

		String response = service.getTravelGuidance(city,month,language,budget);
		model.addAttribute("city",city);
		model.addAttribute("response",response);
		return "travelGuide";
	}

}

package com.ollama.springai.functioncalling;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class StockRetrievalService implements Function<StockRetrievalService.Request, StockRetrievalService.Response> {

	@JsonClassDescription("Request to look up a stock price by ticker symbol")
	public record Request(
			@JsonProperty(required = true)
			@JsonPropertyDescription("The stock ticker symbol, for example AAPL")
			String symbol) {
	}

	public record Response(Double price) {
	}

	@Override
	public Response apply(Request request) {
		return new Response(5000D);
	}

}

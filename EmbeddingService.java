package com.SpringAi.SpringAi2.service;

import java.net.http.HttpClient;
import java.util.List;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class EmbeddingService {

    private final HttpClient client = HttpClient.newHttpClient();

    public List<Double> getEmbedding(String text) throws Exception{

        String requestBody = """
         {
                        "model": "llama3",
                        "prompt": "%s"
                    }
                    
          """.formatted(
                          text.replace("\\", "\\\"")
                              .replace("\\n", " ")
                      );
                      
                        HttpRequest request = HttpRequest.newBuilder()
                                  .uri(URI.create("http://localhost:11434/api/embeddings"))
                                  .header("Content-Type", "application/json")
                                  .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                  .build();
                      
                        HttpResponse<String> response = client.send(
                                  request,
                                  HttpResponse.BodyHandlers.ofString()
                              );
                              
     JSONObject json = new JSONObject(response.body());
            JSONArray array = json.getJSONArray("embedding");
           
            List<Double> embedding = new ArrayList<>();
                   for (int i = 0; i < array.length(); i++) {
                       embedding.add(array.getDouble(i));
                   }
           
                   return embedding;                  
                      
    }
    
    public double cosineSimilarity(
                List<Double> vec1, List<Double> vec2) {
            double dot = 0, mag1 = 0, mag2 = 0;
            for (int i = 0; i < vec1.size(); i++) {
                dot += vec1.get(i) * vec2.get(i);
                mag1 += Math.pow(vec1.get(i), 2);
                mag2 += Math.pow(vec2.get(i), 2);
            }
            return dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
        }
    

}


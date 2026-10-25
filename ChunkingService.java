package com.SpringAi.SpringAi2.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    public List<String> chunkDocument(

            String text,
            int chunkSize,
            int overlapSize
    ){

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while(start < text.length()){
            int end = Math.min(start + chunkSize, text.length());

            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                if (breakPoint > start) end = breakPoint + 1;
            }

            chunks.add(text.substring(start, end).trim());

            start = end - overlapSize;
            if (start >= end) start = end;
        }

        return chunks;
    }
}

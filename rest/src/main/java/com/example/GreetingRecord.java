package com.example;

import jakarta.json.bind.annotation.JsonbCreator;

import java.time.LocalDateTime;

public record GreetingRecord(String name, LocalDateTime sentAt){}
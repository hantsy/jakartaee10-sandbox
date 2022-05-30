package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonTest {

    @Test
    @DisplayName("Test Person")
    public void test() {
        Person person = new Person();
        person.setName("John");
        person.setAge(30);
        assertEquals("John", person.getName());
        assertEquals(30, person.getAge());

    }
}

package com.example;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Person {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private int age = 30;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.MALE;
    private Integer yearsWorked = 2;
    private LocalDateTime birthDate = LocalDateTime.now().minusYears(30);
    private BigDecimal salary = new BigDecimal("12345.678");
    private BigDecimal hourlyRate = new BigDecimal("34.56");

    public Person() {
    }

    public Person(String name, int age) {
        assert age > 0;
        this.name = name;
        this.age = age;
        this.birthDate = LocalDateTime.now().minusYears(this.age);
    }

    public UUID getId() {
        return id;
    }

    public Integer getYearsWorked() {
        return yearsWorked;
    }

    public void setYearsWorked(Integer yearsWorked) {
        this.yearsWorked = yearsWorked;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDay) {
        this.birthDate = birthDay;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public static enum Gender {
        MALE, FEMALE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return getAge() == person.getAge() &&
                Objects.equals(getId(), person.getId()) &&
                getName().equals(person.getName()) &&
                getGender() == person.getGender() &&
                Objects.equals(getYearsWorked(), person.getYearsWorked()) &&
                getBirthDate().equals(person.getBirthDate()) &&
                Objects.equals(getSalary(), person.getSalary()) &&
                Objects.equals(getHourlyRate(), person.getHourlyRate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getId(),
                getName(),
                getAge(),
                getGender(),
                getYearsWorked(),
                getBirthDate(),
                getSalary(),
                getHourlyRate()
        );
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", yearsWorked=" + yearsWorked +
                ", birthDay=" + birthDate +
                ", salary=" + salary +
                ", hourlyRate=" + hourlyRate +
                '}';
    }
}
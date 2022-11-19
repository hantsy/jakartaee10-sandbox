# What's New in Jakarta Persistence 3.1

Compare to JPA 3.0, JPA 3.1 just brings several small enhancements.

Here we list some highlights in JPA 3.1

## UUID Basic Type Support

Firstly UUID is now treated a basic type. Some JPA providers, such as Hibernate and EclipseLink have supported UUID for a long time, and now it finally becomes part of the standard specification.

For the database that has built-in UUID data type, such as PostgreSQL, an UUID field or property is stored in an UUID data type column in database directly. For those databases do not support UUID, the JPA provider could provide a solution to map an UUID value to a varchar type or an equivalent type in the database.

UUID is usually used as ID of an entity. In JPA 3.0 or the earlier versions, Hibernate and EclipseLink provides their specific generators for UUID. JPA 3.1 introduces a new standard ID generation strategy for UUID type - `UUID`.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID) private UUID id;
```

## Extensions of JPQL and Criteria APIs

JPA 3.1 ports several functions that existed in SQL to JPQL and Criteria APIs, including:

* Numeric functions
* Java 8 DateTime(JSR310) specific functions
* A standardized `EXTRACT` function

### Numeric Functions

SQL standards defines a collection of math functions, in JPA 3.1, several functions are standardized as part of JPQL and Criteria APIs.

| JPQL Function                                       | CriteriaBuilder Method | Description                                                                                                                             |
| --------------------------------------------------- | ---------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| CEILING(arithmetic_expression)                      | ceiling()              | Returns the ceiling of its argument: that is, the smallest integer greater than or equal to its argument.                               |
| EXP(arithmetic_expression)                          | exp()                  | Returns the exponential of its argument: that is, Euler's number e raised to the power of its argument.                                 |
| FLOOR(arithmetic_expression)                        | floor()                | Returns the floor of its argument: that is, the largest integer greater than or equal to its argument.                                  |
| LN(arithmetic_expression)                           | ln()                   | Returns the natural logarithm of its argument.                                                                                          |
| POWER(arithmetic_expression, arithmetic_expression) | power()                | Returns the first argument raised to the power of its second argument.                                                                  |
| ROUND(arithmetic_expression, arithmetic_expression) | round()                | Returns the first argument rounded to the number of decimal places given by the second argument.                                        |
| SIGN(arithmetic_expression)                         | sign()                 | Returns the sign of its argument: that is, 1 if its argument is positive, -1 if its argument is negative, or 0 if its argument is zero. |

### Java 8 DateTime Functions

The existing `CURRENT_TIME`, `CURRENT_DATE`, `CURRENT_TIMPSTAMP` functions are mapped the old `java.util.Date` or `java.sql.Date`. To align with Java 8 DateTime APIs, new functions `LOCAL_DATE`, `LOCAL_TIME` and `LOCAL_DATETIME` are added in JPA 3.1 which return `LocalDate`, `LocalTime` and `LocalDateTime` from the `java.time` package.

| JPQL Function  | CriteriaBuilder Method | Description                                                            |
| -------------- | ---------------------- | ---------------------------------------------------------------------- |
| LOCAL DATE     | localDate()            | Returns current local date as defined by the database server.          |
| LOCAL DATETIME | localDateTime()        | Returns current local date and time as defined by the database server. |
| LOCAL TIME     | localTime()            | Returns current local time as defined by the database server.          |

### `EXTRACT` Function

In SQL, the `extract` function can be applied on a datetime expression or function to return a number that stands for a part of the datetime. This function is now working in JPQL, it can also be used on a Java 8 DateTime field or property.

```sql
EXTRACT(datetime_field FROM datetime_expression)
```

`EXTRACT` returns an integer if the datetime_field is one of the following value.

| DateTime  Field | Description                                                         |
| --------------- | ------------------------------------------------------------------- |
| YEAR            | returns the calendar year.                                          |
| QUARTER         | returns the calendar quarter, numbered from 1 to 4.                 |
| MONTH           | returns the calendar month of the year, numbered from 1.            |
| WEEK            | returns the ISO-8601 week number.                                   |
| DAY             | returns the calendar day of the month, numbered from 1.             |
| HOUR            | returns the hour of the day in 24-hour time, numbered from 0 to 23. |
| MINUTE          | returns the minute of the hour, numbered from 0 to 59.              |

`EXTRACT` returns a float if the datetime_field is `SECOND`, which will return the second of the minute, numbered from 0 to 59, including a fractional part representing fractions of a second.

## Other Improvements

JPA 3.1 also includes some small improvements, such as `EntityManagerFactory` and `EntityManager` now implement `AutoClosable` interface. Thus you can use them in a try-resources block.

```java
try(EntityManagerFactory emf=...) {

}

try(EntityManager entityManager=...) {

}
```

There is [a full list to summarize all changes brought in JPA 3.1](https://projects.eclipse.org/projects/ee4j.jpa/releases/3.1).

In the next sections, we will create a simple Java project with Hibernate and a standard Jakarta Web project to experience the new features in JPA 3.1.

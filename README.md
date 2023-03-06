# spring-boot-logging-json

JSON logging support for Spring Boot.

## Usage

```kotlin
fun main(args: Array<String>) {
    runApplication<ServerApp>(*args) {
        addInitializers(JsonLoggingInitializer())
    }
}
```

## License

spring-boot-logging-json is licensed under [Apache License, Version 2.0](LICENSE).

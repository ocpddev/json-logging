# json-logging

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

json-logging is licensed under [Apache License, Version 2.0](LICENSE).

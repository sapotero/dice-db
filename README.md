
# DiceDB Kotlin Client

[![](https://jitpack.io/v/sapotero/dice-db-client.svg)](https://jitpack.io/#sapotero/dice-db-client)

DiceDB is an open-source, in-memory database that is *reactive* (pushes updates to subscribed queries) and optimized for modern hardware.
This Kotlin client provides a **strongly-typed, coroutine-friendly API** for interacting with DiceDB.

## ✨ Features

- ✅ **Strong Typing:** All commands and responses use Kotlin data and sealed classes.
- ✅ **Coroutine Support:** Non-blocking suspend functions.
- ✅ **Clean API:** DSL-style commands like `Command.Set("k", "v")` with type-safe responses.
- ✅ **Extensible:** Easily add new commands.
- ✅ **Testable:** Full test suite covering edge cases.

---

## 📦 Installation (via JitPack)

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

In your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.sapotero:dice-db-client:<version>")
}
```

Replace `<version>` with a tag, commit hash, or `master-SNAPSHOT`.

---

## 🚀 Example Usage

```kotlin
suspend fun main() {
    val client = Client("localhost", 7379)

    with(client) {
        val setResp: Response.SETRes = fire(Command.Set("k1", "v1"))
        val getResp: Response.GETRes = fire(Command.Get("k1"))
        println("SET: $setResp, GET: $getResp")
    }
}
```

---

## 📘 Supported Commands

All commands are accessible as `Command.Xyz(...)`, returning typed `Response.XyzRes`.

### 🔑 String Commands

- [GET](https://dicedb.io/commands/get/)
- [SET](https://dicedb.io/commands/set/)
- [GETSET](https://dicedb.io/commands/getset/)
- [GETEX](https://dicedb.io/commands/getex/)
- [GETDEL](https://dicedb.io/commands/getdel/)
- [ECHO](https://dicedb.io/commands/echo/)
- [DEL](https://dicedb.io/commands/del/)
- [EXISTS](https://dicedb.io/commands/exists/)
- [EXPIRE](https://dicedb.io/commands/expire/)
- [EXPIREAT](https://dicedb.io/commands/expireat/)
- [EXPIRETIME](https://dicedb.io/commands/expiretime/)
- [FLUSHDB](https://dicedb.io/commands/flushdb/)
- [KEYS](https://dicedb.io/commands/keys/)
- [TTL](https://dicedb.io/commands/ttl/)
- [TYPE](https://dicedb.io/commands/type/)
- [PING](https://dicedb.io/commands/ping/)

### 📈 Numeric Commands

- [INCR](https://dicedb.io/commands/incr/)
- [INCRBY](https://dicedb.io/commands/incrby/)
- [DECR](https://dicedb.io/commands/decr/)
- [DECRBY](https://dicedb.io/commands/decrby/)

### 🧮 Hash Commands

- [HSET](https://dicedb.io/commands/hset/)
- [HGET](https://dicedb.io/commands/hget/)
- [HGETALL](https://dicedb.io/commands/hgetall/)

### 🧠 Reactive (Watch) Commands

- [HANDSHAKE](https://dicedb.io/commands/handshake/)
- [UNWATCH](https://dicedb.io/commands/unwatch/)
- [GET.WATCH](https://dicedb.io/commands/get.watch/)
- [HGET.WATCH](https://dicedb.io/commands/hget.watch/)
- [HGETALL.WATCH](https://dicedb.io/commands/hgetall.watch/)
- [ZCARD.WATCH](https://dicedb.io/commands/zcard.watch/)
- [ZCOUNT.WATCH](https://dicedb.io/commands/zcount.watch/)
- [ZRANGE.WATCH](https://dicedb.io/commands/zrange.watch/)
- [ZRANK.WATCH](https://dicedb.io/commands/zrank.watch/)

### 📊 Sorted Set Commands

- [ZADD](https://dicedb.io/commands/zadd/)
- [ZREM](https://dicedb.io/commands/zrem/)
- [ZCARD](https://dicedb.io/commands/zcard/)
- [ZCOUNT](https://dicedb.io/commands/zcount/)
- [ZPOPMAX](https://dicedb.io/commands/zpopmax/)
- [ZPOPMIN](https://dicedb.io/commands/zpopmin/)
- [ZRANK](https://dicedb.io/commands/zrank/)
- [ZRANGE](https://dicedb.io/commands/zrange/)

---

## 🛠 Command Example

```kotlin
data class Get(val key: String) : Command<Response.GETRes>() {
    override fun toProto() = ProtoCommand("GET", listOf(key))
}
```

All commands are sealed classes and implement a `.toProto()` method to serialize them into the DiceDB wire format.

---

## 🧪 Testing

This client has been tested extensively using integration test cases with real command inputs and expected results. Commands are verified for:

- Correct syntax serialization
- Error handling
- Expiration logic
- Numeric overflows and type mismatches

---

## 📄 License

MIT License.
© 2025 [@sapotero](https://github.com/sapotero) — Contributions welcome.

---

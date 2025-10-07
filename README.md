# BMOReDis

BMOReDis is a lightweight Redis-like in-memory key-value store implemented in Java.  
It supports basic Redis commands, key expiration, and simple integer operations. This project is intended for learning, experimentation, and building understanding of in-memory databases.

---

## 🚀 Features (Work in Progress)
- Basic string key-value storage (`SET`, `GET`, `DEL`)
- Key existence check (`EXISTS`)
- Increment and decrement operations (`INCR`, `INCRBY`, `DECRBY`)
- Key expiration and TTL (`EXPIRE`, `TTL`, `PERSIST`)
- Simple command-line client for testing
- RESP (Redis Serialization Protocol) support

Planned features:
- Persistence using AOF (Append-Only File)
- More complex data types (lists, sets)
- Pub/Sub messaging
- Networking enhancements

---

## 💻 Getting Started

### Prerequisites
- Java 11 or higher
- Maven/Gradle (optional if using IDE)
- Git

### Running the server
1. Clone the repository:
```bash
git clone https://github.com/HarshLad-stack/BMOReDis.git
cd BMOReDis

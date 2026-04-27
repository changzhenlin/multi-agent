# Redis Data Structures

## Key Patterns

| Key | Type | TTL | Owner | Purpose |
|-----|------|-----|-------|---------|
| `chat:context:{sessionId}` | Hash | 24h | `chat-web` | Cached recent session context for orchestration. |
| `ratelimit:user:{userId}` | String | 1min | `chat-web` | Per-user request counter for gateway-compatible rate limiting. |
| `rag:doc:{docId}:chunk:{chunkIdx}` | Hash | none | `agent-rag` | RAG chunk text, embedding bytes, and source metadata. |
| `sql:schema:metadata` | String | 10min | `agent-sql` | Cached database schema metadata as JSON. |

## `chat:context:{sessionId}`

Hash fields:

| Field | Value |
|-------|-------|
| `session_id` | Session id. |
| `user_id` | User id. |
| `messages_json` | JSON array of recent message summaries. |
| `updated_at` | ISO-8601 update time. |

TTL: refresh to 24 hours after every successful chat turn.

## `ratelimit:user:{userId}`

String value: integer request count in the current minute window.

TTL: 60 seconds, set when the key is first incremented.

## `rag:doc:{docId}:chunk:{chunkIdx}`

Hash fields:

| Field | Value |
|-------|-------|
| `doc_id` | Knowledge document id. |
| `chunk_idx` | Zero-based chunk index. |
| `title` | Document title. |
| `source_uri` | Original source URI or internal link. |
| `content` | Chunk text. |
| `embedding` | 1024-dimensional FLOAT32 vector bytes. |

TTL: none. Deleting a document must delete all matching chunk keys.

Vector index: `rag_index`, created by `redis/create-rag-index.redis`.

## `sql:schema:metadata`

String value: compact JSON produced from MySQL `information_schema`.

TTL: 10 minutes. Refresh eagerly after startup and lazily on cache miss.

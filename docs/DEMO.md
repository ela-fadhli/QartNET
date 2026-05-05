# QartNET — Defense Demo Script

**Estimated runtime:** 6–7 minutes
**Modules covered:** Forum, Messaging (real-time), AI assistant
**Out of scope (cut for MVP):** Groups, Search, Notifications

---

## 0. Prerequisites (run once before the defense)

1. **Gemini API key** — get a free one at <https://aistudio.google.com/apikey>.
   Put it in `apps/backend/qartnet/src/main/resources/application-local.properties`:
   ```
   gemini.api-key=YOUR_KEY
   gemini.model=gemini-2.5-flash
   ```
2. **Node 20+** is required by Angular 21. If `node -v` shows older, use nvm:
   `nvm install 22 && nvm use 22`.
3. **Docker Desktop** running (only if you use `dev-up.sh` with the bundled
   Postgres container). If you already run Postgres locally, pass `--no-docker`.

## 1. Bring the stack up

From the repo root:

```bash
./apps/dev-up.sh             # Postgres in docker + backend + frontend
# OR
./apps/dev-up.sh --no-docker # if local Postgres is already on :5432
```

Wait until `tail -f .dev-logs/backend.log` shows `Tomcat started on port 8080`
and `.dev-logs/frontend.log` shows `Local: http://localhost:4200`.

Open two browser windows side by side:

- **Window A** → log in as `yassine.benali@enicarthage.tn` / `Test1234!`
- **Window B** → log in as `mariem.trabelsi@enicarthage.tn` / `Test1234!`

Tear down at the end with `./apps/dev-down.sh`.

---

## 2. Walkthrough

### Step 1 — Tour the Forum (Window A, ~45 s)

1. Click **Forum** in the sidebar.
2. Open **ENICarthage Hub**, then the thread *"Spring Boot + Angular PFE — good stack?"*.
3. Highlight: nested replies, tags, view count, author username, time ago.
4. Click **Reply**, post: *"Adding K8s for the deployment story — anyone tried it locally?"*
5. Verify the reply appears at the bottom.

> **Talking point:** "Forum is the first module — fully end-to-end, with Reddit-style
> sub-forums, member roles, and tag auto-creation."

### Step 2 — Real-time Messaging (Windows A + B, ~90 s)

1. **Window A:** Click **Messaging**. You'll see two seeded conversations.
2. Open the conversation with **mariem_ai**. Show the existing history.
3. **Window B (Mariem's session):** Click **Messaging**. Open the conversation with **yassine_dev**.
4. **Window A:** Type *"Are you free for the architecture sync this afternoon?"* and send.
5. **Pause** so the supervisors see the message arriving in **Window B without a refresh**.
6. **Window B:** Reply *"Yes, 4 PM at the library."* — show **Window A** receives it live.

> **Talking point:** "Delivery uses Spring's STOMP broker over WebSocket with the JWT
> validated on the CONNECT frame. Each send fans out on a per-conversation topic and
> on a per-user queue, so the recipient gets badge updates even when the conversation
> isn't open."

### Step 3 — AI Assistant (Window A, ~2 min)

1. Click the **gold sparkle button** at the bottom-right.
2. Ask: *"What is QartNET and what can I do with it?"*
3. Wait for the answer; highlight that the **system prompt** scopes the assistant to
   the platform.
4. Ask a follow-up that exercises **memory**: *"And how does the messaging module
   work for me as a student?"*
5. Click the **"New"** button (top right of the drawer) → ask another question to
   show that the conversation reset successfully.

> **Talking point:** "The assistant is a stateful, per-user conversational agent.
> Each turn is persisted in `chat_messages` and re-sent to Gemini on every call so
> the model has memory across the session. Failures translate to a graceful 503 with
> a user-friendly message — see `LlmUnavailableException`."

### Step 4 — Tests + architecture (~60 s, optional, only if asked)

In a terminal:

```bash
cd apps/backend/qartnet && ./mvnw test
```

72 tests, all green. Mention the convention split:

- `controller/` returns `ApiResponse<T>` — frontend always unwraps via `.data`.
- `service/` throws domain exceptions, `GlobalExceptionHandler` translates.
- All entities extend `BaseEntity` (UUID `publicId`, timestamps).
- WebSocket auth happens at STOMP CONNECT, reusing the JWT filter.

---

## 3. Pre-validated questions for the AI assistant

If your supervisor asks you to type a question on the spot, fall back to one of
these — they've been tested and produce useful answers:

- *"What is QartNET?"*
- *"How can I use QartNET as a final-year ENICarthage student?"*
- *"Explain what JWT authentication is in one paragraph."*
- *"What's the difference between Spring Boot and Spring Framework?"*
- *"Can you suggest a good study routine before final exams?"*

---

## 4. Known limitations (be honest, disclose first)

- **No "start new conversation" UI** — all conversations come from the seed data
  (a public profile page is needed first; planned for the next sprint).
- **Notifications module deferred** — bell icon in header is decorative for now.
- **Groups + Search dropped from MVP** to fit the timeline; spec keeps them in scope.
- **AI is a generic chatbot** with QartNET-aware system prompt + persistent
  memory. RAG over forum threads is the natural next iteration.

## 5. Recovery plan

- **AI assistant fails live:** show the *3-min screen recording* you produced ahead
  of the defense.
- **WebSocket disconnects:** the page polls history on conversation switch — refresh
  the messaging tab and continue.
- **Backend crash:** `./apps/dev-down.sh && ./apps/dev-up.sh` (DB volume persists).

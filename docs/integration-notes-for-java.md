# Integration Notes — Gateway → AI Engine

Notes for the Java side on connecting to the Python AI engine. Written from
the Python side after reading through the current gateway code
(`93bdcf1`) — nothing here has been changed on the Java side, this is just
what's needed to wire the two services together.

The full request/response contract is in
[`docs/contracts/ai-engine.openapi.yaml`](./contracts/ai-engine.openapi.yaml).
This file is the plain-English version of the same thing, plus what's
missing on each side to make it real.

---

## What the AI engine already does (Python side, working today)

- `POST /api/v1/jobs` — accepts a job, builds a use-case prompt, runs the
  (currently stubbed) image edit, stores the result in MinIO, returns a
  presigned URL.
- `GET /api/v1/jobs/{job_id}` — looks up a job and returns a **freshly
  generated** presigned URL each time (not a stored one, so it never goes
  stale).
- Both verified against real Postgres + MinIO, including as a built Docker
  image (`services/ai-engine/Dockerfile`).

This currently uses its **own** Postgres `jobs` table — see "Decisions
needed" below, that's expected to go away once the contract is final.

---

## What's needed on the Java side to actually connect

1. **Call the AI engine from `UploadController`.** Right now
   `AI_ENGINE_URL` is configured in `application.yml` but nothing calls it —
   `upload()` stores the photo and creates a `PENDING` job, then just
   returns. It needs to also `POST` to the AI engine (per the contract:
   `job_id`, `input_image_key`, `use_case`, and whatever `StyleAdvisorService`
   produces).

2. **Add a callback endpoint for the AI engine to report back.**
   `JobController` only has `GET /{id}`. `JobService.updateStatus()` /
   `markDone()` already exist and look ready for this — they just need an
   HTTP entry point (e.g. `PUT /api/v1/jobs/{id}/callback`) for the AI
   engine to call when a job finishes.

3. **Generate a presigned URL before returning job status to the frontend.**
   `JobController.getJob()` currently returns `outputImageKey` — a raw
   MinIO key, not a URL the frontend can actually load. `StorageService`
   has no presigned-URL method yet. (Same pattern Python already uses for
   this, if useful as reference: generate it fresh per-request rather than
   storing a URL that can expire.)

4. **Auth currently blocks the whole upload flow.** `SecurityConfig`
   disables both `httpBasic` and `formLogin` with no JWT filter in place,
   but `anyRequest().authenticated()` still applies to everything except
   `/health` and `/auth/**`. `UploadController` depends on
   `@AuthenticationPrincipal UserDetails`, which has nothing populating it
   right now — so the upload endpoint is unreachable until real
   session/JWT auth is wired in. Not related to the AI engine integration,
   but it'll block any end-to-end test of the full flow until it's fixed.

---

## Decisions still open (need both of us to agree)

- **Who owns the job ID?** `Job.java` already generates a UUID on upload.
  The AI engine should almost certainly accept that ID rather than minting
  its own — otherwise there are two IDs for one job. If we agree on this,
  Python's standalone `jobs` table goes away and Java's `Job` entity
  becomes the single source of truth.
- **Who owns the final prompt text?** `StyleAdvisorService.java` already
  builds a style description per use-case in Java. Python's
  `app/services/prompt.py` currently does the same thing independently.
  Need to decide: does Python just use Java's text directly, or treat it
  as extra context alongside its own prompt engineering?
- **`use_case` casing** — Java uses uppercase (`PASSPORT`), the contract
  draft has been written to match. Just flagging so it doesn't get missed
  if either side's enum changes later.
- **MinIO key convention for outputs** — Java prefixes uploads as
  `uploads/{uuid}-{filename}`. Suggest Python mirrors this with
  `outputs/{job_id}.jpg` so the bucket stays organized. Not load-bearing,
  just a suggestion.

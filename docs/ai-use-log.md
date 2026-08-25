# AI Use Log

This project used AI assistance (Cursor Agent) for the following:

| Date | Area | What AI did | Human decision |
|------|------|-------------|----------------|
| 2026-08-25 | Planning | Reviewed assessment docs; identified commit-plan ritual, auth/concurrency gaps, analytics median issue | Kept product scope; rewrote remaining-work plan as ~18 vertical-slice commits |
| 2026-08-25 | Backend | Flyway wiring, Testcontainers, versioned salary PUT, native SQL median, session auth, chunked seed, health endpoint | Chose PUT over PATCH to avoid API churn; UTC for "today" |
| 2026-08-25 | Frontend | Login, directory, create, detail/history/salary+409, analytics components | OnPush + Signals per architecture doc |
| 2026-08-25 | Delivery | Docker multi-stage build, Render config, CI pipeline | Render Free + Neon Free per architecture |

All domain rules, test assertions, and deployment trade-offs were reviewed before commit.

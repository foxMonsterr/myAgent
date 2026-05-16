---
name: qdrant-skills
description: Project-local guidance for working with Qdrant-style vector search, embeddings, and retrieval workflows in this repo.
---

# Qdrant Skills

Use this skill when working on retrieval-augmented generation, vector search, embeddings, or any Qdrant-related integration in this project.

## What to do
- Keep retrieval logic consistent with the existing Spring AI and `SimpleVectorStore` setup unless the user explicitly asks to switch to Qdrant.
- Prefer minimal, reviewable changes when introducing vector search abstractions.
- If adding a real Qdrant integration, update configuration, startup docs, and any health or deployment checks that depend on it.
- Verify that request/response models, controllers, and front-end API bindings stay aligned.

## Repo-specific notes
- Backend lives under `src/main/java` and uses Spring Boot 3.3.x.
- Configuration is under `src/main/resources`.
- Frontend API and views live under `front/src`.
- Avoid introducing new infrastructure dependencies unless they are necessary for the requested change.

## Validation checklist
- Run or update relevant tests when possible.
- Check for configuration mismatches in `application*.yml`.
- If the change affects deployment, confirm Docker and documentation updates are included.

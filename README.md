# bluestaq_assessment

See below for API documentation

## Notes

- delete and update not currently atomic
- no time for note privacy: if you know the team name you can look up all their notes
- "teamName" in URL params is stand-in for being logged in
- ideal model: users belong to teams, notes belong to users
  - user notes can be shared with a team, team notes can be shared with other teams
- would like to reduce code reuse
  - don't need different models for create, update, etc. if I figure out how to get Spring to make some of the JSON
    params optional
- no reason for notes returned to client to contain "sharedWith" field
- status codes are revealing information about which records exist even if requesting team doesn't have access

# Notes API

A REST API for team-based note taking. Notes belong to a single owning team and can optionally be shared with other teams.

All request and response bodies are JSON. Send `Content-Type: application/json` on requests with a body.

## Access rules

| Action | Owning team | Shared team | Other teams |
|--------|-------------|-------------|-------------|
| View   | Yes         | Yes         | No (404)    |
| Update | Yes         | Yes         | No (404)    |
| Delete | Yes         | No (403)    | No (404)    |

A team with no access gets `404` so it cannot tell whether a note exists.

## Models

### Team

| Field      | Type        | Description                             |
|------------|-------------|-----------------------------------------|
| `teamName` | string      | Unique team name (primary key)          |
| `noteIds`  | UUID[]      | IDs of the notes this team owns         |

### Note

| Field        | Type     | Description                                              |
|--------------|----------|----------------------------------------------------------|
| `id`         | UUID     | Unique note ID (primary key)                             |
| `title`      | string   | Note title. Required, cannot be blank                    |
| `content`    | string   | Note body                                                |
| `sharedWith` | string[] | Names of teams allowed to view and update (not delete)   |

---

## Teams

### Create a team

`POST /teams/create`

**Body**

```json
{ "teamName": "alpha" }
```

**Responses**

| Status | Meaning                          |
|--------|----------------------------------|
| 201    | Team created, returns the `Team` |
| 400    | `teamName` missing or blank      |
| 409    | `teamName` already taken         |

**Example**

```bash
curl -X POST http://localhost:8080/teams/create \
  -H "Content-Type: application/json" \
  -d '{"teamName": "alpha"}'
```

```json
{ "teamName": "alpha", "noteIds": [] }
```

---

## Notes

### Create a note

`POST /notes/create?teamName={teamName}`

Creates a note owned by `teamName`.

**Query parameters**

| Name       | Type   | Required | Description             |
|------------|--------|----------|-------------------------|
| `teamName` | string | Yes      | Team that will own the note |

**Body**

```json
{
  "title": "plan",
  "content": "draft",
  "sharedWith": ["beta"]
}
```

`content` and `sharedWith` are optional. Every team in `sharedWith` must already exist.

**Responses**

| Status | Meaning                                              |
|--------|------------------------------------------------------|
| 201    | Note created, returns the `Note`                     |
| 400    | Title missing or blank, or a shared team doesn't exist |
| 404    | Team `teamName` does not exist                       |
| 409    | A note with that `id` already exists                 |

**Example**

```bash
curl -X POST "http://localhost:8080/notes/create?teamName=alpha" \
  -H "Content-Type: application/json" \
  -d '{"title": "plan", "content": "draft", "sharedWith": ["beta"]}'
```

---

### Get a note

`GET /notes/get?teamName={teamName}&id={id}`

**Query parameters**

| Name       | Type   | Required | Description                          |
|------------|--------|----------|--------------------------------------|
| `teamName` | string | Yes      | Team making the request              |
| `id`       | UUID   | Yes      | ID of the note                       |

**Responses**

| Status | Meaning                                                      |
|--------|--------------------------------------------------------------|
| 200    | Returns the `Note`                                           |
| 400    | `id` is not a valid UUID                                     |
| 404    | Team or note not found, or the team has no access to the note |

**Example**

```bash
curl "http://localhost:8080/notes/get?teamName=beta&id=3f2b8c1e-5a7d-4e0b-9c6a-1d2e3f4a5b6c"
```

---

### Update a note

`PUT /notes/update?teamName={teamName}&id={id}`

Updates the title and/or content. Fields that are omitted (or `null`) are left unchanged. Allowed for the owning team and shared teams.

**Query parameters**

| Name       | Type   | Required | Description             |
|------------|--------|----------|-------------------------|
| `teamName` | string | Yes      | Team making the request |
| `id`       | UUID   | Yes      | ID of the note          |

**Body**

```json
{ "title": "new title", "content": "new content" }
```

**Responses**

| Status | Meaning                                                      |
|--------|--------------------------------------------------------------|
| 200    | Returns the updated `Note`                                   |
| 400    | Title is blank, or `id` is not a valid UUID                  |
| 404    | Team or note not found, or the team has no access to the note |

**Example**

```bash
curl -X PUT "http://localhost:8080/notes/update?teamName=alpha&id=3f2b8c1e-5a7d-4e0b-9c6a-1d2e3f4a5b6c" \
  -H "Content-Type: application/json" \
  -d '{"content": "edited"}'
```

---

### Delete a note

`DELETE /notes/delete?teamName={teamName}&id={id}`

Deletes the note and removes its ID from the owning team's `noteIds`. Only the owning team may delete.

**Query parameters**

| Name       | Type   | Required | Description             |
|------------|--------|----------|-------------------------|
| `teamName` | string | Yes      | Team making the request |
| `id`       | UUID   | Yes      | ID of the note          |

**Responses**

| Status | Meaning                                                  |
|--------|----------------------------------------------------------|
| 204    | Note deleted                                             |
| 400    | `id` is not a valid UUID                                 |
| 403    | Team can view the note (shared) but does not own it      |
| 404    | Team or note not found, or the team has no access        |

**Example**

```bash
curl -X DELETE "http://localhost:8080/notes/delete?teamName=alpha&id=3f2b8c1e-5a7d-4e0b-9c6a-1d2e3f4a5b6c"
```
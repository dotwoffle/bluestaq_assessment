# bluestaq_assessment

Requirements: JDK >25, `mvn`

Run with `mvn spring-boot:run`. Launches a local web server running at `http://localhost:8080`. See below for API and
endpoint documentation. Bruno collection for mock API calls provided. Database persists across runs and is stored in
`./data/notesdb.mv.db`.

## General Structure

I chose Java Spring Boot to implement this REST API. Java is my strongest language and I am most familiar with Spring as
a web backend. Spring has a built-in H2 database that allowed me to develop quickly, but if this were to be made for
real, I would probably opt for a SQLite or MySQL database to connect to. All relevant code is in
`src/main/java/com/dotwoffle/bluestaq_assessment`. I split code into packages for data models (`model`), database schema
and access (`database`), and API control (`api`). The bulk of the REST controller code is in
(`api/ApiRestController.java`).

## Design Decisions

**Teams:** I interpreted the prompt in this way: Individual teams want to have collections of notes shared among members
of the team, and teams may want to share some of their notes with other teams. I designed the API with the goal in mind
that notes must have access restricted based on team. Currently, the API requires that notes be created with an
associated team. When operating on an existing note, the team name of the team that owns the note must be provided to be
allowed to do anything with it. I pass the team name as a URL parameter as a stand-in for a theoretical, more robust
login and authentication system. Notes can be shared among teams, and other teams with access to a note can update it,
but only the owning team can delete a note. I thought this was a good balance between the spirit of collaboration and
total note protection.

**Database Schema:** I tried my best to follow good relational database practices and get to somewhat normal form,
although Spring handled most of the hard work for me. The teams table does not store the entire contents of notes.
Instead, it stores the primary key UUID of each note it owns, and the notes are in a separate table and do not care
about what team owns them (although they store which teams they are shared with). I would have made teams have UUIDs as
primary keys as well if I had more time.

**Users:** I decided not to add a concept of individual users to this iteration of the API. Based on the way I
interpreted the prompt, individual users are overkill when the point is to share notes among a team (ie. a team is a
"workspace" containing notes relevant to whatever task is at hand). If users were to be added in, the structure would
mirror how notes already work with teams: users would own notes, but could share notes with a single team. It wouldn't
be hard to implement but seemed redundant for this exercise.

## Other Notes

- delete and update not currently atomic, need transaction protection
- I had no time for authentication: if you know the team name you can look up all their notes
- would like to reduce code reuse and to organize controller better
  - don't need different models for create, update, etc. if I figure out how to get Spring to make some of the JSON
    params optional
  - some helper methods in the rest controller could go into the models themselves
  - model code could be cleaned up, no need to make fields public for most models
- no reason for notes returned to client to contain "sharedWith" field
- status codes are revealing information about which records exist even if requesting team doesn't have access
- UUIDs should be generated on backend for new notes created by frontend
- I would love to put good javadoc on my classes and methods if I had more time :)

# Notes API Documentation

A REST API for team-based note taking. Notes belong to a single owning team and can optionally be shared with other
teams.

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
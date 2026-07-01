---
name: release
description: Bump the sol4k library version everywhere, open a release PR, and draft GitHub release notes. Use when the user wants to release/cut a new version (e.g. "release 0.8.0", "bump version to X"). Updates gradle.properties and all docs/README*.md dependency snippets, creates a branch and PR, and drafts release notes from the commits since the last version.
---

# Release a new sol4k version

Bumps the project version across every file that hardcodes it and opens a
release PR, mirroring past release PRs (e.g. #184, #202).

## Inputs

- **New version** (required), e.g. `0.8.0`. Ask the user if not provided.
- Determine the **current version** from `gradle.properties`
  (`currentVersion=...`).

## Files to update

The version string appears in exactly these files — replace the current
version with the new one in each:

1. `gradle.properties` — the `currentVersion=` property.
2. `docs/README.md` — the Gradle `implementation 'org.sol4k:sol4k:X'` line and
   the Maven `<version>X</version>` line.
3. `docs/README_JP.md` — same two lines.
4. `docs/README_KR.md` — same two lines.
5. `docs/README_ZH.md` — same two lines.

Do not touch test data, changelogs, or any other `0.x.y` occurrences — only the
five files above carry the published version.

## Release notes

After the PR is opened, draft the GitHub release notes so the user can copy them
when publishing the release (the release is created manually on GitHub, not by
this skill). Releases are GitHub Releases whose tag is the bare version (e.g.
`0.8.0`) with a title of `<version> - <short summary>` and a body of bullet
points — match the style of past releases (`gh release view 0.8.0`).

Build the notes from the commits between the previous version tag and the tip of
`main` (exclude the version-bump commit itself):
```shell
git log <old-version>..origin/main --oneline
```
Turn each meaningful commit into a bullet, dropping the `(#NNN)` PR suffix and
rewording terse commit subjects into user-facing changes. Pick a concise title
summary from the most significant change (e.g. a Kotlin bump, a new API).

Present the result as a ready-to-copy block, e.g.:
```
Title: 0.8.1 - Configurable RPC transport

- Make the RPC transport configurable and add support for custom headers
```

## Steps

1. Read the current version from `gradle.properties`.
2. Create a branch named `<new-version>-release` (e.g. `0.8.0-release`). If the
   user is already on such a branch and just wants the edits committed there,
   skip branch creation.
3. Replace the old version with the new one in the five files above. A quick way:
   ```shell
   OLD=<current>; NEW=<new>
   sed -i '' "s/$OLD/$NEW/g" gradle.properties docs/README.md docs/README_JP.md docs/README_KR.md docs/README_ZH.md
   ```
   (On Linux use `sed -i` without the `''`.)
4. Verify with `git diff` that exactly 9 lines changed (1 in gradle.properties,
   2 in each of the 4 READMEs).
5. Commit:
   ```
   Update version to <new-version>

   Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
   ```
6. Push the branch and open a PR with `gh pr create`:
   - Title: `<new-version> release`
   - Body (matching prior release PRs):
     ```
     Version bump from <old> to <new> across all configuration and documentation files.

     **Changes:**
     - `gradle.properties`: Updated `currentVersion` property
     - `docs/README*.md`: Updated version strings in Gradle and Maven dependency examples (English, Japanese, Korean, Chinese variants)
     ```
   - End the body with the standard Claude Code attribution line.
7. Draft the GitHub release notes (see **Release notes** above) from
   `git log <old-version>..origin/main --oneline`, and present them to the user
   as a ready-to-copy title + body block for when they publish the release.

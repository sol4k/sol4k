---
name: release
description: Bump the sol4k library version everywhere and open a release PR. Use when the user wants to release/cut a new version (e.g. "release 0.8.0", "bump version to X"). Updates gradle.properties and all docs/README*.md dependency snippets, then creates a branch and PR.
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

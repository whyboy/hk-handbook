# Packaged guide content

Place reviewed `<id>.json` files here. Only `content/*.json` in the application classpath is loaded, once at startup. Empty content is valid; do not copy internal drafts or synthetic test fixtures into this directory to populate production results.

The JSON contract is `docs/content/MODEL.md`. Structural errors fail startup with a file and field location. Structurally valid but ineligible documents remain hidden. Eligibility is recalculated on each request in the Asia/Hong_Kong timezone. Editing files requires rebuilding and restarting through the existing authorized release workflow.

Editorial ordering is maintained in `recommendations.txt`: one stable guide ID per line, in priority order; blank lines and `#` comments are ignored. Duplicate or malformed IDs fail startup. Missing or ineligible IDs are ignored during queries and never expose drafts. Unlisted guides follow publication date descending and ID ascending. Keyword queries use relevance instead of editorial ordering. No popularity metrics are collected. Rebuild and deploy to change ordering.

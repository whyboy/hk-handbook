# Packaged guide content

Place reviewed `<id>.json` files here. Only `content/*.json` in the application classpath is loaded, once at startup. Empty content is valid; do not copy internal drafts or synthetic test fixtures into this directory to populate production results.

The JSON contract is `docs/content/MODEL.md`. Structural errors fail startup with a file and field location. Structurally valid but ineligible documents remain hidden. Eligibility is recalculated on each request in the Asia/Hong_Kong timezone. Editing files requires rebuilding and restarting through the existing authorized release workflow.

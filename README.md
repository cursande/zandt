# Zandt

A Clojure application for processing, analysing and presenting Telegram export data.

For now it just processes it through to SQLite, and results can be analysed in the REPL.

Future: 

- Speed up chat processing times by importing data into SQL per user in parallel
- Output to file via different formats
- Pluck out other things that are interesting: images, links, stickers etc
- Display results in web UI

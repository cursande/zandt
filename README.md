# Zandt

A Clojure application for processing, analysing and presenting Telegram export data.

For now it just processes it through SQLite until the schema/concept is a little
more settled.

## Installation

Download from https://github.com/cursande/zandt

## Usage

FIXME: explanation

    $ java -jar zandt-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### TODO:
- Order and list most frequently used words
- Order and list most frequently used emojis
- Count messages and display proportion of total messages
  belonging to a given user
- Persist these values in a SQLite db:
  - User table for the user ids
  - Message table for each individual message
  - Word table for every different kind of word
  - Emoji table which we insert into when we come across emojis


## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

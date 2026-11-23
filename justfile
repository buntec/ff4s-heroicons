# Link the examples and serve them at http://127.0.0.1:8080/ (e.g. `just serve-examples 2.13`)
serve-examples scala="3":
    sbt '++ {{scala}}' examples/fastLinkJS
    python3 -m http.server 8080 --bind 127.0.0.1 --directory examples

# Recompile the examples on every source change (e.g. `just watch-examples 2.13`)
watch-examples scala="3":
    sbt '++ {{scala}}' '~examples/fastLinkJS'

# Install the JavaScript dependencies (heroicons, and jsdom for the tests) from package-lock.json
install:
    npm ci

# Regenerate the icon definitions from node_modules/heroicons (run `just install` first)
generate:
    sbt generateHeroicons

# Run the test suite in jsdom (e.g. `just test 2.13`; run `just install` first)
test scala="3":
    sbt '++ {{scala}}' ff4s-heroicons/test

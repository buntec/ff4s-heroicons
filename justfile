# Link the examples and serve them at http://127.0.0.1:8080/ (e.g. `just serve-examples 2.13`);
# the page reloads whenever the bundle is relinked, e.g. by `just watch-examples`
serve-examples scala="3":
    sbt '++ {{scala}}' examples/fastLinkJS
    npm exec vite -- examples --host 127.0.0.1 --port 8080 --strictPort

# Recompile the examples on every source change (e.g. `just watch-examples 2.13`)
watch-examples scala="3":
    sbt '++ {{scala}}' '~examples/fastLinkJS'

# Install the JavaScript dependencies (heroicons, jsdom for the tests, Vite for the examples) from package-lock.json
install:
    npm ci

# Regenerate the icon definitions from node_modules/heroicons (run `just install` first)
generate:
    sbt generateHeroicons

# Run the test suite in jsdom (e.g. `just test 2.13`; run `just install` first)
test scala="3":
    sbt '++ {{scala}}' ff4s-heroicons/test

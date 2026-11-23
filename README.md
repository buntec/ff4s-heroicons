# ff4s-heroicons

[Heroicons](https://heroicons.com/) (v2.2.0) for [ff4s](https://github.com/buntec/ff4s).

```scala
libraryDependencies += "io.github.buntec" %%% "ff4s-heroicons" % "0.1.0"
```

Mix `ff4s.heroicons.Heroicons[State, Action]` into your ff4s app to get the
icons in four variants: `heroicons.outline` (24x24), `heroicons.solid` (24x24),
`heroicons.mini` (20x20) and `heroicons.micro` (16x16). Each variant also has
an `all` list of `(name, icon)` pairs.

```scala
import heroicons.*

div(outline.`academic-cap`.withClass("w-6 h-6"), micro.`x-mark`)
```

## Development

Requires [just](https://github.com/casey/just), sbt and Node.js.

- `just install`: install heroicons and jsdom from `package-lock.json`
- `just generate`: regenerate the icon definitions from `node_modules/heroicons`
- `just test [scala]`: run the tests in jsdom, e.g. `just test 2.13`
- `just serve-examples [scala]`: serve the examples at http://127.0.0.1:8080/
- `just watch-examples [scala]`: recompile the examples on every change

To update Heroicons, bump `heroicons` in `package.json`, run `npm install` and
`just generate`, and commit the result. CI checks that the committed icons match
the generator output.

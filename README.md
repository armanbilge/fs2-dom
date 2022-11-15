# fs2-dom

Idiomatic [Cats Effect] and [FS2] integrations for [Web APIs] via [scala-js-dom].

[Cats Effect]: https://typelevel.org/cats-effect/
[FS2]: https://fs2.io/
[Web APIs]: https://developer.mozilla.org/en-US/docs/Web/API
[scala-js-dom]: https://github.com/scala-js/scala-js-dom

## Usage

```scala
libraryDependencies += "com.armanbilge" %%% "fs2-dom" % "0.1.0-M1"
```

## Features

- [`ReadableStream`] conversions
- [`EventTarget`] listeners
- Lifecycle-managed [`AbortSignal`]s
- Wrappers for [`Clipboard`], [`History`], [`Lock`], [`Storage`] APIs
- [`Serializable`] typeclass
- Contributions welcome!

[`ReadableStream`]: https://developer.mozilla.org/en-US/docs/Web/API/ReadableStream
[`EventTarget`]: https://developer.mozilla.org/en-US/docs/Web/API/EventTarget
[`AbortSignal`]: https://developer.mozilla.org/en-US/docs/Web/API/AbortSignal
[`Clipboard`]: https://developer.mozilla.org/en-US/docs/Web/API/Clipboard_API
[`History`]: https://developer.mozilla.org/en-US/docs/Web/API/History
[`Lock`]: https://developer.mozilla.org/en-US/docs/Web/API/Web_Locks_API
[`Storage`]: https://developer.mozilla.org/en-US/docs/Web/API/Storage
[`Serializable`]: https://developer.mozilla.org/en-US/docs/Glossary/Serializable_object

package fs2.dom

import org.scalajs.dom

final case class EventListenerOptions(capture: Boolean,
                                      once: Boolean,
                                      passive: Boolean)

object EventListenerOptions {
  lazy val default = EventListenerOptions(
    capture = false,
    once = false,
    passive = false
  )
}

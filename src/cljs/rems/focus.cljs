(ns rems.focus
  "Focuses an HTML element as soon as it exists."
  (:require [rems.util :refer [visibility-ratio]]))

(defn- scroll-below-navigation-menu
  "Scrolls an element into view if it's behind the navigation menu."
  [element]
  (when-let [navbar (.querySelector js/document ".fixed-top")]
    (let [navbar-height (.-height (.getBoundingClientRect navbar))
          element-top (.-top (.getBoundingClientRect element))]
      (when (< element-top navbar-height)
        (.scrollBy js/window 0 (- element-top navbar-height))))))

(defn focus-element-async
  "Focus an element when it appears. Options can include:
    :tries -- number of times to poll, defaults to 100"
  [selector & [options]]
  (let [tries (get options :tries 100)]
    (when (pos? tries)
      (if-let [element (.querySelector js/document selector)]
        (do
          (.setAttribute element "tabindex" "-1")
          ;; Focusing the element scrolls it into the viewport, but
          ;; it can still be hidden behind the navigation menu.
          (.focus element)
          (scroll-below-navigation-menu element))
        (js/setTimeout #(focus-element-async selector (assoc options :tries (dec tries)))
                       10)))))

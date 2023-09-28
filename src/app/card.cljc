(ns app.card
  (:require
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(e/defn Card
  [body & [style]]
  (dom/div
    (dom/style style)
    (dom/props
     {:class "card"})
    (new body)))

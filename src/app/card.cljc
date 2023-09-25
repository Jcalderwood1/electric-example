(ns app.card
  (:require
   [app.color :as color]
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def card-style
  {:text-shadow      style/text-shadow
   :background-color (color/color :base)
   :border           "none"
   :border-radius    "12px"
   :box-shadow       style/box-shadow
   :margin           "20px 30px"
   :font-size        "18px"
   :outline          "none"
   :padding          "20px"})

(e/defn Card
  [body & [style]]
  (dom/div
    (dom/style
     (merge card-style style))
    (body.)))

(ns app.card
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def card-style
  {:color            "#394a56"
   :text-shadow      "1px 1px 2px #ffffff, -1px -1px 2px #d1d9e6"
   :background-color "#ecf0f3"
   :border           "none"
   :border-radius    "12px"
   :box-shadow       "7px 7px 14px #d1d9e6, -7px -7px 14px #ffffff"
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

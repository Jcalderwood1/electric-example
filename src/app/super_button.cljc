(ns app.super-button
  (:require
   contrib.str
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def button-style
  {:color            "#8C8C8C"
   :text-shadow      "1px 1px 2px #ffffff, -1px -1px 2px #b8b8b8"
   :cursor           "pointer"
   :width            "140px"
   :height           "100px"
   :background-color "#e0e0e0"
   :border           "none"
   :border-radius    "12px"
   :box-shadow       "7px 7px 14px #b8b8b8, -7px -7px 14px #ffffff"
   :padding          "10px 20px"
   :font-size        "18px"
   :outline          "none"})

(def button-active-style
  (merge button-style
         {:box-shadow "inset 5px 5px 10px #b8b8b8, inset -5px -5px 10px #ffffff"}))

(e/defn SuperButton
  [on-click body & [style]]
  (let [up   (merge button-style style)
        down (merge button-active-style style)]
    (ui/button
     on-click
     (dom/style up)
     (dom/on "mousedown"  (e/fn [e] (.preventDefault e) (dom/style down)))
     (dom/on "mouseup"    (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseleave" (e/fn [e] (.preventDefault e) (dom/style up)))
     (new body))))

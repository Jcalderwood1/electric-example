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

(def button-hover-style
  (merge button-style
         {:color "white"}))

(def button-active-style
  (merge button-style
         {:box-shadow "inset 5px 5px 10px #b8b8b8, inset -5px -5px 10px #ffffff"}))

(e/defn SuperButton
  [on-click body & [style]]
  (let [up    (merge button-style style)
        down  (merge button-active-style style)
        hover (merge button-hover-style style)]
    (ui/button
     on-click
     (dom/style up)
     (dom/on "mouseover"  (e/fn [e] (.preventDefault e) (dom/style hover)))
     (dom/on "mousedown"  (e/fn [e] (.preventDefault e) (dom/style down)))
     (dom/on "mouseup"    (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseleave" (e/fn [e] (.preventDefault e) (dom/style up)))
     (new body))))

(def purple-button-style
  {:background-color "#a593e0"
   :border           "none"
   :border-radius    "15px"
   :padding          "10px 20px"
   :color            "#FFF"
   :text-shadow      "1px 1px 2px #d9d0f0, -1px -1px 2px #5a4875"
   :font-size        "18px"
   :box-shadow       "7px 7px 14px #b8b8b8, -7px -7px 14px #ffffff"
   :outline          "none"
   :cursor           "pointer"})

(def purple-button-hover-style
  (merge purple-button-style
         {:background-color "#9a79db"
          :color            "#fff"}))

(def purple-button-active-style
  (merge purple-button-style
         {:box-shadow "inset 2px 2px 4px rgba(154, 121, 219, 0.5), inset -2px -2px 4px rgba(255, 255, 255, 0.5)"}))

(e/defn CallToActionButton
  [on-click body & [style]]
  (let [up    (merge purple-button-style style)
        down  (merge purple-button-active-style style)
        hover (merge purple-button-hover-style style)]
    (ui/button
     on-click
     (dom/style up)
     (dom/on "mousedown"  (e/fn [e] (.preventDefault e) (dom/style down)))
     (dom/on "mouseup"    (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseleave" (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseenter" (e/fn [e] (.preventDefault e) (dom/style hover)))
     (new body))))

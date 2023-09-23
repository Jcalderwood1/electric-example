(ns app.super-button
  (:require
   contrib.str
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def button-style
  {:color            "#394a56"
   :text-shadow      "1px 1px 2px #ffffff, -1px -1px 2px #d1d9e6"
   :cursor           "pointer"
   :width            "120px"
   :height           "80px"
   :background-color "#ecf0f3"
   :border           "none"
   :border-radius    "12px"
   :box-shadow       "7px 7px 14px #d1d9e6, -7px -7px 14px #ffffff"
   :padding          "10px 20px"
   :font-size        "18px"
   :outline          "none"
   :display          "flex"
   :justify-content  "center"
   :align-items      "center"})

(def button-hover-style
  (merge button-style
         {:color "white"}))

(def button-active-style
  (merge button-style
         {:box-shadow "inset 5px 5px 10px #d1d9e6, inset -5px -5px 10px #ffffff"}))

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

(def cta-button-style
  {:background-color "#DC5078"
   :border           "none"
   :border-radius    "15px"
   :padding          "10px 20px"
   :color            "#FFF"
   :text-shadow      "1px 1px 2px #B43C62, -1px -1px 2px #5a4875" ; Darker shade for text-shadow
   :font-size        "18px"
   :box-shadow       "7px 7px 14px #b8b8b8, -7px -7px 14px #ffffff"
   :outline          "none"
   :cursor           "pointer"})

(def cta-button-hover-style
  (merge cta-button-style
         {:background-color "#C4476F"
          :color            "#fff"}))

(def cta-button-active-style
  (merge cta-button-style
         {:box-shadow "inset 2px 2px 4px rgba(192, 72, 104, 0.5), inset -2px -2px 4px rgba(255, 255, 255, 0.5)"}))

(def cta-button-style
  {:background-color "#DC5078"
   :border           "none"
   :border-radius    "15px"
   :padding          "10px 20px"
   :color            "#FFF"
   :text-shadow      "1px 1px 2px #B43C62, -1px -1px 2px #5a4875"
   :font-size        "18px"
   :box-shadow       "7px 7px 14px #d1d9e6, -7px -7px 14px #ffffff"
   :outline          "none"
   :cursor           "pointer"})

(def cta-button-hover-style
  (merge cta-button-style
         {:background-color "#C4476F"
          :color            "#fff"}))

(def cta-button-active-style
  (merge cta-button-style
         {:box-shadow "inset 2px 2px 4px rgba(192, 72, 104, 0.5), inset -2px -2px 4px rgba(255, 255, 255, 0.5)"}))


(e/defn CallToActionButton
  [on-click body & [style]]
  (let [up    (merge cta-button-style style)
        down  (merge cta-button-active-style style)
        hover (merge cta-button-hover-style style)]
    (ui/button
     on-click
     (dom/style up)
     (dom/on "mousedown"  (e/fn [e] (.preventDefault e) (dom/style down)))
     (dom/on "mouseup"    (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseleave" (e/fn [e] (.preventDefault e) (dom/style up)))
     (dom/on "mouseenter" (e/fn [e] (.preventDefault e) (dom/style hover)))
     (new body))))

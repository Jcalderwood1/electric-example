(ns app.super-button
  (:require
   [app.color :as color]
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(def button-style
  {:color            (color/color :text)
   :text-shadow      style/text-shadow
   :cursor           "pointer"
   :width            "120px"
   :height           "80px"
   :background-color (color/color :button)
   :border           "none"
   :border-radius    "12px"
   :box-shadow       style/box-shadow
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
         {:box-shadow (str "inset 5px 5px 10px " (color/color :shadow) ", inset -5px -5px 10px " (color/color :highlight))}))

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
  {:background-color (color/color :call-to-action)
   :border           "none"
   :border-radius    "15px"
   :padding          "10px 20px"
   :color            (color/color :call-to-action-text)
   :text-shadow      "1px 1px 2px #B43C62, -1px -1px 2px #5a4875"
   :font-size        "18px"
   :box-shadow       style/box-shadow
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

(ns app.super-button
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(e/defn SuperButton
  [on-click body & [style]]
  (ui/button
   on-click
   (dom/props {:class "super-button"})
   (dom/style style)
   (new body)))

(e/defn CallToActionButton
  [on-click body & [style]]
  (ui/button
   on-click
   (dom/props {:class "cta-button"})
   (dom/style style)
   (new body)))

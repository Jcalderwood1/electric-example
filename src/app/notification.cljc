(ns app.notification
  (:require
   [app.super-button :as sb]
   [app.state :as state]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

#?(:cljs
   (e/def notification (:notification state/db)))

#?(:cljs
   (defn notify!
     [message]
     (state/assoc-in-db! [:notification] message)))

(e/defn Notification []
  (when notification
    (dom/div
      (dom/props
       {:class "notification"})
      (dom/text notification)
      (sb/SuperButton.
       (e/fn [] (notify! nil))
       (e/fn [] (dom/text "Dismiss"))))))

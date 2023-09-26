(ns app.notification
  (:require
   [app.super-button :as sb]
   [app.state :as state]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

(e/defn Notification
  []
  (let [db (e/watch state/!db)
        notification (:notification db)]
    (when notification
      (dom/div
        (dom/props {:class "notification"})
        (dom/text notification)
        (sb/SuperButton.
         (e/fn [] (e/client (state/update-db! [:notification] nil)))
         (e/fn [] (dom/text "Dismiss")))))))

#?(:cljs
   (defn notify!
     [message]
     (state/update-db! [:notification] message)))

(ns app.state.order
  (:require
   [app.menu :as menu]
   [app.state :as state]
   [hyperfiddle.electric :as e]))

#?(:cljs
   (e/def current-order
     (:current-order state/db)))

#?(:cljs
   (e/defn reset-order! []
     (state/assoc-in-db! :current-order {})))

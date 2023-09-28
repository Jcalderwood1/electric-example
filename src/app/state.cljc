(ns app.state
  (:require
   [hyperfiddle.electric :as e]))

(def app-state
  {:notification nil
   :current-order {}
   :cart {}})

#?(:cljs (def !db (atom app-state)))
#?(:cljs (e/def db (e/watch !db)))

#?(:cljs
   (defn update-in-db!
     [path f & args]
     (apply (partial swap! !db update-in path f) args)))

#?(:cljs
   (defn update-db!
     [f & args]
     (apply (partial swap! !db update f) args)))

#?(:cljs
   (defn assoc-in-db!
     [path value]
     (swap! !db assoc-in path value)))

;; -------------------------------------------------------------------

#?(:cljs
   (e/def customer-name
     (get-in db [:current-order :customer :name])))

#?(:cljs
   (defn set-customer-name! [v]
     (assoc-in-db! [:current-order :customer :name] v)))

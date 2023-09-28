(ns app.state.drink
  (:require
   [app.menu :as menu]
   [app.state :as state]
   [hyperfiddle.electric :as e]))

(defn as-drink
  [menu-item size]
  (assoc menu-item
         :add-ins {}
         :size size))

(defn add-in-name
  [id]
  (->> menu/add-ins
       (filter (fn [x] (= (:id x) id)))
       first
       :name))

(defn remove-zero-qty-add-ins
  [drink]
  (update drink
          :add-ins
          (fn [add-ins]
            (into {}
                  (remove (fn [add-in]
                            (<= (-> add-in second :quantity) 0))
                          add-ins)))))

(defn update-add-ins
  [drink add-in-id change]
  (-> drink
      (update-in [:add-ins add-in-id :quantity] change)
      (remove-zero-qty-add-ins)))

#?(:cljs
   (defn remove-add-in!
     [add-in-id]
     (state/update-in-db! [:current-order :drink]
                          dissoc
                          add-in-id)))

#?(:cljs
   (defn inc-add-in-qty!
     [add-in-id]
     (state/update-in-db! [:current-order :drink]
                          update-add-ins
                          add-in-id
                          inc)))

#?(:cljs
   (defn dec-add-in-qty!
     [add-in-id]
     (state/update-in-db! [:current-order :drink]
                          update-add-ins
                          add-in-id
                          dec)))

#?(:cljs
   (defn new-drink!
     [menu-item size]
     (state/assoc-in-db! [:current-order :drink]
                         (as-drink menu-item size))))

#?(:cljs
   (defn reset-add-ins! []
     (state/assoc-in-db! [:current-order :drink :add-ins]
                         {})))

#?(:cljs
   (defn reset-drink! []
     (state/assoc-in-db! [:current-order :drink]
                         nil)))

#?(:cljs
   (defn set-drink-size!
     [size]
     (state/assoc-in-db! [:current-order :drink :size]
                         size)))

#?(:cljs
   (e/def size (get-in state/db [:current-order :drink :size])))

#?(:cljs
   (e/def drink (get-in state/db [:current-order :drink])))

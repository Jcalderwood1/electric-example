(ns app.state.cart
  (:require
   [app.state :as state]
   [hyperfiddle.electric :as e]))

;;;; Cart

(defn cart-item
  [id menu-item]
  {:id id
   :item (assoc menu-item :quantity 1)})

(defn item-in-cart?
  [menu-item {cart-item :item}]
  (when (= (:category menu-item)
           (:category cart-item))
    (case (:category menu-item)
      "drink" (and (= (:id menu-item)
                      (:id cart-item))
                   (= (:add-ins menu-item)
                      (:add-ins cart-item))
                   (= (:size menu-item)
                      (:size cart-item)))
      "treat" (= (:id menu-item)
                 (:id cart-item)))))

(defn matching-cart-item
  [item cart]
  (first
   (filter (partial item-in-cart? item)
           (vals cart))))

(defn remove-zero-quantity
  [cart]
  (->> cart
       (remove #(<= (-> % second :item :quantity) 0))
       (into {})))

(defn update-cart
  [cart menu-item change]
  (let [?match (matching-cart-item menu-item cart)
        cart-item-id (or (:id ?match) (random-uuid))]
    (-> (if (contains? cart cart-item-id)
          (update-in cart [cart-item-id :item :quantity] change)
          (assoc cart
                 cart-item-id
                 (cart-item cart-item-id menu-item)))
        remove-zero-quantity)))

#?(:cljs
   (defn inc-cart-item-qty!
     [menu-item]
     (state/update-in-db! [:cart] update-cart menu-item inc)))

#?(:cljs
   (defn dec-cart-item-qty!
     [menu-item]
     (state/update-in-db! [:cart] update-cart menu-item dec)))

#?(:cljs
   (defn remove-cart-item! [k]
     (state/update-in-db! [:cart] dissoc k)))

#?(:cljs
   (defn empty-cart! []
     (state/assoc-in-db! [:cart] {})))

#?(:cljs
   (e/def cart (:cart state/db)))

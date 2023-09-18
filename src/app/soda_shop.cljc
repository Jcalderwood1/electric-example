(ns app.soda-shop
  (:require
   contrib.str
   [app.menu :as menu]
   [app.super-button :as sb]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

#?(:clj (def !orders (atom [])))
(e/def orders (e/server (e/watch !orders)))

#?(:clj (def !order-number (atom 1000)))
(e/def order-number (e/server (e/watcj !order-number)))

#?(:clj (def !notification (atom nil)))
(e/def notification (e/server (e/watch !notification)))

(e/defn Notification
  []
  (when notification
    (dom/div
      (dom/style
       {:position         "fixed"
        :top              "10px"
        :right            "10px"
        :background-color "#D4EDDA"
        :padding          "20px"
        :border           "1px solid #C3E6CB"
        :border-radius    "5px"
        :box-shadow       "0 0 10px rgba(0, 0, 0, 0.1)"})
      (dom/text notification)
      (sb/SuperButton.
       (e/fn [] (e/server (reset! !notification nil)))
       (e/fn [] (dom/text "Dismiss"))))))

(defn cart-item
  [id menu-item]
  {:id id
   :item (assoc menu-item :quantity 1)})

(defn as-drink
  [menu-item]
  (assoc menu-item :add-ins {}))

(defn item-in-cart?
  [menu-item {cart-item :item}]
  (when (= (:category menu-item)
           (:category cart-item))
    (case (:category menu-item)
      "drink" (and (= (:id menu-item)
                      (:id cart-item))
                   (= (:add-ins menu-item)
                      (:add-ins cart-item)))
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
            
(def text-style
  {:color       "#8C8C8C"
   :text-shadow "1px 1px 2px #ffffff, -1px -1px 2px #b8b8b8"})

(def grid-style
  {:display               "grid" 
   :grid-template-columns "repeat(4, 1fr)" 
   :grid-gap              "1rem" 
   :justify-content       "center" 
   :align-items           "center" 
   :max-width             "800px"
   :margin                "0 auto"})

(e/defn Menu
  [!cart !drink]
  (dom/div
    (dom/h2
      (dom/style text-style)
      (dom/text "Menu"))
    (dom/div
      (dom/style grid-style)
      (e/server
        (e/for-by :id [menu-item menu/items]
          (e/client
            (sb/SuperButton.
             (e/fn []
               (case (:category menu-item)
                 "drink" (reset! !drink (as-drink menu-item))
                 "treat" (swap! !cart update-cart menu-item inc)))
             (e/fn []
               (dom/text (:name menu-item)))
             {:margin "10px"})))))))

(defn add-in-name
   [id]
   (->> menu/add-ins
        (filter (fn [x] (= (:id x) id)))
        first
        :name))

(e/defn AddInList
  [drink]
  (dom/div
    (e/for-by first [[id add-in] (:add-ins drink)]
      (e/client
        (dom/div
          (dom/text (:quantity add-in) " x " )
          (dom/text
           (e/server (add-in-name id))))))))

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

(defn inc-add-in-qty!
  [!drink add-in-id]
  (swap! !drink update-add-ins add-in-id inc))

(defn dec-add-in-qty!
  [!drink add-in-id]
  (swap! !drink update-add-ins add-in-id dec))

(defn remove-add-in!
  [!drink add-in-id]
  (swap! !drink update :add-ins dissoc add-in-id))

(defn reset-add-ins!
  [!drink]
  (swap! !drink assoc :add-ins {}))

(e/defn QuantityWidget
  [quantity increase-fn decrease-fn]
  (let [style {:width            "50px"
               :height           "50px"
               :border-radius    "50%"}]
    (dom/div
      (dom/props {:class "button"})
      (dom/style
       {:display          "flex"
        :align-items      "center"})
      (sb/SuperButton.
       decrease-fn
       (e/fn []
         (dom/text "-"))
       style)
      (dom/div
        (dom/style
         (merge text-style
                {:width           "40px"
                 :height          "50px"
                 :font-size       "24px"
                 :display         "flex"
                 :align-items     "center"
                 :justify-content "center"}))
        (dom/text quantity))
      (sb/SuperButton.
       increase-fn
       (e/fn []
         (dom/text "+"))
       style))))

(e/defn AddInEditor
  [!drink drink]
  (e/for-by first [[id add-in] (:add-ins drink)]
    (e/client
      (dom/div
        (dom/style
         (merge
          card-style
          {:display         "flex"
           :align-items     "center"
           :justify-content "space-between"}))
        (QuantityWidget.
         (:quantity add-in)
         (e/fn []
           (e/client
             (inc-add-in-qty! !drink id)))
         (e/fn []
           (e/client
             (dec-add-in-qty! !drink id))))
        (dom/h3
          (dom/text
           (e/server (add-in-name id))))
        (dom/div
          (sb/SuperButton.
           (e/fn []
             (e/client
               (remove-add-in! !drink id)))
           (e/fn []
             (dom/text "remove"))
           {:height "50px"}))))))

(defn inc-cart-item-qty!
  [!cart item]
  (swap! !cart update-cart item inc))

(defn dec-cart-item-qty!
  [!cart item]
  (swap! !cart update-cart item dec))

(defn remove-item-from-cart!
  [!cart k]
  (println @!cart)
  (swap! !cart dissoc k))

(def section-style
  {:margin-top    "20px"
   :margin-bottom "20px"
   :color         "#8C8C8C"
   :text-shadow   "1px 1px 2px #ffffff, -1px -1px 2px #b8b8b8"
   :background    "#e0e0e0"
   :border-radius "10px"
   :padding       "10px"
   :overflow-y    "auto"
   :max-height    "300px"
   :max-width     "800px"
   :box-shadow    "inset 5px 5px 10px #b8b8b8, inset -5px -5px 10px #ffffff"})

(defn empty-cart!
  [!cart]
  (reset! !cart {}))

(e/defn Section
  [BodyContent & [style]]
  (dom/div
    (dom/style (merge section-style style))
    (dom/div
      (BodyContent.))))

(def card-style
  {:color            "#8C8C8C"
   :text-shadow      "1px 1px 2px #ffffff, -1px -1px 2px #b8b8b8"
   :background-color "#e0e0e0"
   :border           "none"
   :border-radius    "12px"
   :box-shadow       "7px 7px 14px #b8b8b8, -7px -7px 14px #ffffff"
   :margin           "20px 30px"
   :font-size        "18px"
   :outline          "none"
   :padding          "20px"})

(e/defn CurrentOrderListItem
  [!cart {:keys [item id]}]
  (dom/div
    (dom/style card-style)
    (dom/div
      (dom/style
       {:display         "flex"
        :flex-direction  "column"
        :min-height      "50px"
        :align-items     "flex-start"
        :justify-content "flex-start"})
      (dom/div
        (dom/style
         {:display         "flex"
          :justify-content "space-between"
          :align-items     "center"
          :width           "100%"})
        (QuantityWidget.
         (:quantity item)
         (e/fn []
           (e/client
             (inc-cart-item-qty! !cart item)))
         (e/fn []
           (e/client
             (dec-cart-item-qty! !cart item))))
        (dom/div
          (dom/style text-style)
          (dom/h3 (dom/text (:name item)))
          (AddInList. item))
        (dom/div
          (sb/SuperButton.
           (e/fn []
             (e/client
               (remove-item-from-cart! !cart id)))
           (e/fn []
             (dom/text "remove"))
           {:height "50px"}))))))

(defn notify!
  [message]
  (reset! !notification message))

(e/defn CurrentOrder
  [!cart]
  (let [cart (e/watch !cart)]
    (Section.
     (e/fn []
       (when (not-empty cart)
         (e/for-by first [[k v] cart]
           (CurrentOrderListItem. !cart v)))))
    (dom/div
      (dom/style
       {:display         "flex"
        :justify-content "center"})
      (sb/CallToActionButton.
       (e/fn []
         (when (not-empty cart)
           (e/server
             (let [order-number (swap! !order-number inc)]
               (swap! !orders
                      conj
                      {:items    (mapv (comp :item second)
                                       (e/client @!cart))
                       :order-id order-number})
               (notify! (str "Order " order-number " placed successfully!")))))
         (e/client
           (reset! !cart {})))
       (e/fn []
         (dom/text "Place Order"))
       {:width  "300px"
        :height "50px"}))))

(e/defn PlacedOrders []
  (Section.
   (e/fn []
     (when (not-empty orders)
       (e/for-by :order-id [{:keys [order-id items]} orders]
         (dom/div
           (dom/style card-style)
           (dom/h3
             (dom/style text-style)
             (dom/text (str "Order " order-id ":")))
           (e/for-by :id [item items]
             (dom/div
               (dom/style {:padding "10px"})
               (dom/div
                 (dom/style
                  {:display         "flex"
                   :justify-content "space-between"
                   :padding         "5px"})
                 (dom/span
                   (dom/style text-style)
                   (dom/text (:name item)))
                 (dom/span
                   (dom/style text-style)
                   (dom/text (str "Quantity: " (:quantity item)))))
               (when-let [add-ins (:add-ins item)]
                 (dom/div
                   (dom/style {:padding-left "20px"})
                   (AddInList. item)))))))))))

(e/defn AddIns
  [!drink drink-name]
  (dom/div
    (dom/style text-style)
    (dom/h2
      (dom/text "Customize " drink-name))
    (dom/div
      (dom/style
       (merge
        grid-style
        {:margin-bottom "40px"
         :margin-top    "40px"}))
      (e/server
        (e/for-by :id [add-in menu/add-ins]
          (e/client
            (dom/div
              (sb/SuperButton.
               (e/fn []
                 (inc-add-in-qty! !drink (:id add-in)))
               (e/fn []
                 (dom/text (:name add-in)))
               {:height "100px"}))))))))

(e/defn CustomizeDrink
  [!cart !drink drink]
  (dom/div
    (AddIns. !drink (:name drink))
    (dom/div
      (dom/style text-style)
      (Section.
       (e/fn []
         (when (not-empty (:add-ins drink))
           (AddInEditor. !drink drink)))))
    (dom/div
      (dom/style
       {:display         "flex"
        :justify-content "space-between"
        :align-items     "center"})
      (sb/SuperButton.
       (e/fn []
         (reset! !drink nil))
       (e/fn []
         (dom/text "Back"))
       {:width  "150px"
        :height "50px"})
      (sb/CallToActionButton.
       (e/fn []
         (inc-cart-item-qty! !cart drink)
         (reset! !drink nil))
       (e/fn []
         (dom/text "Add to Cart"))
       {:width  "150px"
        :height "50px"}))))

(e/defn point-of-sale []
  (e/client
    (dom/div
      (dom/style
       {:display          "flex"
        :justify-content  "center"
        :background-color "#DADADA"
        :padding          "40px"})
      (let [!cart  (atom {})
            !drink (atom nil)
            drink  (e/watch !drink)]
        (if drink
          (CustomizeDrink. !cart !drink drink)
          (dom/div
            (Notification.)
            (Menu. !cart !drink)
            (CurrentOrder. !cart)
            (PlacedOrders.)))))))

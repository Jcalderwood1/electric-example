(ns app.soda-shop
  (:require
   contrib.str
   [app.card :as card]
   [app.color :as color]
   [app.menu :as menu]
   [app.notification :as notification]
   [app.state :as state]
   [app.super-button :as sb]
   [app.tabs :as tabs]
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

#?(:clj (def !orders (atom [])))
(e/def orders (e/server (e/watch !orders)))

#?(:clj (def !order-number (atom 1000)))
(e/def order-number (e/server (e/watch !order-number)))

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

(e/defn DrinkSizeSelector []
  (let [!selected-size (atom 16)
        selected-size  (e/watch !selected-size)
        sizes          [16 24 32 44]
        num-sizes      (count sizes)
        index-of       (fn [c item] (.indexOf c item))]
    (dom/div
      (dom/props
       {:class "slider-container"})
      (.setProperty (.-style dom/node) "--num-sizes" num-sizes)
      (e/on-unmount #(.removeProperty (.-style dom/node) "--num-sizes"))
      (dom/div
        (dom/props {:class "slider-channel"})
        (let [channel-width (some-> js/document
                                    (.querySelector ".slider-channel")
                                    .getBoundingClientRect
                                    .-width)
              step-size     (int (/ channel-width num-sizes))]
          (.setProperty (.-style dom/node) "--step-size" (str step-size "px"))
          (e/on-unmount #(.removeProperty (.-style dom/node) "--step-size")))
        (dom/div
          (dom/props {:class "slider-pill"})
          (dom/style {:transform (str "translateX(calc(var(--step-size) * " (index-of sizes selected-size) "))")})
          (dom/text (str selected-size " oz")))
        (e/for-by identity [size sizes]
          (dom/div
            (dom/props {:class "size-label"})
            (dom/style {:left (str "calc(var(--pill-width) * " (index-of sizes size) ")")})
            (dom/on "click" (e/fn [e] (.preventDefault e) (reset! !selected-size size)))
            (dom/text (str size " oz"))))))))

(e/defn Menu
  [!cart !drink]
  (let [!item-type (atom nil)
        item-type  (e/watch !item-type)]
    (dom/div
      (DrinkSizeSelector.)
      (dom/div
        (dom/props {:class "menu-grid"})
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
               {:margin "10px"}))))))))

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
  (let [style {:width         "50px"
               :height        "50px"
               :border-radius "50%"}]
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
         (merge style/text
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
      (card/Card.
       (e/fn []
         (QuantityWidget.
          (:quantity add-in)
          (e/fn []
            (e/client
              (inc-add-in-qty! !drink id)))
          (e/fn []
            (e/client
              (dec-add-in-qty! !drink id))))
         (dom/text
          (e/server (add-in-name id)))
         (dom/div
           (sb/SuperButton.
            (e/fn []
              (e/client
                (remove-add-in! !drink id)))
            (e/fn []
              (dom/text "remove"))
            {:height "50px"})))
       {:display         "flex"
        :align-items     "center"
        :justify-content "space-between"}))))

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

(defn empty-cart!
  [!cart]
  (reset! !cart {}))

(e/defn Section
  [BodyContent & [style]]
  (dom/div
    (dom/props {:class "section"})
    (dom/style style)
    (dom/div
      (BodyContent.))))

(e/defn CurrentOrderListItem
  [!cart {:keys [item id]}]
  (card/Card.
   (e/fn []
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
           (dom/style style/text)
           (dom/h3 (dom/text (:name item)))
           (AddInList. item))
         (dom/div
           (sb/SuperButton.
            (e/fn []
              (e/client
                (remove-item-from-cart! !cart id)))
            (e/fn []
              (dom/text "remove"))
            {:height "50px"})))))))

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
               (e/client
                 (notification/notify! (str "Order " (e/server order-number) " placed successfully!"))))))
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
         (card/Card.
          (e/fn []
            (dom/h3
              (dom/style style/text)
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
                    (dom/style style/text)
                    (dom/text (:name item)))
                  (dom/span
                    (dom/style style/text)
                    (dom/text (str "Quantity: " (:quantity item)))))
                (when-let [add-ins (:add-ins item)]
                  (dom/div
                    (dom/style {:padding-left "20px"})
                    (AddInList. item))))))))))))

(e/defn AddIns
  [!drink drink-name]
  (dom/div
    (dom/style style/text)
    (dom/h2
      (dom/text "Customize " drink-name))
    (dom/div
      (dom/props {:class "menu-grid"})
      (e/server
        (e/for-by :id [add-in menu/add-ins]
          (e/client
            (dom/div
              (sb/SuperButton.
               (e/fn []
                 (inc-add-in-qty! !drink (:id add-in)))
               (e/fn []
                 (dom/text (:name add-in)))
               {:margin "10px"}))))))))

(e/defn CustomizeDrink
  [!cart !drink drink]
  (dom/div
    (AddIns. !drink (:name drink))
    (dom/div
      (dom/style style/text)
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

(e/defn Logo []
  (dom/div
    (dom/props
     {:class "logo-container"})
    (dom/div
      (dom/img
        (dom/props
         {:src   "/images/logo.png"
          :class "logo"})))))

(e/defn point-of-sale []
  (e/client
    (dom/div
      (dom/div
        (dom/props {:class "navbar"})
        (Logo.))
      (let [!cart  (atom {})
            !drink (atom nil)
            drink  (e/watch !drink)]
        (dom/div
          (dom/props
           {:class "main-section"})
          (dom/div
            (dom/props
             {:class "section-1"})
            (if drink
              (CustomizeDrink. !cart !drink drink)
              (Menu. !cart !drink)))
          (dom/div
            (dom/props
             {:class "section-2"})
            (CurrentOrder. !cart))
          (notification/Notification.))))))

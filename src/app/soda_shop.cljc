(ns app.soda-shop
  (:require
   contrib.str
   [app.card :as card]
   [app.color :as color]
   [app.menu :as menu]
   [app.notification :as notification]
   [app.state :as state]
   [app.state.cart :as cart-state]
   [app.state.drink :as drink-state]
   [app.super-button :as sb]
   [app.style :as style]
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

#?(:clj (def !orders (atom [])))
(e/def orders (e/server (e/watch !orders)))

#?(:clj (def !order-number (atom 1000)))
(e/def order-number (e/server (e/watch !order-number)))

(defn inc-order-number! []
  (swap! !order-number inc))

(e/defn InputSubmit
  [f]
  (dom/input
    (dom/props
     {:placeholder (if (not-empty state/customer-name)
                     (str "Order for " state/customer-name)
                     "Enter a name for the order")
      :class       "text-input"})
    (dom/on "keydown"
            (e/fn [e]
              (when (= "Enter" (.-key e))
                (when-some [v (contrib.str/empty->nil (-> e .-target .-value))]
                  (f v)
                  (.blur dom/node)
                  (set! (.-value dom/node) nil)))))))

(e/defn NameInput []
  (e/client
    (new InputSubmit state/set-customer-name!)))

(e/defn RadioOption
  [id label f]
  (dom/div
    (dom/props
     {:class "wrapper"})
    (dom/input
      (dom/props
       {:class "state"
        :type  "radio"
        :name  "app"
        :id    id
        :value id})
      (dom/on "change" (e/fn [_] (f id))))
    (dom/label
      (dom/props
       {:class "label" :for id})
      (dom/div
        (dom/props
         {:class "indicator"}))
      (dom/span
        (dom/props
         {:class "text"})
        (dom/text label)))))

(e/defn DrinkSizeSelector []
  (dom/div
    (dom/props
     {:class "radiogroup"})
    (e/for-by identity [size [16 24 32 44]]
      (new RadioOption size size drink-state/set-drink-size!))))

(e/defn Section
  [BodyContent & [style]]
  (dom/div
    (dom/props {:class "section"})
    (dom/style style)
    (dom/div
      (new BodyContent))))

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
      (new sb/SuperButton
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
      (new sb/SuperButton
           increase-fn
           (e/fn []
             (dom/text "+"))
           style))))

(e/defn CurrentOrder []
  (let [cart cart-state/cart]
    (new Section
         (e/fn []
           (when (not-empty cart)
             (e/for-by first [[k v] cart]
               (new CurrentOrderListItem v)))))
    (dom/div
      (dom/style
       {:display         "flex"
        :justify-content "center"})
      (new sb/CallToActionButton
       (e/fn []
         (when (not-empty cart)
           (e/server
             (let [order-number (inc-order-number!)]
               (swap! !orders
                      conj
                      {:items    (mapv (comp :item second) (e/client cart))
                       :order-id order-number})
               (e/client
                 (state/set-customer-name! nil)
                 (notification/notify! (str "Order " (e/server order-number) " placed successfully!"))))))
         (e/client
           (cart-state/empty-cart!)))
       (e/fn []
         (dom/text "Place Order"))
       {:width  "300px"
        :height "50px"}))))

(e/defn PlacedOrders []
  (new Section
       (e/fn []
         (println "orders" orders)
         (when (not-empty orders)
           (e/for-by :order-id [{:keys [order-id items]} orders]
             (new card/Card
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
                            (new AddInList item))))))))))))

(e/defn Menu []
  (let [!customize? (atom false)
        customize?  (e/watch !customize?)
        close-fn    #(reset! !customize? false)]
    (if customize?
      (new CustomizeDrink close-fn)
      (dom/div
        (new DrinkSizeSelector)
        (dom/div
          (dom/props {:class "menu-grid"})
          (e/server
            (e/for-by :id [menu-item menu/items]
              (e/client
                (new sb/SuperButton
                     (e/fn []
                       (case (:category menu-item)
                         "drink" (do
                                   (drink-state/new-drink! menu-item drink-state/size)
                                   (reset! !customize? true))
                         "treat" (cart-state/inc-cart-item-qty! menu-item)))
                     (e/fn []
                       (dom/text (:name menu-item)))
                     {:margin "10px"})))))))))

(e/defn AddInEditor
  [drink]
  (e/for-by first [[id add-in] (:add-ins drink)]
    (e/client
      (new card/Card
       (e/fn []
         (new QuantityWidget
          (:quantity add-in)
          (e/fn []
            (e/client
              (drink-state/inc-add-in-qty! id)))
          (e/fn []
            (e/client
              (drink-state/dec-add-in-qty! id))))
         (dom/text
          (e/server (add-in-name id)))
         (dom/div
           (new sb/SuperButton
                (e/fn []
                  (e/client
                    (drink-state/remove-add-in! id)))
                (e/fn []
                  (dom/text "remove"))
                {:height "50px"})))
       {:display         "flex"
        :align-items     "center"
        :justify-content "space-between"}))))

(e/defn AddInList
  [drink]
  (dom/div
    (e/for-by first [[id add-in] (:add-ins drink)]
      (e/client
        (dom/div
          (dom/text (:quantity add-in) " x " )
          (dom/text
           (e/server (add-in-name id))))))))

(e/defn CurrentOrderListItem
  [{:keys [item id]}]
  (new card/Card
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
         (new QuantityWidget
          (:quantity item)
          (e/fn []
            (e/client
              (cart-state/inc-cart-item-qty! item)))
          (e/fn []
            (e/client
              (cart-state/dec-cart-item-qty! item))))
         (dom/div
           (dom/style style/text)
           (dom/h3 (dom/text (when (= "drink" (:category item))
                               (str (:size item) " oz "))
                             (:name item)))
           (new AddInList item))
         (dom/div
           (new sb/SuperButton
            (e/fn []
              (e/client
                (cart-state/remove-cart-item! id)))
            (e/fn []
              (dom/text "remove"))
            {:height "50px"})))))))

(e/defn AddIns
  [drink-name]
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
              (new sb/SuperButton
               (e/fn []
                 (drink-state/inc-add-in-qty! (:id add-in)))
               (e/fn []
                 (dom/text (:name add-in)))
               {:margin "10px"}))))))))

(e/defn CustomizeDrink
  [close-fn]
  (let [drink drink-state/drink]
    (dom/div
      (new AddIns (:name drink))
      (dom/div
        (dom/style style/text)
        (new Section
             (e/fn []
               (when (not-empty (:add-ins drink))
                 (new AddInEditor drink)))))
      (dom/div
        (dom/style
         {:display         "flex"
          :justify-content "space-between"
          :align-items     "center"})
        (new sb/SuperButton
             (e/fn []
               (close-fn)
               (drink-state/reset-drink!))
             (e/fn []
               (dom/text "Back"))
             {:width  "150px"
              :height "50px"})
        (new sb/CallToActionButton
             (e/fn []
               (close-fn)
               (cart-state/inc-cart-item-qty! drink)
               (drink-state/reset-drink!))
             (e/fn []
               (dom/text "Add to Cart"))
             {:width  "150px"
              :height "50px"})))))

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
        (new Logo)
        (new NameInput))
      (dom/div
        (dom/props
         {:class "main-section"})
        (dom/div
          (dom/props
           {:class "section-1"})
          (new Menu)
          (new PlacedOrders))
        (dom/div
          (dom/props
           {:class "section-2"})
          (new CurrentOrder))
        (new notification/Notification)))))
